# Phase 3 Quick Start Guide

Quick reference for using Phase 3 features in Pastely.

## Media Support

### Upload a File
```bash
curl -X POST http://localhost/api/v2/media/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@/path/to/file.pdf"
```

Response:
```json
{
  "id": "abc12345",
  "filename": "randomstring_file.pdf",
  "original_filename": "file.pdf",
  "mime_type": "application/pdf",
  "file_size": 102400,
  "storage_type": "MINIO",
  "download_url": "/api/v2/media/download/abc12345",
  "created_at": "2026-03-12T10:30:00Z"
}
```

### Attach to Paste
```bash
curl -X POST http://localhost/api/v2/media/paste/xyz789/attach \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"attachmentId": "abc12345"}'
```

### Download File
```bash
curl -X GET http://localhost/api/v2/media/download/abc12345 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o downloaded_file.pdf
```

---

## GitHub Gist Integration

### Import Gist
```bash
curl -X POST http://localhost/api/v2/integration/gist/import \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://gist.github.com/username/abc123def456"
  }'
```

### Export to Gist
```bash
curl -X POST http://localhost/api/v2/integration/gist/export/xyz789 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "githubToken": "ghp_xxxxxxxxxxxx",
    "isPublic": false
  }'
```

Response:
```json
{
  "gist_url": "https://gist.github.com/username/abc123def456",
  "success": true
}
```

---

## Webhooks

### Create Webhook
```bash
curl -X POST http://localhost/api/v2/webhooks \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com/webhook",
    "events": "paste.created,paste.updated,paste.deleted"
  }'
```

Response:
```json
{
  "id": "wh12345",
  "url": "https://example.com/webhook",
  "secret": "randomsecret32chars",
  "events": "paste.created,paste.updated,paste.deleted",
  "isActive": true,
  "created_at": "2026-03-12T10:30:00Z"
}
```

### Webhook Payload Example
When an event occurs, Pastely sends:
```json
{
  "event": "paste.created",
  "timestamp": 1678624200000,
  "data": {
    "key": "xyz789",
    "title": "My Paste",
    "content": "...",
    "created_at": "2026-03-12T10:30:00Z"
  }
}
```

Headers:
```
X-Pastely-Signature: sha256=abcdef123456...
X-Pastely-Event: paste.created
Content-Type: application/json
User-Agent: Pastely-Webhook/1.0
```

### Verify Webhook (Node.js)
```javascript
const crypto = require('crypto');

function verifyWebhook(payload, signature, secret) {
  const hmac = crypto.createHmac('sha256', secret);
  hmac.update(payload);
  const expectedSignature = 'sha256=' + hmac.digest('hex');
  return crypto.timingSafeEqual(
    Buffer.from(signature),
    Buffer.from(expectedSignature)
  );
}

app.post('/webhook', (req, res) => {
  const signature = req.headers['x-pastely-signature'];
  const payload = JSON.stringify(req.body);

  if (verifyWebhook(payload, signature, WEBHOOK_SECRET)) {
    console.log('Event:', req.body.event);
    console.log('Data:', req.body.data);
    res.sendStatus(200);
  } else {
    res.sendStatus(401);
  }
});
```

### Verify Webhook (Python)
```python
import hmac
import hashlib

def verify_webhook(payload, signature, secret):
    expected = 'sha256=' + hmac.new(
        secret.encode(),
        payload.encode(),
        hashlib.sha256
    ).hexdigest()
    return hmac.compare_digest(signature, expected)

@app.route('/webhook', methods=['POST'])
def webhook():
    signature = request.headers.get('X-Pastely-Signature')
    payload = request.get_data(as_text=True)

    if verify_webhook(payload, signature, WEBHOOK_SECRET):
        data = request.json
        print(f"Event: {data['event']}")
        print(f"Data: {data['data']}")
        return '', 200
    return '', 401
```

---

## Code Templates

### Create Template
```bash
curl -X POST http://localhost/api/v2/templates \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "React Component",
    "description": "Basic React functional component",
    "language": "javascript",
    "content": "import React from 'react';\n\nexport default function Component() {\n  return <div>Hello</div>;\n}",
    "category": "React",
    "isPublic": true
  }'
```

### List Templates
```bash
# All public templates
curl -X GET http://localhost/api/v2/templates

# Filter by language
curl -X GET "http://localhost/api/v2/templates?language=javascript"

# Filter by category
curl -X GET "http://localhost/api/v2/templates?category=React"

# Include private templates (requires auth)
curl -X GET "http://localhost/api/v2/templates?public=false" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Use Template (Increment Count)
```bash
curl -X POST http://localhost/api/v2/templates/tpl12345/use
```

---

## Configuration

### Environment Variables

```bash
# MinIO Configuration (required for media support)
MINIO_SERVER=http://localhost:9000
MINIO_BUCKET=pastely
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# Rate Limiting
RATELIMITER_MILLIS=5000
RATELIMITER_LIMIT=5
```

### MinIO Setup (Docker)
```bash
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin \
  --name minio \
  quay.io/minio/minio server /data --console-address ":9001"

# Create bucket
docker exec minio mc alias set local http://localhost:9000 minioadmin minioadmin
docker exec minio mc mb local/pastely
docker exec minio mc anonymous set public local/pastely
```

---

## Common Use Cases

### 1. File Attachments for Code Snippets
```bash
# Upload documentation
curl -X POST http://localhost/api/v2/media/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@README.md"

# Attach to paste
curl -X POST http://localhost/api/v2/media/paste/$PASTE_KEY/attach \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"attachmentId\": \"$ATTACHMENT_ID\"}"
```

### 2. Sync with GitHub Gists
```bash
# Import your gists
curl -X POST http://localhost/api/v2/integration/gist/import \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://gist.github.com/yourusername/abc123"}'

# Export paste to gist
curl -X POST http://localhost/api/v2/integration/gist/export/$PASTE_KEY \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"githubToken\": \"$GITHUB_TOKEN\", \"isPublic\": false}"
```

### 3. Webhook Notifications
```bash
# Subscribe to all paste events
curl -X POST http://localhost/api/v2/webhooks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com/webhook",
    "events": "paste.*"
  }'

# Subscribe to specific events
curl -X POST http://localhost/api/v2/webhooks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://discord.com/api/webhooks/...",
    "events": "paste.created"
  }'
```

### 4. Code Template Library
```bash
# Browse templates
curl -X GET "http://localhost/api/v2/templates?language=python&category=Django"

# Create custom template
curl -X POST http://localhost/api/v2/templates \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Python FastAPI Endpoint",
    "description": "Basic FastAPI route handler",
    "language": "python",
    "content": "@app.get(\"/\")\nasync def root():\n    return {\"message\": \"Hello World\"}",
    "category": "FastAPI",
    "isPublic": true
  }'
```

---

## Troubleshooting

### Media Upload Fails
```bash
# Check MinIO is running
curl http://localhost:9000/minio/health/live

# Verify bucket exists
docker exec minio mc ls local/pastely

# Check Pastely logs
tail -f pastely.log | grep -i minio
```

### Webhook Not Triggering
```bash
# Check webhook status
curl -X GET http://localhost/api/v2/webhooks/$WEBHOOK_ID \
  -H "Authorization: Bearer $TOKEN"

# View event history
curl -X GET "http://localhost/api/v2/webhooks/$WEBHOOK_ID/events?limit=10" \
  -H "Authorization: Bearer $TOKEN"

# Test webhook
curl -X POST http://localhost/api/v2/webhooks/$WEBHOOK_ID/test \
  -H "Authorization: Bearer $TOKEN"
```

### GitHub Gist Import Fails
```bash
# Verify gist is public
curl https://api.github.com/gists/abc123def456

# Check rate limits
curl -H "Authorization: Bearer $GITHUB_TOKEN" \
  https://api.github.com/rate_limit
```

---

## Integration with Pastely Workflow

### Complete Workflow Example
```bash
#!/bin/bash

TOKEN="your_api_token"
BASE_URL="http://localhost"

# 1. Create a paste
PASTE=$(curl -s -X POST "$BASE_URL/api/v2/pastes" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Project",
    "content": "# Project Documentation\n...",
    "type": "markdown"
  }')

PASTE_KEY=$(echo $PASTE | jq -r '.key')
echo "Created paste: $PASTE_KEY"

# 2. Upload related files
ATTACHMENT=$(curl -s -X POST "$BASE_URL/api/v2/media/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@diagram.png")

ATTACHMENT_ID=$(echo $ATTACHMENT | jq -r '.id')
echo "Uploaded file: $ATTACHMENT_ID"

# 3. Attach file to paste
curl -s -X POST "$BASE_URL/api/v2/media/paste/$PASTE_KEY/attach" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"attachmentId\": \"$ATTACHMENT_ID\"}"

echo "Attached file to paste"

# 4. Export to GitHub
GIST=$(curl -s -X POST "$BASE_URL/api/v2/integration/gist/export/$PASTE_KEY" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"githubToken\": \"$GITHUB_TOKEN\", \"isPublic\": false}")

GIST_URL=$(echo $GIST | jq -r '.gist_url')
echo "Exported to gist: $GIST_URL"

# 5. Setup webhook for updates
WEBHOOK=$(curl -s -X POST "$BASE_URL/api/v2/webhooks" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com/webhook",
    "events": "paste.updated"
  }')

echo "Webhook configured"
echo "Secret: $(echo $WEBHOOK | jq -r '.secret')"
```

---

## API Testing with Postman

Import this collection to test all endpoints:

```json
{
  "info": {
    "name": "Pastely Phase 3 API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Media",
      "item": [
        {
          "name": "Upload File",
          "request": {
            "method": "POST",
            "header": [],
            "body": {
              "mode": "formdata",
              "formdata": [
                {
                  "key": "file",
                  "type": "file"
                }
              ]
            },
            "url": "{{base_url}}/api/v2/media/upload"
          }
        },
        {
          "name": "Attach to Paste",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"attachmentId\": \"abc12345\"}"
            },
            "url": "{{base_url}}/api/v2/media/paste/{{paste_key}}/attach"
          }
        }
      ]
    },
    {
      "name": "Integration",
      "item": [
        {
          "name": "Import Gist",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"url\": \"https://gist.github.com/username/abc123\"}"
            },
            "url": "{{base_url}}/api/v2/integration/gist/import"
          }
        },
        {
          "name": "Export to Gist",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"githubToken\": \"{{github_token}}\", \"isPublic\": false}"
            },
            "url": "{{base_url}}/api/v2/integration/gist/export/{{paste_key}}"
          }
        }
      ]
    }
  ],
  "auth": {
    "type": "bearer",
    "bearer": [
      {
        "key": "token",
        "value": "{{api_token}}"
      }
    ]
  },
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost"
    },
    {
      "key": "api_token",
      "value": "your_token_here"
    },
    {
      "key": "github_token",
      "value": "your_github_token_here"
    },
    {
      "key": "paste_key",
      "value": "xyz789"
    }
  ]
}
```

---

## Next Steps

1. **Test the APIs** using curl or Postman
2. **Implement Frontend** components for these features
3. **Configure MinIO** for file storage
4. **Setup Webhooks** for integrations
5. **Create Templates** for common code patterns

For detailed implementation information, see `PHASE3_IMPLEMENTATION.md`.
