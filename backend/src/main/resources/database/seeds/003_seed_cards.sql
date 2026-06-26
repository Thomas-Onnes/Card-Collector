INSERT INTO cards (
    id,
    game_type,
    external_api_id,
    name,
    image_url
)
OVERRIDING SYSTEM VALUE
VALUES
    (1001, 'pokemon', 'dummy-pokemon-sv151-charizard', 'Charizard', 'https://example.com/charizard.png'),
    (1002, 'pokemon', 'dummy-pokemon-sv151-pikachu', 'Pikachu', 'https://example.com/pikachu.png'),
    (1003, 'pokemon', 'dummy-pokemon-sv151-mew', 'Mew ex', 'https://example.com/mew.png'),
    (1004, 'pokemon', 'dummy-pokemon-base-charizard', 'Charizard', 'https://example.com/base-charizard.png'),
    (1005, 'pokemon', 'dummy-pokemon-base-blastoise', 'Blastoise', 'https://example.com/blastoise.png'),
    (1006, 'pokemon', 'dummy-pokemon-base-venusaur', 'Venusaur', 'https://example.com/venusaur.png'),
    (1007, 'pokemon', 'dummy-pokemon-paldea-quaquaval', 'Quaquaval ex', 'https://example.com/quaquaval.png'),
    (1008, 'pokemon', 'dummy-pokemon-paldea-tinkaton', 'Tinkaton ex', 'https://example.com/tinkaton.png'),

    (2001, 'mtg', 'dummy-mtg-ltr-lightning-bolt', 'Lightning Bolt', 'https://example.com/lightning-bolt.png'),
    (2002, 'mtg', 'dummy-mtg-m10-lightning-bolt', 'Lightning Bolt', 'https://example.com/lightning-bolt-m10.png'),
    (2003, 'mtg', 'dummy-mtg-ltr-llanowar-elves', 'Llanowar Elves', 'https://example.com/llanowar-elves.png'),
    (2004, 'mtg', 'dummy-mtg-dom-llanowar-elves', 'Llanowar Elves', 'https://example.com/llanowar-elves-dom.png'),
    (2005, 'mtg', 'dummy-mtg-dmu-sheoldred', 'Sheoldred, the Apocalypse', 'https://example.com/sheoldred.png'),
    (2006, 'mtg', 'dummy-mtg-znr-omnath', 'Omnath, Locus of Creation', 'https://example.com/omnath.png'),
    (2007, 'mtg', 'dummy-mtg-ltr-sol-ring', 'Sol Ring', 'https://example.com/sol-ring.png'),
    (2008, 'mtg', 'dummy-mtg-cmm-sol-ring', 'Sol Ring', 'https://example.com/sol-ring-cmm.png')
ON CONFLICT (id)
DO NOTHING;
