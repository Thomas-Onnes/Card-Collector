CREATE TABLE IF NOT EXISTS pokemon_cards (
    card_id INTEGER PRIMARY KEY,
    set_id integer NOT NULL,
    hp integer,
    rarity varchar(200) NOT NULL,
    types varchar(200) NOT NULL,
    evolves_from varchar(200),
    collector_number varchar(200) NOT NULL,
    artist varchar(200),
    raw_json TEXT,
    UNIQUE(set_id, collector_number),

    FOREIGN KEY (card_id) REFERENCES cards(id),
    FOREIGN KEY (set_id) REFERENCES pokemon_sets(id)
)