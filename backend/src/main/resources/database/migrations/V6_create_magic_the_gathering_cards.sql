CREATE TABLE magic_the_gathering_cards (
    card_id INTEGER PRIMARY KEY,

    scryfall_id VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    set_code VARCHAR(50) NOT NULL,
    set_name VARCHAR(200) NOT NULL,
    rarity VARCHAR(50) NOT NULL,
    mana_cost VARCHAR(200),
    type_line VARCHAR(200),
    illustrator VARCHAR(200),

    is_creature BOOLEAN NOT NULL,
    is_instant BOOLEAN NOT NULL,
    is_sorcery BOOLEAN NOT NULL,
    is_enchantment BOOLEAN NOT NULL,
    is_artifact BOOLEAN NOT NULL,
    is_land BOOLEAN NOT NULL,
    is_planeswalker BOOLEAN NOT NULL,
    is_legendary BOOLEAN NOT NULL,
    is_saga BOOLEAN NOT NULL,
    is_room BOOLEAN NOT NULL,
    is_battle BOOLEAN NOT NULL,
    is_kindred BOOLEAN NOT NULL,

    FOREIGN KEY (card_id) REFERENCES cards(id)
);