INSERT INTO user_cards(
            user_id, card_id, quantity, card_condition, is_foil, language
) VALUES (
          1,
          1,
          1,
          'clean',
          TRUE,
          'English'
         )
ON CONFLICT (user_id, card_id)
DO NOTHING;