CREATE TABLE IF NOT EXISTS pokemon_cards (
    card_id INTEGER PRIMARY KEY,
    set_id INTEGER NOT NULL,
    hp INTEGER,
    rarity VARCHAR(200) NOT NULL,
    types VARCHAR(200) NOT NULL,
    evolves_from VARCHAR(200),
    collector_number VARCHAR(200) NOT NULL,
    artist VARCHAR(200),
    price_eur NUMERIC(10, 2),
    raw_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(set_id, collector_number),

    FOREIGN KEY (card_id) REFERENCES cards(id),
    FOREIGN KEY (set_id) REFERENCES pokemon_sets(id)
);
