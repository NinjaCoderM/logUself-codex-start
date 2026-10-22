# logUself-codex-start
A personal and professional task management and time tracking application that helps users organize activities, record time spent on tasks, and analyze how they allocate their time.

logUself verwaltet Projekte, Tickets mit dynamischen Eintragstypen und strukturierte Erfassungen für persönliche, familiäre und betriebliche Nutzung.

## Projektstand

Die Anforderungen und der erste Umsetzungsschritt sind dokumentiert. Backend, Frontend und Datenbankumgebung sind noch nicht implementiert. Git ist bereits initialisiert.

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

Für die Umsetzung werden ein passendes Java-JDK, Node.js/npm und Docker Desktop benötigt. Die konkreten Versionen werden bei der Umgebungsprüfung festgelegt und hier dokumentiert. Eine vorhandene PostgreSQL-Instanz oder deren Zugangsdaten sind bei Verwendung der geplanten lokalen Docker-Datenbank nicht erforderlich.

Start-, Stopp- und Prüfkommandos werden ergänzt, sobald die jeweiligen Komponenten eingerichtet und die Kommandos überprüft sind. Derzeit gibt es noch keine startbare Anwendung.

Lokale Datenbankzugangsdaten gehören in eine nicht versionierte Konfiguration; eine Vorlage kann Platzhalter enthalten. Bis zur Umsetzung von Anmeldung und Zugriffsschutz wird der Prototyp ausschließlich lokal mit Testdaten betrieben.

## Prüfung von M1

1. Ticket über die Oberfläche erstellen und wieder laden.
2. Zwei Sporterfassungen mit 120 und 30 Minuten speichern; die Summe beträgt 2 Stunden 30 Minuten.
3. Oberfläche neu laden und Backend neu starten; Tickets, Erfassungen und Summe bleiben erhalten.
4. Dauer 0 und negative Dauer werden serverseitig abgewiesen und mit verständlicher Fehlermeldung angezeigt.

Die vollständigen Anforderungen für Version 1 gehen über diesen ersten Meilenstein hinaus. Die weitere Reihenfolge und offene Arbeiten stehen im Arbeitsplan.
