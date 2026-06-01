INSERT INTO cards ( game_type, external_api_id, name, set_code,
                   collector_number, rarity, image_url, price, currency, raw_json)
VALUES (
    'Pokemon',
    'tcgdex',
    'Charizard',
    '151',
    3,
    'RARE',
    'filler',
    100.00,
    'EUR',
    'json'
        )
ON CONFLICT (game_type, external_api_id)
DO NOTHING;