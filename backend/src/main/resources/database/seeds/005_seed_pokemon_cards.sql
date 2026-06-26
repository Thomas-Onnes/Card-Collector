INSERT INTO pokemon_cards (
    card_id,
    hp,
    rarity,
    types,
    evolves_from,
    set_name,
    set_code,
    collector_number,
    artist,
    raw_json,
    price
)
VALUES
    (
        1001,
        180,
        'Rare Holo',
        'Fire',
        'Charmeleon',
        'Scarlet & Violet 151',
        'SV151',
        '006',
        'Mitsuhiro Arita',
        '{}',
        19.99
    ),
    (
        1002,
        60,
        'Common',
        'Lightning',
        NULL,
        'Scarlet & Violet 151',
        'SV151',
        '025',
        'Atsuko Nishida',
        '{}',
        2.49
    ),
    (
        1003,
        180,
        'Ultra Rare',
        'Psychic',
        NULL,
        'Scarlet & Violet 151',
        'SV151',
        '151',
        '5ban Graphics',
        '{}',
        14.95
    ),
    (
        1004,
        120,
        'Rare Holo',
        'Fire',
        'Charmeleon',
        'Base Set',
        'BS',
        '004',
        'Mitsuhiro Arita',
        '{}',
        249.99
    )
ON CONFLICT
DO NOTHING;