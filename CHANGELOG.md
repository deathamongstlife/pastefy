# Changelog

All notable changes to this project will be documented in this file.

## [7.0.0] - 2026-03-12 - Pastely Rebrand

### Breaking Changes

This release represents a complete rebrand from "Pastefy" to "Pastely" with significant architectural changes.

#### Backend Changes
- **Package Rename**: `de.interaapps.pastefy` → `de.interaapps.pastely`
- **Main Class**: `Pastefy.java` → `Pastely.java`
- **Singleton Access**: `Pastefy.getInstance()` → `Pastely.getInstance()`
- **Database Table Prefix**: `pastefy_` → `pastely_`
- **Environment Variables**: `PASTEFY_*` → `PASTELY_*`
- **Config Keys**: `pastefy.*` → `pastely.*`
- **Elasticsearch Index**: `pastefy_pastes` → `pastely_pastes`
- **Plugin Interfaces**: `PastefyBackendPlugin` → `PastelyBackendPlugin`
- **CLI Command**: `pastefy` → `pastely`

#### Frontend Changes
- **Window Object**: `window.pastefy` → `window.pastely` (backward compat alias maintained)
- **Plugin Registration**: `window.registerPastelyPlugin`
- **Custom Elements**: `<pastefy-highlighted>` → `<pastely-highlighted>`
- **CSS Classes**: `.cm-s-pastefy` → `.cm-s-pastely`
- **App Title**: "Pastefy" → "Pastely"
- **Domain References**: `pastefy.app` → `pastely.app`

#### Infrastructure Changes
- **Docker Image**: `interaapps/pastefy` → `interaapps/pastely`
- **Docker Compose Service**: `pastefy:` → `pastely:`
- **Kubernetes Resources**: `pastefy-prod` → `pastely-prod`
- **GitLab Environment**: `pastefy-prod` → `pastely-prod`

### Migration Guide

#### For Existing Installations

1. **Backup Your Database**
   ```bash
   mysqldump -u username -p database_name > backup.sql
   ```

2. **Run Database Migration**
   ```bash
   mysql -u username -p database_name < migrations/pastefy_to_pastely.sql
   ```

3. **Update Environment Variables**
   - Rename all `PASTEFY_*` variables to `PASTELY_*` in your `.env` file
   - The application will still support old variable names for backward compatibility

4. **Update Docker Compose**
   ```yaml
   # Change image reference
   image: interaapps/pastely:latest

   # Update service name
   pastely:
     ...
   ```

5. **Reindex Elasticsearch (if using)**
   ```bash
   # Run elastic migration command
   java -jar pastely.jar auto-migrate-elastic
   ```

6. **Update Plugins**
   - Plugin authors: Update imports from `de.interaapps.pastefy` to `de.interaapps.pastely`
   - Frontend plugins: Use `window.pastely` (or legacy `window.pastefy` alias)

#### For Plugin Developers

**Backend Plugins:**
```java
// Old
import de.interaapps.pastefy.Pastefy;
public class MyPlugin extends PastefyBackendPlugin {
    public void onLoad(Pastefy pastefy) { }
}

// New
import de.interaapps.pastely.Pastely;
public class MyPlugin extends PastelyBackendPlugin {
    public void onLoad(Pastely pastely) { }
}
```

**Frontend Plugins:**
```javascript
// New (recommended)
window.pastely.createPlugin({ /* ... */ })

// Old (still works via backward compatibility alias)
window.pastefy.createPlugin({ /* ... */ })
```

### Backward Compatibility

- **Frontend**: `window.pastefy` alias maintained for plugin compatibility
- **Config Keys**: Both `pastefy.*` and `pastely.*` keys supported in transition
- **Database Names**: Existing database names (user/password) can remain unchanged
- **API Endpoints**: All endpoints remain at `/api/v2/*` (no changes)

### Attribution

This project is a fork and rebrand of the original Pastefy project by InteraApps.

**Original Project**: https://github.com/interaapps/pastefy
**Original Author**: InteraApps
**License**: MIT

We extend our gratitude to the original developers for creating an excellent foundation.

### New Features in 7.0

For details on the 150+ new features added in version 7.0, see the main README.md file.

Key feature categories:
- Version Control System (Git-like branching and diffs)
- Real-time Collaboration
- Advanced Security
- Social Features
- Analytics & Tracking
- Enhanced Organization
- Integrations (Webhooks, GitHub Gist)
- Media Support
- Code Templates
- And much more!

---

## Previous Versions

For changelog history prior to the rebrand, please refer to the original Pastefy project repository.
