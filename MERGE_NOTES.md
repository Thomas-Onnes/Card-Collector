# Card Collector merged version

This version keeps the Android Studio frontend, login/register/session logic, collections logic and Docker/PostgreSQL setup from the Android project. The external API logic from the groupmate project has been integrated into the backend.

## What was merged

- Scryfall API client and DTOs for Magic: The Gathering.
- TCGdex API client and DTOs for Pokémon.
- Mappers for converting external API responses into database models.
- Database repositories for Pokémon sets/cards and MTG sets/cards.
- Import endpoints in the existing `/cards` backend handler.
- Updated migrations to support Pokémon sets, card prices and MTG prices.
- Updated seeders with more dummy Pokémon and MTG cards.

## Important endpoints

All endpoints require `Authorization: Bearer <token>`.

### Search cards already in the database

```http
GET /cards/search?gameType=pokemon&set=sv3pt5&collectorNumber=006
GET /cards/search?gameType=mtg&name=lightning
```

### Import cards from external APIs

```http
POST /cards/import/pokemon?set=sv3pt5
POST /cards/import/mtg?set=ltr
POST /cards/update-prices/mtg
```

The Android app still searches the local database. Use the import endpoints to fill the database from the APIs.

## Run

```powershell
cd backend
docker compose down -v
docker compose up --build
```

Open `android-app` in Android Studio and run the app on an emulator.

## Notes

- Users are not seeded. Create users through the Android app so passwords are hashed correctly.
- `SEED_DATABASE` is enabled in docker-compose so the dummy card data is inserted.
- For production, keep `SEED_DATABASE=false`, use HTTPS, and do not expose the database port publicly.
