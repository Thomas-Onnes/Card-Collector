INSERT INTO users(
    username,
    email,
    password_hash
) VALUES (
'test',
'test@gmail.com',
'test123'
)
ON CONFLICT (email)
DO NOTHING;