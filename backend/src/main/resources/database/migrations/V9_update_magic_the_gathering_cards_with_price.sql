ALTER TABLE magic_the_gathering_cards
ADD COLUMN IF NOT EXISTS price_eur NUMERIC(10, 2),
ADD COLUMN IF NOT EXISTS price_updated_at TIMESTAMP;