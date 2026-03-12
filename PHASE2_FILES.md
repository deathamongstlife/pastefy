# Phase 2 Implementation - File Locations

This document lists all files created for Phase 2 implementation.

## Backend Files

### Models (Database Entities)
```
backend/src/main/java/cc/allyapps/pastely/model/database/
├── Collection.java                 # Collection model
├── CollectionPaste.java           # Collection-Paste junction
├── SmartFolder.java               # Smart folder model
├── UserFollow.java                # User follow relationship
├── PasteComment.java              # Paste comment with threading
├── UserActivity.java              # Activity tracking
├── UserProfile.java               # Extended user profiles
├── PasteView.java                 # View tracking
└── ViewAnalytics.java             # Aggregated analytics
```

### Controllers
```
backend/src/main/java/cc/allyapps/pastely/controller/
├── CollectionController.java      # Collection CRUD + paste management
├── SocialController.java          # Follow, feed, profiles
├── CommentController.java         # Comment CRUD + threading
├── AnalyticsController.java       # View tracking + analytics
└── BulkOperationsController.java  # Bulk operations
```

### Request DTOs
```
backend/src/main/java/cc/allyapps/pastely/model/requests/
├── CreateCollectionRequest.java
├── UpdateCollectionRequest.java
├── CreateCommentRequest.java
├── UpdateProfileRequest.java
├── BulkPasteRequest.java
└── BulkFolderRequest.java
```

### Response DTOs
```
backend/src/main/java/cc/allyapps/pastely/model/responses/
├── CollectionResponse.java
├── CommentResponse.java
├── ActivityResponse.java
├── UserProfileResponse.java
├── PasteAnalyticsResponse.java
├── UserStatsResponse.java
└── BulkOperationResponse.java
```

### Enhanced Existing Files
```
backend/src/main/java/cc/allyapps/pastely/model/database/
└── Folder.java                    # Added nested folder helpers
```

## Frontend Files

### TypeScript Types
```
frontend/src/types/
├── collection.ts                  # Collection types
├── comment.ts                     # Comment types
├── social.ts                      # Social feature types
├── analytics.ts                   # Analytics types
└── bulk.ts                        # Bulk operation types
```

### Components (Examples)
```
frontend/src/components/
├── collections/
│   └── CollectionCard.vue        # Collection display card
└── social/
    └── ActivityFeed.vue          # Activity timeline feed
```

## Documentation
```
PHASE2_IMPLEMENTATION.md           # Comprehensive implementation guide
PHASE2_FILES.md                   # This file
```

## API Endpoints Summary

### Collections
- GET    /api/v2/collection
- POST   /api/v2/collection
- GET    /api/v2/collection/{id}
- PUT    /api/v2/collection/{id}
- DELETE /api/v2/collection/{id}
- POST   /api/v2/collection/{id}/paste/{pasteKey}
- DELETE /api/v2/collection/{id}/paste/{pasteKey}
- GET    /api/v2/collection/{id}/pastes
- PUT    /api/v2/collection/{id}/reorder

### Social
- POST   /api/v2/social/follow/{userId}
- DELETE /api/v2/social/follow/{userId}
- GET    /api/v2/social/followers
- GET    /api/v2/social/followers/{userId}
- GET    /api/v2/social/following
- GET    /api/v2/social/following/{userId}
- GET    /api/v2/social/feed
- GET    /api/v2/social/profile/{userId}
- PUT    /api/v2/social/profile

### Comments
- GET    /api/v2/paste/{pasteKey}/comment
- POST   /api/v2/paste/{pasteKey}/comment
- GET    /api/v2/paste/{pasteKey}/comment/{commentId}
- PUT    /api/v2/paste/{pasteKey}/comment/{commentId}
- DELETE /api/v2/paste/{pasteKey}/comment/{commentId}
- GET    /api/v2/paste/{pasteKey}/comment/{commentId}/replies

### Analytics
- POST   /api/v2/analytics/track/{pasteKey}
- GET    /api/v2/analytics/paste/{pasteKey}
- GET    /api/v2/analytics/trending
- GET    /api/v2/analytics/user/{userId}/stats

### Bulk Operations
- POST   /api/v2/bulk/paste
- POST   /api/v2/bulk/folder

## Database Tables

New tables created (with pastely_ prefix):
- collections
- collection_pastes
- smart_folders
- user_follows
- paste_comments
- user_activities
- user_profiles
- paste_views
- view_analytics

## Controller Auto-Registration

Controllers are automatically registered via:
```java
httpRouter.controller(HttpController.class, AppController.class.getPackage());
```

All controllers in `cc.allyapps.pastely.controller` package are automatically discovered and registered.

## Key Features Implemented

### 1. Enhanced Organization
- Collections with custom icons/colors
- Sortable paste collections
- Smart folder foundation
- Enhanced nested folder navigation
- Folder depth and ancestry tracking

### 2. Social Features
- User following system
- Activity feed from followed users
- Extended user profiles
- Threaded comments on pastes
- Line-specific code comments
- Activity logging

### 3. Analytics & Tracking
- Detailed view tracking (IP, user agent, referer, geo)
- Daily analytics aggregation
- Unique view counting
- Top countries and referers
- Trending algorithm
- User statistics

### 4. Bulk Operations
- Batch delete pastes/folders
- Batch move operations
- Batch visibility changes
- Success/failure tracking

## Next Steps for Full Frontend Implementation

To complete the frontend, create these additional components:

### Collections
- CollectionList.vue - Grid of collections
- CollectionView.vue - Single collection page
- CreateCollectionDialog.vue - Creation form
- EditCollectionDialog.vue - Edit form
- AddPasteDialog.vue - Add paste to collection

### Social
- UserProfileView.vue - User profile page
- FollowersList.vue - Followers display
- FollowButton.vue - Follow/unfollow button
- ActivityCard.vue - Single activity item

### Comments
- CommentList.vue - Comment thread display
- CommentForm.vue - Comment creation form
- CommentItem.vue - Single comment display
- ReplyForm.vue - Reply to comment

### Analytics
- AnalyticsDashboard.vue - Main analytics page
- ViewChart.vue - View statistics chart
- TrendingList.vue - Trending pastes
- GeographicMap.vue - Geographic view distribution

### Bulk Operations
- BulkSelectToolbar.vue - Selection controls
- BulkActionsMenu.vue - Action dropdown
- BulkProgressDialog.vue - Operation progress

## Integration Points

### With Existing Features
- Collections integrate with existing paste system
- Comments integrate with paste viewing
- Analytics integrate with paste access
- Social features integrate with user system
- Bulk operations work with existing paste/folder management

### Authentication
- All endpoints respect existing auth middleware
- Login requirements follow pastely.loginrequired settings
- Permission checking uses existing User model

### Rate Limiting
- Creation endpoints use existing rate-limiter middleware
- Prevents abuse of comment/follow/collection creation

## Testing Commands

```bash
# Create collection
curl -X POST http://localhost/api/v2/collection \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Collection","description":"Test"}'

# Follow user
curl -X POST http://localhost/api/v2/social/follow/USER_ID \
  -H "Authorization: Bearer YOUR_TOKEN"

# Create comment
curl -X POST http://localhost/api/v2/paste/PASTE_KEY/comment \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Great paste!"}'

# Track view
curl -X POST http://localhost/api/v2/analytics/track/PASTE_KEY

# Get trending
curl http://localhost/api/v2/analytics/trending?limit=10
```
