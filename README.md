# logUself-codex-start
A personal and professional task management and time tracking application that helps users organize activities, record time spent on tasks, and analyze how they allocate their time.

logUself verwaltet Projekte, Tickets mit dynamischen Eintragstypen und strukturierte Erfassungen für persönliche, familiäre und betriebliche Nutzung.

## Projektstand

Die Anforderungen und der erste Umsetzungsschritt sind dokumentiert. Die lokale PostgreSQL-Entwicklungsdatenbank ist eingerichtet und geprüft. Backend und Frontend sind noch nicht implementiert. Git ist bereits initialisiert.

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

Backend- und Frontend-Kommandos folgen nach deren Einrichtung. Die Datenbank kann bereits mit den folgenden Befehlen gestartet werden.

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

Verifiziert: Konfiguration gültig, Container healthy, TCP-Port 5433 erreichbar, Passwortanmeldung erfolgreich. Ein Testdatensatz blieb nach `down` und erneutem `up` erhalten; Testtabelle und Testschema wurden anschließend entfernt. Anwendungstabellen werden erst mit den Backend-Migrationen angelegt.

## Prüfung von M1

1. Ticket über die Oberfläche erstellen und wieder laden.
2. Zwei Sporterfassungen mit 120 und 30 Minuten speichern; die Summe beträgt 2 Stunden 30 Minuten.
3. Oberfläche neu laden und Backend neu starten; Tickets, Erfassungen und Summe bleiben erhalten.
4. Dauer 0 und negative Dauer werden serverseitig abgewiesen und mit verständlicher Fehlermeldung angezeigt.

Die vollständigen Anforderungen für Version 1 gehen über diesen ersten Meilenstein hinaus. Die weitere Reihenfolge und offene Arbeiten stehen im Arbeitsplan.
