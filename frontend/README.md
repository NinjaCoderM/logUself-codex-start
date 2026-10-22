# logUself Frontend

Angular-Oberfläche für den ersten lokalen Meilenstein: Tickets und Sporterfassungen.

Die vollständige Start- und Prüfanleitung steht in [README.md](../README.md), die Planung in [TODO.md](../doc_logUself/TODO.md).

```powershell
npm.cmd ci
npm.cmd start
npm.cmd run build
npm.cmd run test:e2e
```

Frontend: http://127.0.0.1:4200. Backend: http://127.0.0.1:8080 (Entwicklungsproxy für `/api`).

Die Browsertests benötigen laufendes Frontend, Backend, PostgreSQL und lokal installiertes Microsoft Edge. Sie hinterlassen Browsercheck-Tickets mit Testdaten. Anmeldung, Berechtigungsoberfläche und vollständige Historie folgen später.
