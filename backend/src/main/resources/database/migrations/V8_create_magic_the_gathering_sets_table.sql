CREATE TABLE IF NOT EXISTS magic_the_gathering_sets (
    id SERIAL PRIMARY KEY,
    scryfall_id TEXT UNIQUE NOT NULL,
    code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL
);