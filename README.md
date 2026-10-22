# logUself-codex-start
A personal and professional task management and time tracking application that helps users organize activities, record time spent on tasks, and analyze how they allocate their time.

logUself verwaltet Projekte, Tickets mit dynamischen Eintragstypen und strukturierte Erfassungen für persönliche, familiäre und betriebliche Nutzung.

## Projektstand

Der erste Browser-Ablauf ist umgesetzt: Angular zeigt Tickets und Sporterfassungen an, legt neue Daten über das Kotlin-Backend in PostgreSQL ab und berechnet die Gesamtdauer am Ticket. Build und Browsertests sind erfolgreich. M1 ist als lokaler Prototyp nutzbar; die weiteren Anforderungen der Spec sind noch nicht vollständig umgesetzt. Git ist bereits initialisiert.

- [Funktionale und technische Spec](doc_logUself/spec.me)
- [Arbeitsplan und Erledigungsstand](doc_logUself/TODO.md)

## Erster testbarer Meilenstein M1

Im Browser ein Ticket „Mein Sport“ im vorbelegten Projekt „PrivatMartin“ anlegen, eine Sporterfassung speichern und nach dem Neuladen wieder anzeigen. Die Dauer-Summe wird am Ticket dargestellt.

Geplanter Aufbau:

- Frontend: Angular und TypeScript, zunächst im Browser.
- Backend: Spring Boot mit Kotlin und REST-API.
- Datenbank: PostgreSQL über Docker Compose, Migrationen und JSONB für dynamische Daten gemäß Spec.
- Electron: nach dem ersten funktionierenden Browser-Ablauf.

## Voraussetzungen und lokaler Betrieb

Geprüft sind Node.js 24.18.0, npm 11.16.0 und JDK 21.0.4 unter `C:\Program Files\Java\jdk-21`. Derzeit zeigt `java` im PATH auf Java 23; das Backend-Skript verwendet bei nicht gesetztem `JAVA_HOME` JDK 21. Eine vorhandene PostgreSQL-Instanz ist nicht erforderlich.

Das Backend verwendet Spring Boot 4.0.8, Kotlin 2.2.21 und Java 21. Der Maven-Wrapper ist enthalten. Das Frontend verwendet Angular 22.2 und TypeScript 6.0; die aufgelösten Abhängigkeiten stehen in `frontend/package-lock.json`.

Geprüfte lokale Docker-Umgebung:

- Docker Desktop ist gestartet und über die CLI erreichbar (Desktop 4.34.2, Engine 27.2.0).
- Docker Compose ist installiert: `docker-compose version` meldet `v2.29.2-desktop.2`.
- In dieser Umgebung funktioniert `docker-compose` mit Bindestrich. `docker compose` mit Leerzeichen wird derzeit nicht erkannt; für die lokale Einrichtung verwenden wir deshalb `docker-compose`.
- PostgreSQL 17.11 läuft über `postgres:17-alpine` mit dem Volume `loguself-dev_postgres_data`. Das Image bleibt auf Hauptversion 17 begrenzt; zukünftige Downloads können neuere 17er-Patchstände liefern.
- Der Zugriff aus der eingeschränkten Agentenumgebung erforderte eine Freigabe. Das ist von der lokalen Docker-Installation zu unterscheiden und bedeutet nicht, dass jedes normale Terminal Administratorrechte benötigt.

Lokale Datenbankzugangsdaten gehören in eine nicht versionierte Konfiguration; eine Vorlage kann Platzhalter enthalten. Bis zur Umsetzung von Anmeldung und Zugriffsschutz wird der Prototyp ausschließlich lokal mit Testdaten betrieben.

### PostgreSQL starten und prüfen

Alle Befehle im Repository-Hauptverzeichnis ausführen. Die lokale `.env` wurde bereits mit einem zufälligen Passwort erstellt und ist von Git ausgeschlossen. Auf einem neuen Rechner zunächst `Copy-Item .env.example .env` ausführen und das Platzhalterpasswort in `.env` ersetzen.

```powershell
docker-compose config --quiet
docker-compose up -d --wait postgres
docker-compose ps
'SELECT current_database(), current_user;' | docker-compose exec -T postgres psql -U loguself_dev -d loguself -v ON_ERROR_STOP=1
```

Verbindung vom Rechner: Host `127.0.0.1`, Port `5433`, Datenbank `loguself`, Benutzer `loguself_dev`, Passwort aus `.env`. Der Port ist ausschließlich an die lokale Loopback-Adresse gebunden. Der durch das Image angelegte Benutzer ist ein lokaler Entwicklungsadministrator; getrennte eingeschränkte Anwendungsrechte folgen vor dem produktiven Einsatz.

```powershell
# Stoppen, Container behalten
docker-compose stop postgres
# Container und Projektnetzwerk entfernen, Datenvolume behalten
docker-compose down
# Mit denselben Daten wieder starten
docker-compose up -d --wait postgres
```

`down` ohne `--volumes` erhält die Daten. `down --volumes` würde die Daten dieses Projekts löschen. Änderungen an Initialisierungswerten in `.env`, insbesondere am Passwort, ändern keine bereits initialisierte Datenbank.

Verifiziert: Konfiguration gültig, Container healthy, TCP-Port 5433 erreichbar, Passwortanmeldung erfolgreich. Ein Testdatensatz blieb nach `down` und erneutem `up` erhalten; Testtabelle und Testschema wurden anschließend entfernt. Anwendungstabellen und die Beispieldaten PrivatMartin/Sport werden beim Backendstart durch Flyway angelegt.

### Backend starten und testen

In PowerShell im Repository-Hauptverzeichnis:

```powershell
docker-compose up -d --wait postgres
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/backend.ps1 run
```

Das Skript lädt ausschließlich die PostgreSQL-Konfiguration aus `.env` und verwendet JDK 21, wenn `JAVA_HOME` noch nicht gesetzt ist. Ein vorhandenes `JAVA_HOME` muss auf ein geeignetes JDK zeigen. Beim ersten Aufruf lädt der Wrapper Maven und Abhängigkeiten aus dem Internet. Der Server bindet an `127.0.0.1:8080`. Stoppen im ausführenden Terminal mit Strg+C.

```powershell
# In einem zweiten Terminal: Integrationstest gegen PostgreSQL
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/backend.ps1 test
# API lesen
Invoke-RestMethod http://127.0.0.1:8080/api/projects
Invoke-RestMethod http://127.0.0.1:8080/api/tickets
```

Der Integrationstest nutzt das separate Schema `loguself_test` und entfernt seine Tickets und Erfassungen anschließend. Er prüft echte HTTP-Requests, Speicherung, 120 + 30 Minuten, JSONB-Sportart, unverändertes Kalenderdatum, ungültige Eingaben und fehlende Tickets. PostgreSQL muss dafür laufen. Der normale Entwicklungsbetrieb verwendet das Schema `public`.

Verfügbare API:

| Methode | Pfad | Zweck |
| --- | --- | --- |
| GET | `/api/projects` | Projekte auflisten |
| GET | `/api/entry-types` | Typen auflisten |
| GET / POST | `/api/tickets` | Tickets auflisten / erstellen |
| GET | `/api/tickets/{id}` | Einzelnes Ticket lesen |
| GET / POST | `/api/tickets/{id}/records` | Erfassungen samt `totalMinutes` lesen / Sporterfassung speichern |

Ticket erstellen: JSON mit `projectId`, `entryTypeId`, `title` und optional `description`. Die vorbelegten IDs sind `00000000-0000-0000-0000-000000000001` (PrivatMartin) und `00000000-0000-0000-0000-000000000002` (Sport). Sporterfassung erstellen: JSON mit `date` (YYYY-MM-DD), `durationMinutes` (positive Ganzzahl), `sport` und optional `description`.

M1 unterstützt derzeit ausdrücklich nur die vorbereitete Sportdefinition und ihre festen Validierungen. Ein generischer Typeditor, beliebige Schemaauswertung, Authentifizierung und Historie sind noch nicht umgesetzt. Beim manuellen API-Test wurde ein Demo-Ticket „Mein Sport“ mit 120 Minuten angelegt und bleibt als lokaler Beispieldatensatz erhalten.

## Prüfung von M1

### Angular starten

Backend und PostgreSQL müssen laufen. In einem weiteren PowerShell-Terminal:

```powershell
cd frontend
npm.cmd ci
npm.cmd start
```

Öffnen: [logUself lokal](http://127.0.0.1:4200). Bei bereits installierten Abhängigkeiten kann `npm.cmd ci` entfallen. Stoppen mit Strg+C. Der Entwicklungsproxy leitet `/api` an `127.0.0.1:8080` weiter. Der Angular-Server ist nur lokal erreichbar. Für eine spätere Auslieferung des Builds muss separat eine Weiterleitung für `/api` eingerichtet werden.

Ein Ticket auswählen oder im Projekt PrivatMartin ein Sport-Ticket anlegen. Dann Datum, Sportart und positive Dauer in Minuten eingeben. Die Oberfläche zeigt alle Erfassungen des Tickets und die Gesamtdauer; es gibt noch keine Wochenfilterung. Datumsvorbelegung erfolgt für Europe/Vienna.

```powershell
# Produktionsbuild prüfen
npm.cmd run build
# Echte Browsertests (laufendes Frontend, Backend und PostgreSQL erforderlich)
npm.cmd run test:e2e
```

Die Playwright-Tests nutzen lokal installiertes Microsoft Edge im Headless-Modus. Sie prüfen Ticketanlage, zwei Erfassungen mit insgesamt 150 Minuten, gesperrtes Speichern bei Dauer 0, Datenerhalt nach Neuladen, schmale Darstellung und Fehlermeldung samt Wiederholen bei API-Ausfall. Jeder Durchlauf hinterlässt ein gekennzeichnetes `Browsercheck …`-Ticket mit Testdaten. Screenshots liegen unter `frontend/test-results/` (nicht versioniert).

Vite ist per npm-Override auf Patchstand 8.3.5 festgelegt, da die initial erzeugte Angular-Build-Abhängigkeit einen von npm gemeldeten fehlerhaften Stand verwendete. Nach Aktualisierung meldet npm keine bekannten Lücken. Bei einem späteren Angular-Update ist zu prüfen, ob der Override noch erforderlich ist.

### Manuelle Abnahme

1. Ticket über die Oberfläche erstellen und wieder laden.
2. Zwei Sporterfassungen mit 120 und 30 Minuten speichern; die Summe beträgt 2 Stunden 30 Minuten.
3. Oberfläche neu laden und Backend neu starten; Tickets, Erfassungen und Summe bleiben erhalten.
4. Dauer 0 und negative Dauer werden serverseitig abgewiesen und mit verständlicher Fehlermeldung angezeigt.

Die vollständigen Anforderungen für Version 1 gehen über diesen ersten Meilenstein hinaus. Die weitere Reihenfolge und offene Arbeiten stehen im Arbeitsplan.
