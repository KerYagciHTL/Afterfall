User: Lies dir im spec Ordner und im CLAUDE.md alles durch und implementiere ein basic frontend und backend Ordner. Es muss nichts großes sein einfach ein initial commit. Backend soll basic dockerfile haben. Bei fragen gerne Fragen!

---

User: Lies spec/ alles durch. Unser Chat wird bitte in spec/chats/YAGCI-CHAT.md dokumentiert. Wir machen nur das backend. Benutze die gh cli und lese issue #5. Frage mich umbedingt nach fragen bevor du beginnst zu implementieren. Das backend ist eh was kleines daher kannst du nach dem fragen direkt mit dem ganzen implementieren und dockerizen beginnen.

Fragen gestellt:
1. Token → env var `RANKING_TOKEN`, Wert beim `docker compose up` gesetzt
2. SQLite → Volume in Docker (`/data/ranking.db`)
3. Port → 8080 OK
4. Server-URL → echte Server-IP im Java-Client

Implementiert (Issue #5):
- `backend/pom.xml` — `spring-boot-starter-jdbc` + `sqlite-jdbc 3.45.3.0` hinzugefügt
- `backend/src/main/resources/application.properties` — SQLite Datasource: `${DB_PATH:ranking.db}` (lokal: `ranking.db`, Docker: `/data/ranking.db`)
- `backend/src/main/java/at/htl/afterfall/db/DatabaseInitializer.java` — `players`-Tabelle per `@PostConstruct` anlegen
- `backend/src/main/java/at/htl/afterfall/controller/RankingController.java` — GET /ranking (Top-100, ROW_NUMBER), POST /ranking (Token-Auth, Upsert, Rang zurück)
- `backend/docker-compose.yml` — Volume `ranking-data:/data`, env `RANKING_TOKEN` + `DB_PATH=/data/ranking.db`
- `backend/.env` — shared secret (nicht committen, in .gitignore)

Getestet (alle Akzeptanzkriterien erfüllt):
- POST kxrim → `{"rank":1}` ✓
- POST fridolino → `{"rank":2}` ✓
- POST kxrim upsert (höherer Wert) → `{"rank":1}` ✓
- GET ranking → korrekte Reihenfolge ✓
- POST falsches Token → HTTP 401 ✓

Hinweis lokal: `RANKING_TOKEN=$(grep RANKING_TOKEN .env | cut -d= -f2) mvn spring-boot:run`
Docker: `docker compose up --build` (.env wird auto gelesen)
