# logUself-codex-start
A personal and professional task management and time tracking application that helps users organize activities, record time spent on tasks, and analyze how they allocate their time.

logUself verwaltet Projekte, Tickets mit dynamischen Eintragstypen und strukturierte Erfassungen für persönliche, familiäre und betriebliche Nutzung.

## Projektstand

Die lokale PostgreSQL-Entwicklungsdatenbank und das Kotlin-Backend sind eingerichtet. Tickets und Sporterfassungen können über die REST-API gespeichert und geladen werden. Das Angular-Frontend ist noch nicht implementiert; M1 ist deshalb noch nicht vollständig abgeschlossen. Git ist bereits initialisiert.

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

Geprüft sind Node.js 24.18.0, npm 11.16.0 und JDK 21.0.4 unter `C:\Program Files\Java\jdk-21`. Derzeit zeigt `java` im PATH auf Java 23; für das spätere Backend ist JDK 21 vorgesehen. Die konkrete Spring-Boot-/Angular-Version wird beim Anlegen der Anwendungen festgelegt. Eine vorhandene PostgreSQL-Instanz ist nicht erforderlich.

Das Backend verwendet Spring Boot 4.0.8, Kotlin 2.2.21 und Java 21. Der Maven-Wrapper ist enthalten. Frontend-Kommandos folgen nach dessen Einrichtung.

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

1. Ticket über die Oberfläche erstellen und wieder laden.
2. Zwei Sporterfassungen mit 120 und 30 Minuten speichern; die Summe beträgt 2 Stunden 30 Minuten.
3. Oberfläche neu laden und Backend neu starten; Tickets, Erfassungen und Summe bleiben erhalten.
4. Dauer 0 und negative Dauer werden serverseitig abgewiesen und mit verständlicher Fehlermeldung angezeigt.

Die vollständigen Anforderungen für Version 1 gehen über diesen ersten Meilenstein hinaus. Die weitere Reihenfolge und offene Arbeiten stehen im Arbeitsplan.
