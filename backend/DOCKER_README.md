# Docker gebruiken

Ga naar de backend-map:

```bash
cd backend
```

Start de backend en de PostgreSQL database:

```bash
docker compose up --build
```

De backend draait daarna op:

```text
http://localhost:8080
```

De backend-container verbindt met PostgreSQL via de Docker service-naam `database`:

```text
jdbc:postgresql://database:5432/card_collector
```

Dat is bewust niet `localhost`, want binnen een Docker-container betekent `localhost` de container zelf.

Database openen:

```bash
docker compose exec database psql -U admin -d card_collector
```

Alles stoppen:

```bash
docker compose down
```

Alles stoppen én de database-data verwijderen:

```bash
docker compose down -v
```
