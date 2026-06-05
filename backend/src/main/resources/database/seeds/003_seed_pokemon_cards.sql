INSERT INTO pokemon_cards(
    card_id,
    hp,
    rarity,
    types,
    evolves_from,
    set_name,
    set_code,
    collector_number,
    artist
) values (
1,
120,
'Rare',
'Fire',
'Charmeleon',
'Scarlet & Voilet 151',
151,
'006',
'Mitsuhiro Arita'
)
ON CONFLICT (set_code, collector_number)
DO NOTHING;