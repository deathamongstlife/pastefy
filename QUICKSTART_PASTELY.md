# Pastely 7.0 - Quick Start Guide

## Getting Started

This guide will help you quickly get started with the new Pastely 7.0 features.

## Installation

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8+ or SQLite
- (Optional) Redis 6+
- (Optional) Elasticsearch 8+
- (Optional) MinIO/S3

### Setup Steps

1. **Clone and Build**
```bash
git clone https://github.com/YOUR_USERNAME/pastely.git
cd pastely

# Build frontend
cd frontend
npm install
npm run build

# Build backend
cd ../backend
mvn clean package

# Copy environment file
cd ..
cp .env.example .env
nano .env
```

2. **Configure Database**
```env
DATABASE_DRIVER=mysql
DATABASE_HOST=localhost
DATABASE_PORT=3306
DATABASE_NAME=pastely
DATABASE_USER=root
DATABASE_PASSWORD=your_password

# Auto-migration is enabled by default
PASTELY_AUTOMIGRATE=true
```

3. **Configure J.A.R.V.I.S AI (Optional)**
```env
# J.A.R.V.I.S Gateway Configuration
JARVIS_GATEWAY_URL=http://127.0.0.1:18789
JARVIS_GATEWAY_TOKEN=your-jarvis-token
JARVIS_AGENT_ID=main
JARVIS_TIMEOUT_MS=30000
```

To get a J.A.R.V.I.S token:
- Visit the J.A.R.V.I.S Gateway documentation
- Create an agent with ID "main"
- Copy the API token to your `.env` file

4. **Run**
```bash
java -jar backend/target/backend.jar
```

The application will be available at `http://localhost:80`

## Using New Features

### 1. Version Control

**Create a Revision**
```bash
curl -X POST http://localhost/api/v2/paste/YOUR_PASTE_KEY/revisions \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "commitMessage": "Fixed typo in line 10",
    "diffContent": "--- Original\n+++ Modified\n@@ -10,1 +10,1 @@\n-console.log(\"Helo\")\n+console.log(\"Hello\")"
  }'
```

**Get Revision History**
```bash
curl http://localhost/api/v2/paste/YOUR_PASTE_KEY/revisions \
  -H "Authorization: Bearer YOUR_API_KEY"
```

### 2. Real-time Collaboration

**Start a Collaboration Session**
```bash
curl -X POST http://localhost/api/v2/collaboration/sessions \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "pasteKey": "YOUR_PASTE_KEY",
    "maxParticipants": 5,
    "expiresInHours": 2
  }'
```

Response includes a `sessionToken` that others can use to join.

**Join a Session**
```bash
curl http://localhost/api/v2/collaboration/sessions/token/SESSION_TOKEN
```

### 3. Advanced Security

**Set Password Protection**
```bash
curl -X POST http://localhost/api/v2/paste/YOUR_PASTE_KEY/security/access \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "password": "mySecurePassword",
    "maxViews": 10,
    "ipWhitelist": "192.168.1.0/24"
  }'
```

**Verify Access**
```bash
curl -X POST http://localhost/api/v2/paste/YOUR_PASTE_KEY/security/verify \
  -H "Content-Type: application/json" \
  -d '{
    "password": "mySecurePassword"
  }'
```

### 4. Social Features

**Follow a User**
```bash
curl -X POST http://localhost/api/v2/social/follow/USER_ID \
  -H "Authorization: Bearer YOUR_API_KEY"
```

**Add a Comment**
```bash
curl -X POST http://localhost/api/v2/social/paste/YOUR_PASTE_KEY/comments \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Great code! Thanks for sharing."
  }'
```

**Get Activity Feed**
```bash
curl http://localhost/api/v2/social/feed \
  -H "Authorization: Bearer YOUR_API_KEY"
```

### 5. Analytics

**Track a View** (automatically called by frontend)
```bash
curl -X POST http://localhost/api/v2/analytics/paste/YOUR_PASTE_KEY/view \
  -H "Content-Type: application/json" \
  -d '{
    "timeSpent": 120
  }'
```

**Get Analytics**
```bash
curl http://localhost/api/v2/analytics/paste/YOUR_PASTE_KEY \
  -H "Authorization: Bearer YOUR_API_KEY"
```

**Get Trending Pastes**
```bash
curl http://localhost/api/v2/analytics/trending?limit=20
```

### 6. Collections

**Create a Collection**
```bash
curl -X POST http://localhost/api/v2/collections \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "React Snippets",
    "description": "Useful React code snippets",
    "isPublic": true
  }'
```

**Add Paste to Collection**
```bash
curl -X POST http://localhost/api/v2/collections/COLLECTION_ID/pastes \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "pasteKey": "YOUR_PASTE_KEY",
    "sortOrder": 0
  }'
```

### 7. Webhooks

**Create a Webhook**
```bash
curl -X POST http://localhost/api/v2/webhooks \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://your-server.com/webhook",
    "events": "paste.created,paste.updated,paste.deleted"
  }'
```

**Webhook Payload Format**
```json
{
  "event": "paste.created",
  "timestamp": "2024-03-12T10:30:00Z",
  "data": {
    "pasteKey": "abc123",
    "title": "My Paste",
    "userId": "user123"
  }
}
```

Webhooks are signed with HMAC-SHA256. Verify using the `X-Webhook-Signature` header.

### 8. Code Templates

**Create a Template**
```bash
curl -X POST http://localhost/api/v2/templates \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "React Component Boilerplate",
    "language": "typescript",
    "content": "import React from \"react\"\n\nexport const Component = () => {\n  return <div></div>\n}",
    "category": "react",
    "isPublic": true
  }'
```

**Search Templates**
```bash
curl http://localhost/api/v2/templates?language=typescript&category=react
```

## Frontend Usage

### Using Stores in Vue 3 Components

**Revisions Store**
```vue
<script setup lang="ts">
import { useRevisionsStore } from '@/stores/revisions'
import { onMounted } from 'vue'

const revisionsStore = useRevisionsStore()
const pasteKey = 'abc123'

onMounted(async () => {
  await revisionsStore.fetchRevisions(pasteKey)
})

const createRevision = async () => {
  await revisionsStore.createRevision(pasteKey, {
    commitMessage: 'Updated function',
    diffContent: '...'
  })
}
</script>

<template>
  <div>
    <div v-for="revision in revisionsStore.revisions" :key="revision.id">
      {{ revision.commitMessage }} - {{ revision.createdAt }}
    </div>
  </div>
</template>
```

**Collaboration Store**
```vue
<script setup lang="ts">
import { useCollaborationStore } from '@/stores/collaboration'

const collabStore = useCollaborationStore()

const startSession = async () => {
  const session = await collabStore.createSession({
    pasteKey: 'abc123',
    maxParticipants: 5
  })

  if (session) {
    collabStore.connectWebSocket(session.id)
  }
}

const updateCursor = async (line: number, column: number) => {
  await collabStore.updateCursor({
    userId: 'user123',
    userDisplayName: 'John Doe',
    userColor: '#FF5733',
    cursorLine: line,
    cursorColumn: column
  })
}
</script>
```

**Analytics Store**
```vue
<script setup lang="ts">
import { useAnalyticsStore } from '@/stores/analytics'
import { onMounted, onUnmounted } from 'vue'

const analyticsStore = useAnalyticsStore()
const pasteKey = 'abc123'
let startTime: number

onMounted(async () => {
  startTime = Date.now()
  await analyticsStore.fetchAnalytics(pasteKey)
  await analyticsStore.fetchTimeline(pasteKey, 30)
})

onUnmounted(() => {
  const timeSpent = Math.floor((Date.now() - startTime) / 1000)
  analyticsStore.trackView(pasteKey, { timeSpent })
})
</script>

<template>
  <div v-if="analyticsStore.analytics">
    <p>Total Views: {{ analyticsStore.analytics.totalViews }}</p>
    <p>Unique Views: {{ analyticsStore.analytics.uniqueViews }}</p>
    <p>Trending Score: {{ analyticsStore.analytics.trendingScore }}</p>
  </div>
</template>
```

**Social Store**
```vue
<script setup lang="ts">
import { useSocialStore } from '@/stores/social'
import { ref } from 'vue'

const socialStore = useSocialStore()
const pasteKey = 'abc123'
const commentText = ref('')

const loadComments = async () => {
  await socialStore.fetchComments(pasteKey)
}

const addComment = async () => {
  if (commentText.value) {
    await socialStore.addComment(pasteKey, {
      content: commentText.value
    })
    commentText.value = ''
  }
}

const followUser = async (userId: string) => {
  await socialStore.followUser(userId)
}
</script>

<template>
  <div>
    <textarea v-model="commentText" placeholder="Add a comment"></textarea>
    <button @click="addComment">Post Comment</button>

    <div v-for="comment in socialStore.comments" :key="comment.id">
      <p>{{ comment.content }}</p>
      <small>by {{ comment.userId }} at {{ comment.createdAt }}</small>
    </div>
  </div>
</template>
```

## Environment Variables

### Required
```env
DATABASE_DRIVER=mysql
DATABASE_HOST=localhost
DATABASE_NAME=pastely
DATABASE_USER=root
DATABASE_PASSWORD=password
```

### Optional but Recommended
```env
# Redis for caching and performance
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_POOL_MAX=128

# Elasticsearch for search
ELASTICSEARCH_URL=http://localhost:9200
ELASTICSEARCH_USER=elastic
ELASTICSEARCH_PASSWORD=changeme

# MinIO/S3 for file storage
MINIO_SERVER=localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=pastely

# AI features (optional)
AI_ANTHROPIC_TOKEN=your_token

# Rate limiting
RATELIMITER_MILLIS=5000
RATELIMITER_LIMIT=5
```

## Testing the Implementation

### 1. Test Version Control
```bash
# Create a paste
PASTE_KEY=$(curl -X POST http://localhost/api/v2/paste -H "Authorization: Bearer $API_KEY" -d '{"content":"test"}' | jq -r '.key')

# Create a revision
curl -X POST http://localhost/api/v2/paste/$PASTE_KEY/revisions \
  -H "Authorization: Bearer $API_KEY" \
  -d '{"commitMessage":"Initial commit","diffContent":"..."}'

# List revisions
curl http://localhost/api/v2/paste/$PASTE_KEY/revisions -H "Authorization: Bearer $API_KEY"
```

### 2. Test Security
```bash
# Set password
curl -X POST http://localhost/api/v2/paste/$PASTE_KEY/security/access \
  -H "Authorization: Bearer $API_KEY" \
  -d '{"password":"test123","maxViews":5}'

# Verify access
curl -X POST http://localhost/api/v2/paste/$PASTE_KEY/security/verify \
  -d '{"password":"test123"}'
```

### 3. Test Analytics
```bash
# Track views
for i in {1..10}; do
  curl -X POST http://localhost/api/v2/analytics/paste/$PASTE_KEY/view \
    -d '{"timeSpent":60}'
done

# Get analytics
curl http://localhost/api/v2/analytics/paste/$PASTE_KEY -H "Authorization: Bearer $API_KEY"
```

## Troubleshooting

### Database Issues
```bash
# Check if tables were created
mysql -u root -p pastely -e "SHOW TABLES LIKE 'pastefy_%';"
```

### Connection Issues
```bash
# Check if server is running
curl http://localhost/api/v2/app/info

# Check logs
tail -f logs/pastefy.log
```

### Redis Connection
```bash
# Test Redis
redis-cli PING
```

### Elasticsearch Connection
```bash
# Test Elasticsearch
curl http://localhost:9200/_cluster/health
```

## Next Steps

1. **Explore the API**: Use the endpoints to create, manage, and share pastes
2. **Build Frontend**: Create Vue components using the provided stores
3. **Set up Webhooks**: Integrate with external services
4. **Configure Analytics**: Set up dashboards for your pastes
5. **Customize**: Extend with your own features

## Resources

- Full Documentation: `PASTELY_FEATURES.md`
- Implementation Details: `IMPLEMENTATION_SUMMARY.md`
- API Reference: See feature documentation
- Original Pastefy Docs: https://docs.pastefy.app

## Support

For issues and questions:
1. Check the documentation files
2. Review the implementation summary
3. Examine the code examples
4. Open an issue on GitHub

## Contributing

Contributions welcome! Areas to contribute:
- Frontend Vue components
- Additional features
- Tests
- Documentation
- Bug fixes

---

**Happy Coding with Pastely 7.0!** 🚀

### 9. AI-Powered Features

**Check AI Status**
```bash
curl http://localhost/api/v2/ai/status
```

**Explain Code**
```bash
curl -X POST http://localhost/api/v2/ai/explain \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "function hello() { console.log(\"Hello World\"); }",
    "language": "javascript"
  }'
```

**Detect Bugs**
```bash
curl -X POST http://localhost/api/v2/ai/detect-bugs \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "def divide(a, b):\n    return a / b",
    "language": "python"
  }'
```

**Generate Tags**
```bash
curl -X POST http://localhost/api/v2/ai/generate-tags \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "React Hook Example",
    "content": "import { useState } from \"react\"...",
    "language": "typescript"
  }'
```

**Translate Code**
```bash
curl -X POST http://localhost/api/v2/ai/translate \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "function add(a, b) { return a + b; }",
    "fromLanguage": "javascript",
    "toLanguage": "python"
  }'
```

**Analyze Code Quality**
```bash
curl -X POST http://localhost/api/v2/ai/quality \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "public class Example { ... }",
    "language": "java"
  }'
```

**Generate Documentation**
```bash
curl -X POST http://localhost/api/v2/ai/docs \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "function calculate(x, y) { return x * y; }",
    "language": "javascript",
    "format": "markdown"
  }'
```

**Suggest Improvements**
```bash
curl -X POST http://localhost/api/v2/ai/improve \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "var x = 1; var y = 2; console.log(x + y);",
    "language": "javascript"
  }'
```

All AI features require:
- J.A.R.V.I.S Gateway configured in `.env`
- Valid API authentication
- Active internet connection to J.A.R.V.I.S Gateway

