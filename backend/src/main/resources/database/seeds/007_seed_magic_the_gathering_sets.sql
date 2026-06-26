INSERT INTO magic_the_gathering_sets (
    scryfall_id,
    code,
    name
)
VALUES
    ('dummy-set-ltr', 'LTR', 'The Lord of the Rings: Tales of Middle-earth'),
    ('dummy-set-m10', 'M10', 'Magic 2010'),
    ('dummy-set-dom', 'DOM', 'Dominaria')
ON CONFLICT (code)
DO NOTHING;