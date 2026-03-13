# WebSocket Collaboration Quick Start

## Backend Setup

### 1. Configuration

Add to `.env`:
```bash
WEBSOCKET_ENABLED=true
WEBSOCKET_MAX_CONNECTIONS=1000
WEBSOCKET_IDLE_TIMEOUT_MS=300000
WEBSOCKET_HEARTBEAT_INTERVAL_MS=30000
```

### 2. WebSocket Endpoint

Automatically registered in `Pastely.java`:
```
ws://localhost:1337/api/v2/ws/collaboration/{sessionId}
```

### 3. Check Stats

```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:1337/api/v2/websocket/stats
```

## Frontend Usage

### 1. Install Dependencies

Already included in package.json:
- mitt (event bus)
- primevue (UI components)

### 2. Use in Component

```vue
<script setup lang="ts">
import { onMounted } from 'vue'
import CollaborativeEditor from '@/components/collaboration/CollaborativeEditor.vue'

const sessionId = 'unique-session-id'
const pasteId = 'paste-key'
const content = 'Initial content'

onMounted(() => {
  console.log('Collaboration ready')
})
</script>

<template>
  <CollaborativeEditor
    :session-id="sessionId"
    :paste-id="pasteId"
    :initial-content="content"
    :editable="true"
  />
</template>
```

### 3. Use Composable Directly

```typescript
import { onMounted } from 'vue'
import { useCollaboration } from '@/composables/useCollaboration'

const { users, isConnected, connect, sendEdit } = useCollaboration(sessionId, pasteId)

onMounted(async () => {
  await connect()
  console.log('Connected:', isConnected.value)
})
```

## Testing

### 1. Backend Test

```bash
cd backend
mvn test -Dtest=OperationalTransformTest
```

### 2. Frontend Test

Open in browser:
```
http://localhost:5173/websocket-test.html
```

Open in multiple tabs to test collaboration.

### 3. Manual Testing

1. Open paste in two browsers
2. Enable collaboration mode
3. Type in one browser
4. See changes appear in other browser

## Message Flow

```
Client 1                    Server                     Client 2
   |                          |                           |
   |--- join ---------------→ |                           |
   |← user_joined -------------|                          |
   |                          |← join -------------------|
   |← user_joined ------------|------ user_joined -----→|
   |                          |                           |
   |--- cursor -------------→ |                           |
   |                          |------ cursor_update ---→|
   |                          |                           |
   |--- edit ---------------→ |                           |
   |                          |------ edit_broadcast --→|
   |                          |                           |
```

## Common Operations

### Join Session
```json
{
  "type": "join",
  "sessionId": "abc123",
  "userId": "user1",
  "pasteId": "paste1"
}
```

### Send Edit
```json
{
  "type": "edit",
  "operation": {
    "type": "insert",
    "position": 10,
    "text": "Hello",
    "version": 5
  }
}
```

### Update Cursor
```json
{
  "type": "cursor",
  "position": {
    "line": 5,
    "column": 10
  }
}
```

## Troubleshooting

### Connection Failed
- Check WebSocket URL (ws:// not http://)
- Verify user is authenticated
- Check paste exists and is accessible

### Changes Not Syncing
- Check browser console for errors
- Verify operation versions
- Check network tab for WebSocket frames

### High CPU Usage
- Check number of active rooms
- Review operation history size
- Verify cleanup task is running

## Performance Tips

1. **Limit Room Size**: Max 100 users per room
2. **Throttle Cursor Updates**: Max 10/second
3. **Clean Up Idle Rooms**: Auto-cleanup after 30 min
4. **Monitor Stats**: Use `/api/v2/websocket/stats`

## Security Checklist

- [ ] User authenticated before joining
- [ ] Paste access verified
- [ ] Message validation enabled
- [ ] Rate limiting configured
- [ ] HTTPS/WSS in production

## Production Deployment

### Nginx Configuration

```nginx
location /api/v2/ws/ {
    proxy_pass http://localhost:1337;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_connect_timeout 7d;
    proxy_send_timeout 7d;
    proxy_read_timeout 7d;
}
```

### Environment Variables

```bash
WEBSOCKET_ENABLED=true
WEBSOCKET_MAX_CONNECTIONS=5000
WEBSOCKET_IDLE_TIMEOUT_MS=600000  # 10 minutes
```

## Monitoring

### Metrics to Track

- Active WebSocket connections
- Number of active rooms
- Total users in collaboration
- Messages per second
- Average operation latency

### Logging

Enable debug mode:
```bash
PASTELY_DEV=true
```

Logs include:
- Connection events
- User join/leave
- Operations applied
- Errors and warnings

## Next Steps

1. Read full documentation: `WEBSOCKET.md`
2. Review code examples
3. Run tests
4. Test with multiple users
5. Monitor performance

## Support

- Documentation: `WEBSOCKET.md`
- Issues: GitHub Issues
- Tests: `backend/src/test/java/cc/allyapps/pastely/websocket/`
