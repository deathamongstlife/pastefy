-- ============================================================================
-- Pastefy to Pastely Database Migration Script
-- ============================================================================
-- This script renames all database tables from pastefy_ to pastely_ prefix
--
-- WARNING: This is a DESTRUCTIVE operation. Backup your database first!
--
-- Usage:
--   MySQL:  mysql -u username -p database_name < pastefy_to_pastely.sql
--   SQLite: sqlite3 database.db < pastefy_to_pastely.sql
--
-- ============================================================================

-- Core tables
RENAME TABLE pastefy_pastes TO pastely_pastes;
RENAME TABLE pastefy_users TO pastely_users;
RENAME TABLE pastefy_folders TO pastely_folders;
RENAME TABLE pastefy_auth_keys TO pastely_auth_keys;

-- Social features
RENAME TABLE pastefy_paste_stars TO pastely_paste_stars;
RENAME TABLE pastefy_paste_tags TO pastely_paste_tags;
RENAME TABLE pastefy_shared_pastes TO pastely_shared_pastes;
RENAME TABLE pastefy_notifications TO pastely_notifications;

-- Analytics & engagement
RENAME TABLE pastefy_public_paste_engagement TO pastely_public_paste_engagement;
RENAME TABLE pastefy_tag_listing TO pastely_tag_listing;

-- Version control (7.0 features)
RENAME TABLE pastefy_paste_versions TO pastely_paste_versions;
RENAME TABLE pastefy_paste_branches TO pastely_paste_branches;

-- Collaboration features
RENAME TABLE pastefy_paste_collaborators TO pastely_paste_collaborators;
RENAME TABLE pastefy_paste_comments TO pastely_paste_comments;

-- User features
RENAME TABLE pastefy_user_followers TO pastely_user_followers;
RENAME TABLE pastefy_user_activity TO pastely_user_activity;

-- Organization features
RENAME TABLE pastefy_collections TO pastely_collections;
RENAME TABLE pastefy_collection_items TO pastely_collection_items;

-- Security & access control
RENAME TABLE pastefy_paste_access_logs TO pastely_paste_access_logs;
RENAME TABLE pastefy_paste_ip_filters TO pastely_paste_ip_filters;

-- Templates
RENAME TABLE pastefy_paste_templates TO pastely_paste_templates;
RENAME TABLE pastefy_template_categories TO pastely_template_categories;

-- Integrations
RENAME TABLE pastefy_webhooks TO pastely_webhooks;
RENAME TABLE pastefy_webhook_logs TO pastely_webhook_logs;

-- Media & attachments
RENAME TABLE pastefy_paste_attachments TO pastely_paste_attachments;

-- ============================================================================
-- Migration complete!
--
-- Next steps:
-- 1. Update environment variables: PASTEFY_* -> PASTELY_*
-- 2. Update config keys in application config
-- 3. Restart the application
-- 4. If using Elasticsearch, reindex with new index name: pastely_pastes
-- ============================================================================
