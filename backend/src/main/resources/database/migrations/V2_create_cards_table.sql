CREATE TABLE cards (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    game_type VARCHAR(200) NOT NULL,
    external_api_id TEXT,
    name VARCHAR(200) NOT NULL,
    set_code VARCHAR(200),
    collector_number VARCHAR(200),
    rarity VARCHAR(200) NOT NULL,
    image_url TEXT,
    price DECIMAL(10,2),
    currency VARCHAR(200) NOT NULL,
    raw_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)