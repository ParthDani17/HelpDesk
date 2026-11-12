UPDATE refresh_tokens
SET session_id = gen_random_uuid()
WHERE session_id IS NULL;

ALTER TABLE refresh_tokens
    ALTER COLUMN session_id SET NOT NULL;