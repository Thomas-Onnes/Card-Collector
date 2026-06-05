INSERT INTO cards (
    game_type,
    external_api_id,
    name,
    image_url,
    raw_json
) VALUES (
'Pokemon',
'tcgdex',
'Charizard',
'filler',
'json'
)
ON CONFLICT (game_type, external_api_id)
DO NOTHING;