CREATE TABLE pokemon_cards (
    card_id INTEGER PRIMARY KEY,
    hp integer,
    rarity varchar(200) NOT NULL,
    types varchar(200) NOT NULL,
    evolves_from varchar(200),
    set_name varchar(200) NOT NULL,
    set_code varchar(200) NOT NULL,
    collector_number varchar(200) NOT NULL,
    artist varchar(200),

    UNIQUE(set_code, collector_number),

    FOREIGN KEY (card_id) REFERENCES cards(id)
)