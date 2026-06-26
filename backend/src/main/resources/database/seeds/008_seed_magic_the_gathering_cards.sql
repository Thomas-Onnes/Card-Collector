INSERT INTO magic_the_gathering_cards (
    card_id,
    scryfall_id,
    name,
    set_code,
    set_name,
    rarity,
    mana_cost,
    type_line,
    illustrator,
    price_eur,
    is_creature,
    is_instant,
    is_sorcery,
    is_enchantment,
    is_artifact,
    is_land,
    is_planeswalker,
    is_legendary,
    is_saga,
    is_room,
    is_battle,
    is_kindred
)
VALUES
    (2001, 'dummy-scryfall-ltr-lightning-bolt', 'Lightning Bolt', 'LTR', 'The Lord of the Rings: Tales of Middle-earth', 'UNCOMMON', '{R}', 'Instant', 'Christopher Rush', 1.25, false, true, false, false, false, false, false, false, false, false, false, false),
    (2002, 'dummy-scryfall-m10-lightning-bolt', 'Lightning Bolt', 'M10', 'Magic 2010', 'COMMON', '{R}', 'Instant', 'Christopher Moeller', 0.80, false, true, false, false, false, false, false, false, false, false, false, false),
    (2003, 'dummy-scryfall-ltr-llanowar-elves', 'Llanowar Elves', 'LTR', 'The Lord of the Rings: Tales of Middle-earth', 'COMMON', '{G}', 'Creature — Elf Druid', 'Viko Menezes', 0.40, true, false, false, false, false, false, false, false, false, false, false, false),
    (2004, 'dummy-scryfall-dom-llanowar-elves', 'Llanowar Elves', 'DOM', 'Dominaria', 'COMMON', '{G}', 'Creature — Elf Druid', 'Chris Rahn', 0.35, true, false, false, false, false, false, false, false, false, false, false, false),
    (2005, 'dummy-scryfall-dmu-sheoldred', 'Sheoldred, the Apocalypse', 'DMU', 'Dominaria United', 'MYTHIC', '{2}{B}{B}', 'Legendary Creature — Phyrexian Praetor', 'Chris Rahn', 64.95, true, false, false, false, false, false, false, true, false, false, false, false),
    (2006, 'dummy-scryfall-znr-omnath', 'Omnath, Locus of Creation', 'ZNR', 'Zendikar Rising', 'MYTHIC', '{R}{G}{W}{U}', 'Legendary Creature — Elemental', 'Chris Rahn', 7.50, true, false, false, false, false, false, false, true, false, false, false, false),
    (2007, 'dummy-scryfall-ltr-sol-ring', 'Sol Ring', 'LTR', 'The Lord of the Rings: Tales of Middle-earth', 'UNCOMMON', '{1}', 'Artifact', 'Mark Tedin', 1.99, false, false, false, false, true, false, false, false, false, false, false, false),
    (2008, 'dummy-scryfall-cmm-sol-ring', 'Sol Ring', 'CMM', 'Commander Masters', 'UNCOMMON', '{1}', 'Artifact', 'Lie Setiawan', 2.25, false, false, false, false, true, false, false, false, false, false, false, false)
ON CONFLICT (scryfall_id)
DO NOTHING;
