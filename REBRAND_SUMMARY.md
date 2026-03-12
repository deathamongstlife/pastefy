# Pastefy → Pastely Rebrand Summary

## Overview
Comprehensive rebrand from "Pastefy" to "Pastely" completed on 2026-03-12. This document summarizes all changes made across the entire codebase.

## Phase 1: Backend Package Rename ✅

### Java Package Structure
- **Directory**: Renamed `backend/src/main/java/de/interaapps/pastefy/` → `backend/src/main/java/de/interaapps/pastely/`
- **Package declarations**: Updated all 125 Java files from `package de.interaapps.pastefy.*` to `package de.interaapps.pastely.*`
- **Import statements**: Updated all 434 imports from `import de.interaapps.pastefy.*` to `import de.interaapps.pastely.*`

### Main Singleton Class
- **File**: `Pastefy.java` → `Pastely.java`
- **Class name**: `public class Pastefy` → `public class Pastely`
- **Instance variable**: `private static Pastefy instance` → `private static Pastely instance`
- **getInstance() method**: Return type updated to `Pastely`
- **Constructor**: `public Pastefy()` → `public Pastely()`

### Configuration Changes
- **Table prefix**: Changed from `pastefy_` to `pastely_` (line 300)
- **Environment variables**: All `PASTEFY_*` → `PASTELY_*` mappings (lines 175-188)
- **Config keys**: All `pastefy.*` → `pastely.*` internal keys
- **Elasticsearch index**: `pastefy_pastes` → `pastely_pastes`

### CLI Classes
- **File**: `cli/PastefyCLI.java` → `cli/PastelyCLI.java`
- **Class name**: `PastefyCLI` → `PastelyCLI`
- **CLI command**: `name = "pastefy"` → `name = "pastely"`
- **Description**: Updated CLI metadata

### Plugin System
- **Files renamed**:
  - `PastefyPlugin.java` → `PastelyPlugin.java`
  - `PastefyPluginConfig.java` → `PastelyPluginConfig.java`
  - `PastefyBackendPlugin.java` → `PastelyBackendPlugin.java`
- **Class names**: All plugin classes updated
- **Constructor signatures**: `PastefyBackendPlugin(Pastefy pastefy)` → `PastelyBackendPlugin(Pastely pastely)`

### Build Configuration
- **pom.xml**:
  - Line 7: GroupId already correct (`de.interaapps.pastely`)
  - Line 180: `<mainClass>de.interaapps.pastely.Pastely</mainClass>`

### HTTP Headers
- **Server header**: `InteraApps-Pastefy` → `InteraApps-Pastely` (line 443)

### Plugin Registration
- **HTML placeholder**: `/*PASTEFY_PLUGINS*/` → `/*PASTELY_PLUGINS*/`

### Static References
- **Total replacements**: 82+ references to `Pastefy.getInstance()` → `Pastely.getInstance()`

## Phase 2: Frontend Updates ✅

### main.ts
- **Window API**: `window.pastefy` → `window.pastely` (line 134)
- **Plugin registration**: `registerPastefyPlugin` → `registerPastelyPlugin` (lines 146-150)
- **Interface definition**: Updated global Window interface type
- **Backward compatibility**: Alias `window.pastefy = window.pastely` added by user

### Custom Elements
- **Element name**: `pastefy-highlighted` → `pastely-highlighted` (line 89-91)

### index.html
- **Title**: `<title>pastefy</title>` → `<title>Pastely</title>`
- **Plugin array**: `registerPastefyPlugin` → `registerPastelyPlugin`
- **Plugin placeholder**: `/*PASTEFY_PLUGINS*/` → `/*PASTELY_PLUGINS*/`
- **Meta tags**: All references updated to "Pastely"
  - og:title
  - og:description
  - og:site_name
  - twitter:title
  - twitter:description
- **URLs**: `pastefy.app` → `pastely.app` (in meta tags)
- **Console ASCII art**: Updated GitHub URL to `github.com/interaapps/pastely`

### manifest.json
- **name**: `"pastefy"` → `"Pastely"`
- **short_name**: `"pastefy"` → `"Pastely"`

### CSS (CodeMirror theme)
- **Class names**: All 26 instances of `.cm-s-pastefy` → `.cm-s-pastely`

### embed.js
- **Function name**: `embedPastefy` → `embedPastely`
- **Default origin**: `https://pastefy.app` → `https://pastely.app`

### Vue Components (28 files)
- **CreatePaste.vue**: CodeMirror theme `'pastefy'` → `'pastely'`
- **MarkdownViewer.vue**:
  - Custom elements: `pastefy-mermaid-viewer` → `pastely-mermaid-viewer`
  - Custom elements: `pastefy-highlighted` → `pastely-highlighted`
  - Variable names: `pastefyMermaid` → `pastelyMermaid`
  - Variable names: `pastefyHighlighted` → `pastelyHighlighted`
- **EmbedPasteModal.vue**:
  - Element ID: `pastefy` → `pastely`
  - Function call: `embedPastefy` → `embedPastely`
  - Origin check: `https://pastefy.app` → `https://pastely.app`

## Phase 3: Infrastructure Files ✅

### Docker Compose
- **Service name**: `pastefy:` → `pastely:`
- **Image name**: `interaapps/pastefy:latest` → `interaapps/pastely:latest`
- **Note**: Database credentials kept as "pastefy" (internal, not user-visible)

### Kubernetes (deployment/prod.yaml)
- **All 16 occurrences updated**:
  - Deployment name: `pastefy-prod` → `pastely-prod`
  - App labels: `pastefy-prod` → `pastely-prod`
  - Service name: `pastefy-prod` → `pastely-prod`
  - Ingress name: `pastefy-prod` → `pastely-prod`
  - ConfigMap reference: `pastefy-env` → `pastely-env`

### CI/CD
- **.github/workflows/docker-hub.yml**:
  - Docker image: `interaapps/pastefy` → `interaapps/pastely`
- **.gitlab-ci.yml**:
  - Environment name: `pastefy-prod` → `pastely-prod`

## Phase 4: Documentation ✅

### CLAUDE.md (Coding Standards)
- **Title**: "Pastefy Coding Standards" → "Pastely Coding Standards"
- **Package structure**: `de.interaapps.pastefy` → `de.interaapps.pastely`
- **Main class**: `Pastefy.java` → `Pastely.java`
- **Plugin classes**: All plugin class names updated
- **Table prefix**: `pastefy_` → `pastely_` in examples
- **Config keys**: `pastefy.*` → `pastely.*` in examples
- **Frontend API**: `window.pastefy` → `window.pastely`

### README.md
- **Already updated by user** to reference Pastely
- **GitHub URLs**: Updated to `github.com/interaapps/pastely`
- **Database table**: `pastefy_users` → `pastely_users`

### PLUGINS.md
- **JAR name**: `pastefy-app.jar` → `pastely-app.jar`
- **API references**: `window.pastefy` → `window.pastely`
- **All API calls**: `pastefy.*` → `pastely.*`

### backend/README.md
- **Title**: "Pastefy Backend" → "Pastely Backend"

### .env.example
- **Added comprehensive section** with all new `PASTELY_*` variables
- **Migration notes** for transitioning from `PASTEFY_*`
- **Optional services** documented (Elasticsearch, Redis, MinIO, AI)

## Phase 5: Database Migration ✅

### migrations/pastefy-to-pastely.sql
**Created comprehensive SQL migration script with**:
- Rename all core tables (pastes, users, folders, auth_keys)
- Rename paste-related tables (stars, tags, shared, engagement)
- Rename auxiliary tables (notifications, tag_listing, login_tokens, oauth_accounts)
- Notes for Elasticsearch index migration
- Notes for Redis key migration
- Verification queries

## Phase 6: Migration Documentation ✅

### MIGRATION.md
**Created comprehensive migration guide with**:
- Overview of breaking changes
- Step-by-step migration procedure
- Database backup instructions
- Environment variable mapping
- Docker/Kubernetes update guides
- Elasticsearch reindexing instructions
- Custom plugin update guide
- Rollback procedure
- Troubleshooting section
- Timeline recommendations

## Summary Statistics

### Files Changed
- **Java files**: 125 files (all in backend)
- **Vue/TypeScript files**: 28+ files in frontend
- **Configuration files**: 5 files (pom.xml, docker-compose.yml, deployment.yaml, CI/CD)
- **Documentation files**: 8+ markdown files
- **CSS files**: 1 file (CodeMirror theme)
- **JavaScript files**: 1 file (embed.js)
- **HTML files**: 1 file (index.html)
- **JSON files**: 1 file (manifest.json)
- **Total**: 170+ files modified

### Code Changes
- **Package declarations**: 125 updated
- **Import statements**: 434 updated
- **Class name changes**: 5 main classes (Pastely, PastelyCLI, PastelyPlugin, PastelyPluginConfig, PastelyBackendPlugin)
- **Environment variables**: 14 renamed (PASTEFY_* → PASTELY_*)
- **Database tables**: 10+ tables to rename (pastefy_* → pastely_*)
- **CSS classes**: 26 references (cm-s-pastefy → cm-s-pastely)
- **Custom elements**: 2 renamed (pastefy-highlighted, pastefy-mermaid-viewer)

### Breaking Changes
1. **Java package rename**: Hard break for custom plugins
2. **Database table prefix**: Requires SQL migration
3. **Environment variables**: Must update all PASTEFY_* → PASTELY_*
4. **Docker image name**: Must update to interaapps/pastely
5. **Plugin base classes**: Must extend new Pastely* classes

### Backward Compatibility Maintained
1. **Frontend plugin API**: `window.pastefy` aliased to `window.pastely`
2. **Database credentials**: Internal names can stay as "pastefy"
3. **OAuth configurations**: No changes required

## Verification Checklist

- [x] All Java package declarations updated
- [x] All Java import statements updated
- [x] Main singleton class renamed and updated
- [x] CLI class renamed and updated
- [x] Plugin system classes renamed
- [x] Configuration mappings updated (env vars, config keys)
- [x] Database table prefix updated in code
- [x] Elasticsearch index name updated
- [x] HTTP server header updated
- [x] Frontend Window API renamed
- [x] Custom elements renamed
- [x] HTML title and meta tags updated
- [x] Manifest.json updated
- [x] CSS class names updated
- [x] Embed script function renamed
- [x] Vue components updated
- [x] Docker image name updated
- [x] Kubernetes manifests updated
- [x] CI/CD pipelines updated
- [x] Documentation files updated
- [x] Migration script created
- [x] Migration guide created
- [x] .env.example updated

## Post-Rebrand Tasks

### Required Before Deployment
1. Run database migration script on production database
2. Update environment variables on all servers
3. Update Docker image references in all deployments
4. Reindex Elasticsearch (or let auto-migration handle it)
5. Update OAuth redirect URLs if domain changed

### Recommended
1. Update GitHub repository name (if migrating)
2. Update domain name (pastefy.app → pastely.app)
3. Set up redirects from old domain
4. Update API documentation
5. Notify users of rebrand
6. Update browser extensions
7. Update third-party integrations

### Optional Cleanup
1. Archive old Elasticsearch indices
2. Clear Redis cache
3. Remove deprecated environment variables from configs
4. Update monitoring dashboards
5. Update backup scripts with new table names

## Known Limitations

1. **Maven not available in environment**: Could not verify Java compilation, but all changes follow correct patterns
2. **URL references in docs**: Some documentation still references old pastefy.app URLs (kept intentionally for existing deployments)
3. **API client libraries**: External libraries (Java, Go, JS clients) not updated as they're in separate repositories

## Testing Recommendations

1. **Unit tests**: Run full Java test suite after compilation
2. **Integration tests**: Test all API endpoints
3. **Frontend tests**: Test Vue components and plugins
4. **Database migration**: Test on copy of production database first
5. **OAuth flow**: Test all OAuth providers
6. **Search**: Test Elasticsearch after reindexing
7. **Plugins**: Test plugin loading and execution
8. **Docker**: Build and run Docker image
9. **Kubernetes**: Deploy to staging environment first

## Rollback Plan

If issues are encountered:
1. Keep database backup safe
2. Keep old Docker images available
3. Document all environment variable changes
4. Have rollback SQL script ready
5. Plan maintenance window with buffer time

## Success Criteria

- [ ] Application starts without errors
- [ ] All API endpoints respond correctly
- [ ] OAuth login works for all providers
- [ ] Pastes can be created and retrieved
- [ ] Search functionality works (if using Elasticsearch)
- [ ] Plugins load and execute correctly
- [ ] Frontend displays correctly
- [ ] Docker image builds successfully
- [ ] Kubernetes deployment succeeds
- [ ] All tests pass

## Completion Status

**PHASE 1-5: COMPLETE** ✅

All code changes, infrastructure updates, documentation, and migration scripts have been completed. The rebrand is ready for:
1. Code review
2. Testing
3. Database migration
4. Deployment

**Next Step**: Run comprehensive tests and deploy to staging environment.
