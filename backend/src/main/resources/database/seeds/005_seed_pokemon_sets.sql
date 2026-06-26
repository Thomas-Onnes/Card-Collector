INSERT INTO pokemon_sets (
    id,
    tcgdex_id,
    name,
    series,
    release_date
)
OVERRIDING SYSTEM VALUE
VALUES
    (501, 'sv3pt5', 'Scarlet & Violet 151', 'Scarlet & Violet', '2023-09-22'),
    (502, 'base1', 'Base Set', 'Base', '1999-01-09'),
    (503, 'sv2', 'Paldea Evolved', 'Scarlet & Violet', '2023-06-09')
ON CONFLICT (tcgdex_id)
DO NOTHING;
