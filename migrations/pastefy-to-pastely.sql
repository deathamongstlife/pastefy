-- Pastefy to Pastely Database Migration Script
-- This script renames all database tables from pastefy_ prefix to pastely_ prefix
-- Run this script BEFORE starting the Pastely application with the new codebase
-- IMPORTANT: Backup your database before running this migration!

-- Rename core tables
RENAME TABLE pastefy_pastes TO pastely_pastes;
RENAME TABLE pastefy_users TO pastely_users;
RENAME TABLE pastefy_folders TO pastely_folders;
RENAME TABLE pastefy_auth_keys TO pastely_auth_keys;

-- Rename paste-related tables
RENAME TABLE pastefy_paste_stars TO pastely_paste_stars;
RENAME TABLE pastefy_paste_tags TO pastely_paste_tags;
RENAME TABLE pastefy_shared_pastes TO pastely_shared_pastes;
RENAME TABLE pastefy_public_paste_engagement TO pastely_public_paste_engagement;

-- Rename tag and notification tables
RENAME TABLE pastefy_tag_listing TO pastely_tag_listing;
RENAME TABLE pastefy_notifications TO pastely_notifications;

-- Rename user-related tables
RENAME TABLE pastefy_login_tokens TO pastely_login_tokens;
RENAME TABLE pastefy_oauth_accounts TO pastely_oauth_accounts;

-- If you have additional custom tables with pastefy_ prefix, add them here:
-- RENAME TABLE pastefy_custom_table TO pastely_custom_table;

-- Update Elasticsearch index name (if using Elasticsearch)
-- You will need to manually reindex or rename the Elasticsearch index from:
-- pastefy_pastes -> pastely_pastes

-- Update Redis keys (if using Redis)
-- Redis keys with "pastefy:" prefix will need to be migrated or allowed to expire naturally

-- Verification queries (run these after migration to confirm):
-- SHOW TABLES LIKE 'pastely_%';
-- SELECT COUNT(*) FROM pastely_pastes;
-- SELECT COUNT(*) FROM pastely_users;
