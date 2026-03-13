# Pastely WebSocket Real-Time Collaboration

This document describes the WebSocket-based real-time collaboration system in Pastely.

## Overview

Pastely uses WebSockets to enable real-time collaborative editing of pastes. Multiple users can simultaneously edit the same paste and see each other's changes and cursor positions in real-time.

## Architecture

### Backend Components

1. **CollaborationWebSocketHandler** (`backend/src/main/java/cc/allyapps/pastely/websocket/CollaborationWebSocketHandler.java`)
   - Main WebSocket connection handler
   - Manages connections, rooms, and message routing
   - Handles join, cursor, edit, and ping messages
   - Automatic cleanup of idle rooms

2. **CollaborationRoom** (`backend/src/main/java/cc/allyapps/pastely/websocket/CollaborationRoom.java`)
   - Manages a single collaborative editing session
   - Tracks connected users and their cursors
   - Maintains operation history and document version
   - Broadcasts changes to all participants
   - Max 100 users per room
   - 30-minute idle timeout

3. **OperationalTransform** (`backend/src/main/java/cc/allyapps/pastely/websocket/OperationalTransform.java`)
   - Implements Operational Transform (OT) algorithm
   - Ensures conflict-free concurrent editing
   - Transforms operations against each other
   - Handles insert, delete, and replace operations

4. **WebSocketController** (`backend/src/main/java/cc/allyapps/pastely/controller/WebSocketController.java`)
   - REST API for WebSocket stats and health
   - GET `/api/v2/websocket/stats` - Active rooms and users
   - GET `/api/v2/websocket/health` - Health check

### Frontend Components

1. **CollaborationWebSocket** (`frontend/src/services/collaborationWebSocket.ts`)
   - WebSocket client service
   - Connection management with automatic reconnection
   - Heartbeat/ping-pong for connection monitoring
   - Exponential backoff for reconnection (max 5 attempts)

2. **useCollaboration** (`frontend/src/composables/useCollaboration.ts`)
   - Vue composable for collaboration features
   - Manages WebSocket connection lifecycle
   - Tracks connected users and cursors
   - Event handling for remote changes

3. **CollaborativeEditor** (`frontend/src/components/collaboration/CollaborativeEditor.vue`)
   - Collaborative text editor component
   - Real-time content synchronization
   - Visual indicators for other users
   - Connection status display

4. **CursorOverlay** (`frontend/src/components/collaboration/CursorOverlay.vue`)
   - Displays cursor positions of other users
   - Color-coded per user
   - Smooth cursor animations

## Protocol

### WebSocket Endpoint

```
ws://[host]/api/v2/ws/collaboration/{sessionId}
```

### Message Types

#### Client → Server

**Join Session**
```json
{
  "type": "join",
  "sessionId": "abc123",
  "userId": "user123",
  "pasteId": "paste456"
}
```

**Cursor Update**
```json
{
  "type": "cursor",
  "position": {
    "line": 10,
    "column": 5,
    "selection": {
      "start": { "line": 10, "column": 5 },
      "end": { "line": 10, "column": 15 }
    }
  }
}
```

**Edit Operation**
```json
{
  "type": "edit",
  "operation": {
    "type": "insert|delete|replace",
    "position": 123,
    "text": "code here",
    "version": 5
  }
}
```

**Heartbeat**
```json
{
  "type": "ping"
}
```

#### Server → Client

**User Joined**
```json
{
  "type": "user_joined",
  "user": {
    "id": "user123",
    "username": "john_doe",
    "name": "John Doe",
    "connectionId": "conn-uuid"
  },
  "users": [
    { "id": "user123", "username": "john_doe", ... },
    { "id": "user456", "username": "jane_doe", ... }
  ]
}
```

**User Left**
```json
{
  "type": "user_left",
  "userId": "user123"
}
```

**Cursor Update**
```json
{
  "type": "cursor_update",
  "userId": "user123",
  "position": {
    "line": 10,
    "column": 5
  }
}
```

**Edit Broadcast**
```json
{
  "type": "edit_broadcast",
  "userId": "user123",
  "operation": {
    "type": "insert",
    "position": 123,
    "text": "code",
    "version": 6
  }
}
```

**Heartbeat Response**
```json
{
  "type": "pong"
}
```

**Error**
```json
{
  "type": "error",
  "error": "Error message here"
}
```

## Operational Transform (OT)

The system uses Operational Transform to handle concurrent edits without conflicts.

### Algorithm

1. Each operation has a version number
2. When an operation arrives:
   - Transform it against all concurrent operations
   - Apply the transformed operation
   - Increment version number
   - Broadcast to other users

### Transformation Rules

**Insert vs Insert**
- If positions are different: Adjust position of later insert
- If positions are same: Use userId for tie-breaking

**Insert vs Delete**
- Adjust insert position based on delete location

**Delete vs Delete**
- Handle overlapping deletes by adjusting text range

**Replace**
- Treated as combined delete + insert

### Example

```
Initial: "Hello World"

User A: Insert "Beautiful " at position 6
User B: Delete "World" at position 6

After OT:
1. A's insert is applied: "Hello Beautiful World"
2. B's delete is transformed to position 15
3. Result: "Hello Beautiful "
```

## Configuration

### Environment Variables

```bash
# WebSocket Configuration
WEBSOCKET_ENABLED=true
WEBSOCKET_MAX_CONNECTIONS=1000
WEBSOCKET_IDLE_TIMEOUT_MS=300000      # 5 minutes
WEBSOCKET_HEARTBEAT_INTERVAL_MS=30000 # 30 seconds
```

### Frontend Configuration

No additional configuration needed. WebSocket URL is automatically determined from the current host.

## Performance Considerations

### Backend

1. **Connection Limits**
   - Max 1000 concurrent WebSocket connections (configurable)
   - Max 100 users per collaboration room
   - Automatic cleanup of idle connections

2. **Memory Management**
   - Operation history limited to last 1000 operations per room
   - Idle rooms cleaned up after 30 minutes
   - Periodic cleanup task runs every 1 minute

3. **Message Batching**
   - Cursor updates are throttled client-side
   - No server-side batching (real-time priority)

### Frontend

1. **Reconnection Strategy**
   - Exponential backoff: 1s, 2s, 4s, 8s, 16s, 30s (max)
   - Max 5 reconnection attempts
   - User notified on connection failure

2. **Cursor Updates**
   - Throttled to max 10 updates per second
   - Only sent on actual cursor movement

3. **Edit Operations**
   - Applied immediately for local edits
   - Remote edits queued to avoid race conditions

## Security

1. **Authentication**
   - WebSocket connections require valid user session
   - User ID verified on join
   - Paste access verified before joining room

2. **Authorization**
   - Users must have access to the paste
   - Edit operations validated against paste permissions

3. **Rate Limiting**
   - Per-connection message rate limiting
   - Automatic disconnect on abuse

4. **Data Validation**
   - All incoming messages validated
   - Invalid JSON rejected
   - Malformed operations ignored

## Monitoring

### Stats Endpoint

```bash
GET /api/v2/websocket/stats
Authorization: Bearer {token}
```

Response:
```json
{
  "active_rooms": 5,
  "total_users": 12,
  "rooms": [
    {
      "session_id": "session-123",
      "paste_id": "paste-456",
      "user_count": 3,
      "version": 42,
      "created_at": "2026-03-13T10:00:00Z",
      "last_activity": "2026-03-13T10:05:00Z"
    }
  ]
}
```

### Health Check

```bash
GET /api/v2/websocket/health
```

Response:
```json
{
  "success": true
}
```

### Logging

WebSocket events are logged to console:
- Connection established/closed
- User joined/left
- Room created/cleaned up
- Errors and warnings

Example logs:
```
WebSocket connection established: WebSocketChannel@12345
User john_doe joined room session-123 (total: 2)
User jane_doe left room session-123 (remaining: 1)
Cleaned up idle room: session-456
```

## Usage Example

### Backend

WebSocket is automatically registered in `Pastely.java`:

```java
httpRouter.ws("/api/v2/ws/collaboration/{sessionId}",
              new CollaborationWebSocketHandler());
```

### Frontend

```vue
<script setup lang="ts">
import { onMounted } from 'vue'
import { useCollaboration } from '@/composables/useCollaboration'

const sessionId = 'session-123'
const pasteId = 'paste-456'

const { users, cursors, isConnected, connect, sendEdit } = useCollaboration(sessionId, pasteId)

onMounted(async () => {
  await connect()
  console.log('Connected to collaboration session')
})
</script>

<template>
  <div>
    <p v-if="isConnected">Connected with {{ users.length }} users</p>
    <CollaborativeEditor
      :session-id="sessionId"
      :paste-id="pasteId"
      :initial-content="content"
      :editable="true"
    />
  </div>
</template>
```

## Troubleshooting

### Connection Issues

1. **WebSocket won't connect**
   - Check firewall/proxy settings
   - Verify WebSocket protocol (ws:// vs wss://)
   - Check browser console for errors

2. **Frequent disconnections**
   - Network instability
   - Check heartbeat interval
   - Review server logs for errors

3. **Changes not syncing**
   - Verify OT algorithm is working
   - Check operation version numbers
   - Review browser console for errors

### Performance Issues

1. **High memory usage**
   - Check number of active rooms
   - Verify idle room cleanup is running
   - Review operation history size

2. **Slow updates**
   - Check network latency
   - Review server CPU usage
   - Consider increasing thread pool size

### Debug Mode

Enable detailed logging:

```bash
PASTELY_DEV=true
```

This will log:
- All SQL queries
- WebSocket messages (sanitized)
- OT transformations
- Room state changes

## Future Enhancements

1. **Conflict Resolution UI**
   - Visual indicators for conflicts
   - Merge conflict resolution tools

2. **Presence Awareness**
   - Active typing indicators
   - User status (active/idle/away)

3. **Voice/Video Integration**
   - WebRTC for audio/video calls
   - Screen sharing

4. **Performance Optimizations**
   - Delta compression for large documents
   - Binary protocol for efficiency
   - Message batching

5. **Advanced Features**
   - Undo/redo across users
   - Commenting/annotations
   - Change tracking/history

## References

- [Operational Transform Explained](https://operational-transformation.github.io/)
- [WebSocket Protocol (RFC 6455)](https://tools.ietf.org/html/rfc6455)
- [Undertow WebSocket Documentation](https://undertow.io/undertow-docs/undertow-docs-2.0.0/index.html#websockets)
- [Vue 3 Composition API](https://vuejs.org/guide/extras/composition-api-faq.html)

## License

Same as Pastely project license.

---

**Last Updated**: 2026-03-13
**Version**: 7.1.0
