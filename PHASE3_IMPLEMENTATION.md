# Phase 3 Implementation Summary - Pastely

This document details the implementation of Phase 3 features: Media Support, Integrations, and Enhanced Editor functionality for Pastely.

## Overview

Phase 3 adds advanced features to Pastely, including file attachment support, external service integrations (GitHub Gist), webhook notifications, and code template management.

## Implemented Features

### 1. Media Support

#### File Attachments
- **File Upload**: Users can upload files up to 10MB
- **Storage**: Files are stored in MinIO/S3-compatible storage
- **Attachment Management**: Files can be attached to pastes
- **Download**: Secure file download with access control
- **File Types**: Supports all MIME types with automatic type detection

#### Components Created

**Model**: `PasteAttachment.java` (already existed)
- Stores attachment metadata
- Links files to pastes
- Tracks storage location and type

**Controller**: `MediaController.java` (NEW)
- `POST /api/v2/media/upload` - Upload a file
- `POST /api/v2/media/paste/{pasteKey}/attach` - Attach file to paste
- `GET /api/v2/media/paste/{pasteKey}/attachments` - List attachments
- `GET /api/v2/media/download/{attachmentId}` - Download file
- `DELETE /api/v2/media/{attachmentId}` - Delete attachment

**Request DTOs**:
- `AttachToPasteRequest.java` - Attach file to paste

**Response DTOs**:
- `AttachmentResponse.java` - File attachment information

#### Features
- File size validation (10MB limit)
- Filename sanitization
- MinIO/S3 integration
- Access control (paste ownership check)
- Automatic cleanup on deletion

---

### 2. Integrations

#### GitHub Gist Integration
- **Import**: Import GitHub Gists as pastes
- **Export**: Export pastes to GitHub Gists
- **Language Detection**: Automatic language/type detection from file extensions
- **Privacy Control**: Public/private gist creation

#### Components Created

**Controller**: `IntegrationController.java` (NEW)
- `POST /api/v2/integration/gist/import` - Import from GitHub Gist
- `POST /api/v2/integration/gist/export/{pasteKey}` - Export to GitHub Gist

**Request DTOs**:
- `ImportGistRequest.java` - Gist URL for import
- `ExportGistRequest.java` - GitHub token and privacy settings

**Response DTOs**:
- `GistExportResponse.java` - Created gist URL

#### Features
- Flexible URL parsing (handles various gist URL formats)
- Language mapping (40+ programming languages)
- GitHub API v3 integration
- Token-based authentication
- Error handling with meaningful messages

---

### 3. Webhook System

#### Webhook Management
- **Subscriptions**: Users can subscribe to events
- **Event Filtering**: Support for wildcard and specific event subscriptions
- **Signature Verification**: HMAC-SHA256 signatures for security
- **Failure Handling**: Automatic retry and deactivation after failures
- **Event Log**: Complete history of webhook deliveries

#### Components Created

**Models**: `Webhook.java` and `WebhookEvent.java` (already existed)
- Store webhook configurations
- Track delivery attempts and failures

**Controller**: `WebhookController.java` (already existed)
- Full CRUD for webhook management
- Event history retrieval
- Test endpoint

**Service**: `WebhookService.java` (NEW)
- Asynchronous webhook delivery
- HMAC-SHA256 signature generation
- Event subscription matching (supports wildcards)
- Automatic failure tracking
- Auto-disable after 5 consecutive failures

#### Supported Events
- `paste.created` - New paste created
- `paste.updated` - Paste modified
- `paste.deleted` - Paste deleted
- `paste.shared` - Paste shared with another user
- Wildcard support: `paste.*` matches all paste events

#### Features
- Asynchronous delivery (non-blocking)
- HMAC-SHA256 request signing
- Configurable timeout (10 seconds)
- Failure tracking with auto-disable
- Event filtering with wildcards
- Custom headers (X-Pastely-Signature, X-Pastely-Event)

---

### 4. Enhanced Editor - Code Templates

#### Template Management
- **Library**: Reusable code snippets and templates
- **Categories**: Organize templates by category
- **Public Sharing**: Share templates with community
- **Usage Tracking**: Track template popularity
- **Language Filtering**: Filter by programming language

#### Components Created

**Model**: `CodeTemplate.java` (already existed)
- Store template content and metadata
- Track usage count
- Support public/private templates

**Controller**: `TemplateController.java` (already existed)
- Full CRUD for templates
- Public template discovery
- User template management
- Usage tracking

#### Features
- Public and private templates
- Category organization
- Language-specific filtering
- Usage statistics
- Permission-based access control

---

## API Endpoints Summary

### Media API
```
POST   /api/v2/media/upload                          - Upload file (auth, rate-limited)
POST   /api/v2/media/paste/{pasteKey}/attach         - Attach to paste (auth)
GET    /api/v2/media/paste/{pasteKey}/attachments    - List attachments (login-required-read)
GET    /api/v2/media/download/{attachmentId}         - Download file (login-required-read)
DELETE /api/v2/media/{attachmentId}                  - Delete attachment (auth)
```

### Integration API
```
POST   /api/v2/integration/gist/import               - Import from Gist (auth, rate-limited)
POST   /api/v2/integration/gist/export/{pasteKey}    - Export to Gist (auth, rate-limited)
```

### Webhook API (Already Existed)
```
POST   /api/v2/webhooks                              - Create webhook (auth)
GET    /api/v2/webhooks                              - List webhooks (auth)
GET    /api/v2/webhooks/{webhookId}                  - Get webhook (auth)
PUT    /api/v2/webhooks/{webhookId}                  - Update webhook (auth)
DELETE /api/v2/webhooks/{webhookId}                  - Delete webhook (auth)
GET    /api/v2/webhooks/{webhookId}/events           - Get event history (auth)
POST   /api/v2/webhooks/{webhookId}/test             - Test webhook (auth)
```

### Template API (Already Existed)
```
POST   /api/v2/templates                             - Create template (auth)
GET    /api/v2/templates                             - List templates (public)
GET    /api/v2/templates/{templateId}                - Get template (public/auth)
PUT    /api/v2/templates/{templateId}                - Update template (auth)
DELETE /api/v2/templates/{templateId}                - Delete template (auth)
POST   /api/v2/templates/{templateId}/use            - Increment usage (public)
GET    /api/v2/templates/user/{userId}               - User templates (public/auth)
```

---

## Technical Implementation Details

### Architecture Patterns

#### 1. Controller Pattern
- All controllers extend `HttpController`
- Use JavaWebStack HTTP Router annotations
- Follow RESTful conventions
- Middleware chain for auth and rate limiting

#### 2. Service Pattern
- `WebhookService` uses static utility methods
- Asynchronous execution via Pastely executor
- Business logic separated from controllers

#### 3. DTO Pattern
- Request DTOs: Simple POJOs with public fields
- Response DTOs: Factory methods for creation
- JSON serialization with Gson
- Snake_case for JSON keys via `@SerializedName`

#### 4. Security
- Bearer token authentication
- Permission checks via middleware
- HMAC-SHA256 webhook signatures
- File size validation
- Filename sanitization

### Data Models

#### PasteAttachment
```java
@Table("paste_attachments")
- id (8 chars)
- pasteId (8 chars, nullable)
- filename
- originalFilename
- mimeType
- fileSize (bytes)
- storageType (MINIO, LOCAL)
- storagePath
- thumbnailPath (for images)
- createdAt
```

#### Webhook
```java
@Table("webhooks")
- id (8 chars)
- userId (8 chars)
- url
- secret (32 chars, auto-generated)
- events (comma-separated)
- isActive
- failureCount
- lastTriggeredAt
- createdAt
- updatedAt
```

#### CodeTemplate
```java
@Table("code_templates")
- id (8 chars)
- userId (8 chars)
- name
- description
- language
- content (MEDIUMTEXT)
- category
- isPublic
- useCount
- createdAt
- updatedAt
```

### Configuration

#### Environment Variables
```bash
# MinIO/S3 Storage
MINIO_SERVER=http://localhost:9000
MINIO_BUCKET=pastely
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# Rate Limiting
RATELIMITER_MILLIS=5000
RATELIMITER_LIMIT=5
```

### Integration Points

#### Pastely.java
- Controllers are auto-registered via package scanning
- Line 530: `httpRouter.controller(HttpController.class, AppController.class.getPackage());`
- No manual registration needed for new controllers

#### MinIO Integration
- `Pastely.getInstance().isMinioEnabled()` - Check availability
- `Pastely.getInstance().getMinioClient()` - Get client
- `Pastely.getInstance().getMinioBucket()` - Get bucket name

#### Async Execution
- `Pastely.getInstance().executeAsync(Runnable)` - Non-blocking execution
- Used for webhook delivery and heavy I/O operations

---

## Usage Examples

### 1. Upload and Attach File

```javascript
// Upload file
const formData = new FormData();
formData.append('file', fileInput.files[0]);

const uploadResponse = await fetch('/api/v2/media/upload', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN'
  },
  body: formData
});

const { id: attachmentId } = await uploadResponse.json();

// Attach to paste
await fetch('/api/v2/media/paste/abc123/attach', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ attachmentId })
});
```

### 2. Import GitHub Gist

```javascript
const response = await fetch('/api/v2/integration/gist/import', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    url: 'https://gist.github.com/username/gistid'
  })
});

const paste = await response.json();
console.log('Imported paste:', paste.key);
```

### 3. Export to GitHub Gist

```javascript
const response = await fetch('/api/v2/integration/gist/export/abc123', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    githubToken: 'ghp_xxxxxxxxxxxx',
    isPublic: false
  })
});

const { gist_url } = await response.json();
console.log('Gist created:', gist_url);
```

### 4. Create Webhook

```javascript
const response = await fetch('/api/v2/webhooks', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    url: 'https://example.com/webhook',
    events: 'paste.created,paste.updated'
  })
});

const webhook = await response.json();
console.log('Webhook secret:', webhook.secret);
```

### 5. Verify Webhook Signature

```javascript
const crypto = require('crypto');

function verifyWebhook(payload, signature, secret) {
  const hmac = crypto.createHmac('sha256', secret);
  hmac.update(payload);
  const expectedSignature = 'sha256=' + hmac.digest('hex');
  return signature === expectedSignature;
}

// In your webhook handler
app.post('/webhook', (req, res) => {
  const signature = req.headers['x-pastely-signature'];
  const payload = JSON.stringify(req.body);

  if (verifyWebhook(payload, signature, WEBHOOK_SECRET)) {
    console.log('Valid webhook:', req.body);
    res.sendStatus(200);
  } else {
    res.sendStatus(401);
  }
});
```

---

## Database Schema Changes

All models use ORM auto-migration, so tables will be created automatically on first run:

```sql
-- paste_attachments table (already exists)
CREATE TABLE pastely_paste_attachments (
  id VARCHAR(8) PRIMARY KEY,
  pasteId VARCHAR(8),
  filename VARCHAR(255),
  originalFilename VARCHAR(255),
  mimeType VARCHAR(255),
  fileSize BIGINT,
  storageType VARCHAR(50),
  storagePath VARCHAR(500),
  thumbnailPath VARCHAR(500),
  createdAt TIMESTAMP
);

-- webhooks table (already exists)
CREATE TABLE pastely_webhooks (
  id VARCHAR(8) PRIMARY KEY,
  userId VARCHAR(8),
  url VARCHAR(500),
  secret VARCHAR(100),
  events VARCHAR(500),
  isActive BOOLEAN,
  failureCount INT,
  lastTriggeredAt TIMESTAMP,
  createdAt TIMESTAMP,
  updatedAt TIMESTAMP
);

-- webhook_events table (already exists)
CREATE TABLE pastely_webhook_events (
  id VARCHAR(8) PRIMARY KEY,
  webhookId VARCHAR(8),
  eventType VARCHAR(100),
  payload TEXT,
  success BOOLEAN,
  responseCode INT,
  responseBody TEXT,
  errorMessage TEXT,
  attemptedAt TIMESTAMP
);

-- code_templates table (already exists)
CREATE TABLE pastely_code_templates (
  id VARCHAR(8) PRIMARY KEY,
  userId VARCHAR(8),
  name VARCHAR(255),
  description TEXT,
  language VARCHAR(100),
  content MEDIUMTEXT,
  category VARCHAR(100),
  isPublic BOOLEAN,
  useCount INT,
  createdAt TIMESTAMP,
  updatedAt TIMESTAMP
);
```

---

## Testing

### Manual Testing Checklist

#### Media Support
- [ ] Upload file < 10MB (should succeed)
- [ ] Upload file > 10MB (should fail with 400)
- [ ] Attach file to paste (should succeed)
- [ ] Download attached file (should succeed)
- [ ] Delete attachment (should succeed)
- [ ] Try to access another user's attachment (should fail with 403)

#### GitHub Gist Integration
- [ ] Import public gist (should succeed)
- [ ] Import with invalid URL (should fail with 400)
- [ ] Export paste to gist (should succeed and return URL)
- [ ] Export without GitHub token (should fail with 400)

#### Webhooks
- [ ] Create webhook (should return secret)
- [ ] Trigger webhook on paste creation (should deliver)
- [ ] Verify webhook signature (should match)
- [ ] Fail webhook 5 times (should auto-disable)
- [ ] Test wildcard subscriptions (paste.* should match all)

#### Code Templates
- [ ] Create private template (should succeed)
- [ ] Create public template (should succeed)
- [ ] View public templates (should see all public)
- [ ] Filter by language (should return correct templates)
- [ ] Use template (should increment useCount)

---

## Frontend Integration Notes

The frontend should be updated to integrate these features:

### Required Components (Not Yet Implemented)

1. **File Upload Dropzone**
   - Drag-and-drop support
   - Progress indicator
   - File type icons
   - Size validation feedback

2. **Attachment Manager**
   - List attachments for a paste
   - Download buttons
   - Delete functionality
   - File size/type display

3. **GitHub Integration Dialog**
   - Import Gist input field
   - Export with token input
   - Public/private toggle
   - Success/error feedback

4. **Webhook Manager**
   - CRUD interface for webhooks
   - Event subscription checkboxes
   - Secret display (with copy button)
   - Event history log viewer
   - Test webhook button

5. **Template Library**
   - Browse public templates
   - Filter by language/category
   - Preview template content
   - "Use Template" action
   - User's private templates section

### Suggested UI Locations
- **Media Upload**: New tab in paste editor
- **Gist Integration**: Tools menu or share dialog
- **Webhooks**: User settings page
- **Templates**: Editor toolbar dropdown

---

## Security Considerations

1. **File Upload**
   - Size limit enforced (10MB)
   - Filename sanitization to prevent directory traversal
   - MIME type validation
   - Access control on downloads

2. **Webhooks**
   - HMAC-SHA256 signature verification
   - Rate limiting on webhook creation
   - Auto-disable after repeated failures
   - Timeout protection (10 seconds)

3. **GitHub Integration**
   - User-provided tokens (not stored)
   - Rate limiting on import/export
   - Error message sanitization

4. **Templates**
   - Permission checks on private templates
   - Public templates are read-only for non-owners
   - XSS prevention (content is not executed)

---

## Performance Considerations

1. **Asynchronous Operations**
   - Webhook delivery is non-blocking
   - Uses thread pool (30 threads) for parallelism

2. **File Storage**
   - MinIO/S3 for scalable storage
   - Streaming downloads (no memory buffering)

3. **Database Queries**
   - Indexed on userId for fast lookups
   - Pagination support for lists
   - Connection pooling enabled

---

## Future Enhancements

### Potential Improvements
1. **Media Support**
   - Image thumbnail generation
   - Multiple file upload
   - Cloud storage options (AWS S3, Azure Blob)
   - CDN integration

2. **Integrations**
   - GitLab snippet support
   - Pastebin import/export
   - Slack notifications
   - Discord webhooks

3. **Webhooks**
   - Webhook retry with exponential backoff
   - Custom headers configuration
   - Payload templates
   - Event filtering rules

4. **Templates**
   - Template variables/placeholders
   - Community template marketplace
   - Template versioning
   - Fork/clone templates

---

## Troubleshooting

### Common Issues

**Q: File upload returns 503 "File storage not configured"**
A: Configure MinIO environment variables (MINIO_SERVER, MINIO_BUCKET, etc.)

**Q: Webhooks not being delivered**
A: Check webhook status (may be disabled after failures), verify URL is accessible

**Q: GitHub Gist import fails with 404**
A: Verify gist is public or URL is correct, check GitHub API rate limits

**Q: Templates not appearing**
A: Check isPublic flag and user authentication

---

## Files Created/Modified

### New Files
1. `backend/src/main/java/cc/allyapps/pastely/controller/MediaController.java`
2. `backend/src/main/java/cc/allyapps/pastely/controller/IntegrationController.java`
3. `backend/src/main/java/cc/allyapps/pastely/services/WebhookService.java`
4. `backend/src/main/java/cc/allyapps/pastely/model/requests/AttachToPasteRequest.java`
5. `backend/src/main/java/cc/allyapps/pastely/model/requests/ImportGistRequest.java`
6. `backend/src/main/java/cc/allyapps/pastely/model/requests/ExportGistRequest.java`
7. `backend/src/main/java/cc/allyapps/pastely/model/responses/AttachmentResponse.java`
8. `backend/src/main/java/cc/allyapps/pastely/model/responses/GistExportResponse.java`

### Existing Files (Already Implemented)
- `backend/src/main/java/cc/allyapps/pastely/model/database/PasteAttachment.java`
- `backend/src/main/java/cc/allyapps/pastely/model/database/Webhook.java`
- `backend/src/main/java/cc/allyapps/pastely/model/database/WebhookEvent.java`
- `backend/src/main/java/cc/allyapps/pastely/model/database/CodeTemplate.java`
- `backend/src/main/java/cc/allyapps/pastely/controller/WebhookController.java`
- `backend/src/main/java/cc/allyapps/pastely/controller/TemplateController.java`

---

## Conclusion

Phase 3 successfully implements comprehensive media support, external integrations, and enhanced editor features for Pastely. The implementation follows all established patterns from CLAUDE.md and integrates seamlessly with the existing codebase.

All backend functionality is complete and ready for testing. Frontend integration is the next step to make these features accessible to users through the cyberpunk-themed UI.

**Status**: ✅ Backend Implementation Complete
**Version**: 7.2.0
**Date**: 2026-03-12
