-- V5 — Add original_file_name to portfolio_items for duplicate-upload detection
ALTER TABLE portfolio_items
    ADD COLUMN original_file_name VARCHAR(255);

-- Partial unique index: only enforce uniqueness for rows that have a filename stored.
-- Existing rows and any future rows with NULL are unaffected, so the migration is safe
-- on a live table that already has data.
CREATE UNIQUE INDEX uq_portfolio_original_file
    ON portfolio_items (photographer_profile_id, original_file_name, size_bytes)
    WHERE original_file_name IS NOT NULL;
