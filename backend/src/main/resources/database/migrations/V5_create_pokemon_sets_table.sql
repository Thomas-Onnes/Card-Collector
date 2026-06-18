CREATE TABLE IF NOT EXISTS pokemon_sets (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tcgdex_id varchar(100) UNIQUE NOT NULL,
    name varchar(200) NOT NULL,
    series varchar(200),
    release_date DATE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
