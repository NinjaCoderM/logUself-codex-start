# Arbeitsplan logUself

Diese Datei enthält die Umsetzungsschritte und ihren Erledigungsstand. Die fachlichen Anforderungen stehen in [spec.me](spec.me), die Start- und Prüfanleitung in [README.md](../README.md). Die bisherige Phasenplanung bleibt als Roadmap erhalten; M1 ist der erste kleine Durchstich durch Grundgerüst, Tickets und Zeiterfassung.

## Nächster Meilenstein M1 – Ticket und Sporterfassung dauerhaft speichern

Ziel: Angular im Browser → REST-API mit Spring Boot/Kotlin → PostgreSQL.
Zeitbudget für die erste Sitzung: etwa 2 Stunden, abhängig von Einrichtung und vorhandenen Werkzeugen.
Git ist bereits initialisiert. Die folgenden Umsetzungspunkte sind noch offen.

Bereits geprüft:

- [x] Docker Desktop und Docker Engine über die CLI erreicht.
- [x] Compose-Verfügbarkeit geklärt: `docker-compose` funktioniert (v2.29.2-desktop.2), `docker compose` wird derzeit nicht erkannt. Keine Neuinstallation erforderlich.

Umgebung geprüft: Node.js 24.18.0, npm 11.16.0, JDK 21.0.4 vorhanden (PATH derzeit Java 23). Für das Backend ist JDK 21 vorgesehen; Framework-Versionen folgen bei der Projekterstellung. PostgreSQL 17.11 läuft lokal auf 127.0.0.1:5433; Passwortanmeldung, Healthcheck und Datenerhaltung nach Container-Neuerstellung sind geprüft. Testdaten wurden entfernt.

- [x] Entwicklungsumgebung prüfen: Java, Node/npm, Docker Desktop und vorhandene Projektdateien; JDK 21 als Backend-Basis vorgesehen.
- [x] Lokale PostgreSQL-Entwicklungsdatenbank über Docker Compose mit persistentem Volume einrichten; lokale Zugangsdaten in ausgeschlossener .env, Vorlage in .env.example.
- [ ] Spring-Boot-Backend mit Kotlin, Datenbankanbindung und versionierten Migrationen einrichten.
- [ ] Minimales Modell für Projekt, Eintragstyp, Ticket und Erfassung gemäß spec.me anlegen; PrivatMartin und Sport per Migration vorbelegen.
- [ ] REST-Endpunkte zum Erstellen und Laden von Tickets sowie Speichern und Auflisten ihrer Sporterfassungen bereitstellen; positive Dauer serverseitig prüfen.
- [ ] Angular mit TypeScript im Browser einrichten und API anbinden: Ticket anlegen/auswählen, Sporterfassung mit Datum, Sportart, Dauer und optionaler Beschreibung speichern.
- [ ] Erfassungen und Dauer-Summe am Ticket anzeigen.
- [ ] Prüfen: 120 + 30 Minuten ergeben 2 Stunden 30 Minuten; Neuladen und Backend-Neustart erhalten die Daten; 0 und negative Dauer werden ohne Speicherung abgewiesen.
- [ ] Aussagekräftigen Backend-Integrationstest für Speicherung, Zuordnung und ungültige Dauer ergänzen; sichtbaren Ablauf im Browser prüfen.
- [ ] Tatsächliche Voraussetzungen, Start-, Stopp- und Prüfkommandos in README.md dokumentieren; Erledigungsstand hier aktualisieren.

Zwischenziel bei Einrichtungsaufwand: Ticket über Angular speichern und nach Backend-Neustart wieder laden. Dies ist noch nicht die vollständige M1-Abnahme.
Für M1 zurückgestellt: Electron, Typeditor, Anmeldung, Familienfreigaben, vollständige Historie, Kalender, Wochenbericht und externe Integrationen. Bis zum Zugriffsschutz nur lokale Testdaten verwenden.

## Weitere Roadmap

### Phase 0 – Anforderungen und Architektur

Spec prüfen, offene Fragen klären, Architektur bestätigen und Implementierungsplan festlegen.

### Phase 1 – Lauffähiges Grundgerüst

Zunächst starten Kotlin-Backend, PostgreSQL und Angular im Browser. Electron wird nach dem ersten testbaren Browser-Ablauf ergänzt; die Weboberfläche bleibt gemeinsam nutzbar.

### Phase 2 – Tickets und Datenbank

Datenbankmigrationen, Zugriffsschutz, Ticketverwaltung, Statuswechsel und persistente Speicherung.

### Phase 3 – Zeit, Sport und Ernährung

Formulare und Validierung, Zeitbuchungen sowie Tages- und Projektsummen.

### Phase 4 – Kalender und Wochenbericht

Wochenübersicht, Auswertungen, abgeschlossene Tickets und Vergleich vergangener Wochen.

### Phase 5 – Integration und Qualitätssicherung

iPhone-Bedienung, gemeinsame Datennutzung, Electron-Paketierung und End-to-End-Tests.
