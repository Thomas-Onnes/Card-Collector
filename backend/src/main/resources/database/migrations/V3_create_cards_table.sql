CREATE TABLE IF NOT EXISTS cards (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    game_type VARCHAR(200) NOT NULL,
    external_api_id TEXT,
    name VARCHAR(200) NOT NULL,
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(game_type, external_api_id)
);
