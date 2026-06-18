INSERT INTO user_collections (
    user_id,
    collection_name,
    game_type
) VALUES (
    1,
    'Test Collection',
    'pokemon'
         )
    ON CONFLICT DO NOTHING;