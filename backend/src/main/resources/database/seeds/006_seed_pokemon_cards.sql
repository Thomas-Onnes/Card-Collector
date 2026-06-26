INSERT INTO pokemon_cards (
    card_id,
    set_id,
    hp,
    rarity,
    types,
    evolves_from,
    collector_number,
    artist,
    price_eur,
    raw_json
)
VALUES
    (1001, 501, 180, 'RARE', 'Fire', 'Charmeleon', '006', 'Mitsuhiro Arita', 19.99, '{}'),
    (1002, 501, 60, 'COMMON', 'Lightning', NULL, '025', 'Atsuko Nishida', 2.49, '{}'),
    (1003, 501, 180, 'ULTRA_RARE', 'Psychic', NULL, '151', '5ban Graphics', 14.95, '{}'),
    (1004, 502, 120, 'RARE', 'Fire', 'Charmeleon', '004', 'Mitsuhiro Arita', 249.99, '{}'),
    (1005, 502, 100, 'RARE', 'Water', 'Wartortle', '002', 'Ken Sugimori', 89.95, '{}'),
    (1006, 502, 100, 'RARE', 'Grass', 'Ivysaur', '015', 'Mitsuhiro Arita', 79.95, '{}'),
    (1007, 503, 320, 'ULTRA_RARE', 'Water', 'Quaxwell', '052', '5ban Graphics', 4.25, '{}'),
    (1008, 503, 300, 'ULTRA_RARE', 'Psychic', 'Tinkatuff', '095', 'PLANETA Mochizuki', 6.75, '{}')
ON CONFLICT (set_id, collector_number)
DO NOTHING;
