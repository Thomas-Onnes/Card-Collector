INSERT INTO pokemon_cards(
    card_id,
    set_id,
    hp,
    rarity,
    types,
    evolves_from,
    collector_number,
    artist,
    raw_json
) values (
1,
1,
120,
'RARE',
'Fire',
'Charmeleon',
'006',
'Mitsuhiro Arita',
'json'
)
ON CONFLICT (set_id, collector_number)
DO NOTHING;