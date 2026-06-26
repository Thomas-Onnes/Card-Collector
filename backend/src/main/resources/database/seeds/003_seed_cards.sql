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

    (2001, 'mtg', 'dummy-mtg-ltr-lightning-bolt', 'Lightning Bolt', 'https://example.com/lightning-bolt.png'),
    (2002, 'mtg', 'dummy-mtg-m10-lightning-bolt', 'Lightning Bolt', 'https://example.com/lightning-bolt-m10.png'),
    (2003, 'mtg', 'dummy-mtg-ltr-llanowar-elves', 'Llanowar Elves', 'https://example.com/llanowar-elves.png'),
    (2004, 'mtg', 'dummy-mtg-dom-llanowar-elves', 'Llanowar Elves', 'https://example.com/llanowar-elves-dom.png')
ON CONFLICT (id)
DO NOTHING;