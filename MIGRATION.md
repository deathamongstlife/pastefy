# Migration Guide: Pastefy → Pastely

This document provides comprehensive instructions for migrating from Pastefy to Pastely.

## Overview

Pastely 7.0 represents a complete rebrand of Pastefy. All internal references, package names, and database tables have been renamed from "Pastefy" to "Pastely".

## Breaking Changes

### 1. Java Package Rename
- **Old**: `de.interaapps.pastefy.*`
- **New**: `de.interaapps.pastely.*`
- **Impact**: Any custom plugins or extensions must update their imports

### 2. Database Table Prefix
- **Old**: `pastefy_*`
- **New**: `pastely_*`
- **Impact**: Requires running migration script

### 3. Environment Variables
- **Old**: `PASTEFY_*`
- **New**: `PASTELY_*`
- **Impact**: Update all environment variable names

### 4. Docker Image Name
- **Old**: `interaapps/pastefy:latest`
- **New**: `interaapps/pastely:latest`
- **Impact**: Update docker-compose.yml and Kubernetes deployments

### 5. Plugin System
- **Old**: `PastefyBackendPlugin`, `PastefyPlugin`, `PastefyPluginConfig`
- **New**: `PastelyBackendPlugin`, `PastelyPlugin`, `PastelyPluginConfig`
- **Impact**: Plugins must be updated to extend new base classes

### 6. Frontend API
- **Old**: `window.pastefy`
- **New**: `window.pastely`
- **Impact**: Frontend plugins must update their references (backward compatibility alias provided)

### 7. Custom Elements
- **Old**: `pastefy-highlighted`, `pastefy-mermaid-viewer`
- **New**: `pastely-highlighted`, `pastely-mermaid-viewer`
- **Impact**: Update any custom HTML using these elements

### 8. Elasticsearch Index
- **Old**: `pastefy_pastes`
- **New**: `pastely_pastes`
- **Impact**: Requires reindexing

## Migration Steps

### Step 1: Backup Your Data

**CRITICAL**: Before proceeding, back up:
1. Your database
2. Your `.env` configuration file
3. Any custom plugins
4. MinIO/S3 storage (if applicable)

```bash
# Database backup example
mysqldump -u root -p pastefy > pastefy_backup_$(date +%Y%m%d).sql

# Or for Docker
docker exec pastefy-db mysqldump -u pastefy -p pastefy > pastefy_backup_$(date +%Y%m%d).sql
```

### Step 2: Stop the Application

```bash
# Docker Compose
docker-compose down

# Kubernetes
kubectl scale deployment pastefy-prod --replicas=0 -n your-namespace

# Systemd
systemctl stop pastefy
```

### Step 3: Update Environment Variables

Update your `.env` file with new variable names:

```bash
# OLD → NEW
PASTEFY_LOGIN_REQUIRED → PASTELY_LOGIN_REQUIRED
PASTEFY_LOGIN_REQUIRED_CREATE → PASTELY_LOGIN_REQUIRED_CREATE
PASTEFY_LOGIN_REQUIRED_READ → PASTELY_LOGIN_REQUIRED_READ
PASTEFY_LIST_PASTES → PASTELY_LIST_PASTES
PASTEFY_PUBLIC_STATS → PASTELY_PUBLIC_STATS
PASTEFY_PUBLIC_PASTES → PASTELY_PUBLIC_PASTES
PASTEFY_META_TAGS → PASTELY_META_TAGS
PASTEFY_CUSTOM_BODY → PASTELY_CUSTOM_BODY
PASTEFY_CUSTOM_HEADER → PASTELY_CUSTOM_HEADER
PASTEFY_PAGINATION_PAGE_LIMIT → PASTELY_PAGINATION_PAGE_LIMIT
PASTEFY_DEV → PASTELY_DEV
PASTEFY_AUTOMIGRATE → PASTELY_AUTOMIGRATE
PASTEFY_GRANT_ACCESS_REQUIRED → PASTELY_GRANT_ACCESS_REQUIRED
```

### Step 4: Run Database Migration

Execute the provided SQL migration script:

```bash
# MySQL/MariaDB
mysql -u root -p pastefy < migrations/pastefy-to-pastely.sql

# Or for Docker
docker exec -i pastefy-db mysql -u pastefy -p pastefy < migrations/pastefy-to-pastely.sql
```

**Verify migration:**
```sql
SHOW TABLES LIKE 'pastely_%';
SELECT COUNT(*) FROM pastely_pastes;
SELECT COUNT(*) FROM pastely_users;
```

### Step 5: Update Docker Compose (if using Docker)

Update your `docker-compose.yml`:

```yaml
services:
  pastely:  # Changed from 'pastefy'
    image: interaapps/pastely:latest  # Changed from 'pastefy'
    # ... rest of config
```

**Note**: Database credentials (MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD) can remain as "pastefy" - they are internal and not visible to users.

### Step 6: Update Kubernetes Deployment (if using Kubernetes)

Update your Kubernetes manifests:

```yaml
metadata:
  name: pastely-prod  # Changed from 'pastefy-prod'
spec:
  selector:
    matchLabels:
      app: pastely-prod  # Changed from 'pastefy-prod'
```

Update ConfigMap name:
```yaml
envFrom:
  - configMapRef:
      name: pastely-env  # Changed from 'pastefy-env'
```

### Step 7: Migrate Elasticsearch Index (if using Elasticsearch)

Option A: Reindex in place
```bash
# Using Elasticsearch Reindex API
POST /_reindex
{
  "source": {
    "index": "pastefy_pastes"
  },
  "dest": {
    "index": "pastely_pastes"
  }
}

# Delete old index
DELETE /pastefy_pastes
```

Option B: Let auto-migration recreate the index
- Set `PASTELY_AUTOMIGRATE=true`
- The application will create the new index on startup
- Use the sync command to reindex: `java -jar pastely.jar sync-to-elastic`

### Step 8: Update Custom Plugins

If you have custom plugins, update them:

1. **Java Plugins**: Change imports from `de.interaapps.pastefy.*` to `de.interaapps.pastely.*`
2. **Extend New Base Classes**:
   ```java
   // OLD
   public class MyPlugin extends PastefyBackendPlugin {
       public MyPlugin(Pastefy pastefy) { super(pastefy); }
   }

   // NEW
   public class MyPlugin extends PastelyBackendPlugin {
       public MyPlugin(Pastely pastely) { super(pastely); }
   }
   ```

3. **Frontend Plugins**: Update from `window.pastefy` to `window.pastely`
   ```javascript
   // OLD
   window.pastefy.app

   // NEW (backward compatibility maintained)
   window.pastely.app
   // OR
   window.pastefy.app  // Still works via alias
   ```

### Step 9: Start the Application

```bash
# Docker Compose
docker-compose up -d

# Kubernetes
kubectl apply -f deployment/prod.yaml -n your-namespace

# Systemd
systemctl start pastely
```

### Step 10: Verify Migration

1. **Check application startup**: Look for "Pastely" in logs
2. **Test login**: Verify OAuth providers still work
3. **Test paste creation**: Create a new paste
4. **Test paste retrieval**: Access existing pastes
5. **Check database**: Verify all tables use `pastely_` prefix
6. **Test search**: If using Elasticsearch, verify search works

## Rollback Procedure

If you need to rollback to Pastefy:

1. Stop the Pastely application
2. Restore database backup:
   ```bash
   mysql -u root -p pastefy < pastefy_backup_YYYYMMDD.sql
   ```
3. Revert environment variables to `PASTEFY_*`
4. Use old Docker image: `interaapps/pastefy:latest`
5. Start the old application

## Backward Compatibility Notes

### Maintained
- **Frontend Plugin API**: `window.pastefy` is aliased to `window.pastely` for backward compatibility
- **Database credentials**: Internal database names/users don't need to change
- **OAuth providers**: All OAuth configurations remain unchanged

### Not Maintained
- **Java packages**: Hard rename, no backward compatibility
- **Environment variables**: Must use new `PASTELY_*` names
- **Database table prefix**: Must migrate to `pastely_*`
- **Plugin base classes**: Must extend new Pastely* classes

## Troubleshooting

### Issue: Application won't start
- **Check**: Environment variables updated to `PASTELY_*`
- **Check**: Database migration completed successfully
- **Check**: Docker image name updated to `interaapps/pastely`

### Issue: Pastes not found
- **Check**: Database tables renamed to `pastely_*` prefix
- **Check**: Run migration script again
- **Verify**: `SELECT * FROM pastely_pastes LIMIT 1;`

### Issue: Search not working
- **Check**: Elasticsearch index migrated to `pastely_pastes`
- **Solution**: Run reindex command or let auto-migration recreate

### Issue: Plugins not loading
- **Check**: Plugin extends `PastelyBackendPlugin` (not `PastefyBackendPlugin`)
- **Check**: Plugin imports use `de.interaapps.pastely.*`
- **Rebuild**: Recompile plugins with new dependencies

### Issue: OAuth login fails
- **Check**: OAuth provider configs unchanged
- **Check**: Redirect URLs updated in OAuth provider dashboards (if domain changed)
- **Verify**: `SERVER_NAME` environment variable is correct

## Support

If you encounter issues during migration:
1. Check the [GitHub Issues](https://github.com/interaapps/pastely/issues)
2. Review application logs for detailed error messages
3. Join our community Discord for real-time help
4. Create a new issue with migration logs

## Post-Migration Cleanup

After successful migration and verification:

1. **Delete old Elasticsearch index** (if migrated):
   ```bash
   DELETE /pastefy_pastes
   ```

2. **Remove old environment variables** from `.env` (keep for reference initially)

3. **Update documentation** with new Pastely URLs/references

4. **Clear Redis cache** (if using Redis):
   ```bash
   redis-cli FLUSHALL
   ```

5. **Update monitoring dashboards** with new service names

## Timeline Recommendation

- **Small deployments** (< 1000 pastes): 15-30 minutes
- **Medium deployments** (1000-10000 pastes): 30-60 minutes
- **Large deployments** (> 10000 pastes): 1-2 hours

Plan for a maintenance window with 2x the expected time as buffer.
