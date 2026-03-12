# CLAUDE.md - Pastely Coding Standards

This document defines the coding standards, conventions, and architectural patterns for the Pastely project.

## Project Overview

**Technology Stack:**
- **Backend**: Java 17 with JavaWebStack HTTP Router and Undertow server
- **Frontend**: Vue 3 (Composition API) with TypeScript and Vite
- **Database**: MySQL/SQLite with JavaWebStack ORM
- **Caching**: Redis (optional)
- **Search**: Elasticsearch (optional)
- **Storage**: Database, MinIO (S3-compatible), or HTTP
- **UI**: PrimeVue 4 with Tailwind CSS 4

---

## Backend Standards (Java)

### Framework & Architecture

#### JavaWebStack HTTP Router
- Use `@PathPrefix` for route grouping
- HTTP method annotations: `@Get`, `@Post`, `@Put`, `@Delete`, `@Patch`
- Middleware chain with `@With({"middleware-name"})`
- All controllers extend `HttpController`

#### Controller Patterns

```java
@PathPrefix("/api/v2/resource")
public class ResourceController extends HttpController {

    @Get("/{id}")
    @With({"auth"})
    public ResourceResponse get(@Path("id") String id,
                               @Attrib("user") User user,
                               Exchange exchange) {
        // Implementation
        return ResourceResponse.create(resource);
    }

    @Post
    @With({"auth", "rate-limiter"})
    public ActionResponse create(@Body CreateResourceRequest request,
                                 @Attrib("user") User user) {
        // Implementation
        return new ActionResponse(true);
    }
}
```

**Key Conventions:**
- Method parameters use annotations: `@Path`, `@Body`, `@Attrib`
- Return response objects, not primitives
- Use `Exchange` object for complex request/response handling
- Apply middleware via `@With` annotation

#### Package Structure

```
de.interaapps.pastely/
├── Pastely.java               # Main singleton class
├── auth/                      # Authentication & OAuth2
│   ├── *Middleware.java       # Middleware classes
│   ├── Passport.java          # Auth orchestration
│   └── strategies/            # Auth strategies (OAuth2, etc.)
├── controller/                # HTTP controllers
│   ├── HttpController.java    # Base controller
│   ├── resource/              # Feature-grouped controllers
│   └── admin/                 # Admin-only controllers
├── exceptions/                # Custom exceptions
│   └── HTTPException.java     # Base HTTP exception
├── helper/                    # Helper utilities
├── model/                     # Data models
│   ├── database/              # Database entities
│   ├── requests/              # Request DTOs
│   ├── responses/             # Response DTOs
│   ├── queryparams/           # Query parameter models
│   ├── elastic/               # Elasticsearch models
│   ├── redis/                 # Redis cache models
│   └── plugins/               # Plugin system models
├── services/                  # Business logic services
└── cli/                       # CLI commands
```

### Database & ORM Patterns

#### Model Definition

```java
@Dates                                    // Auto-timestamps
@Table("resource_name")                   // Table name (prefix added)
public class Resource extends Model {

    @Column                               // Basic column
    private String id;

    @Column(size = 255)                   // VARCHAR with size
    private String name;

    @Column(size = 16777215)              // MEDIUMTEXT
    private String content;

    @Searchable                           // Elasticsearch indexable
    @Filterable                           // Query filterable
    @Column
    private String description;

    @Column
    private ResourceType type;            // Enums supported

    // Standard getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Lifecycle hooks
    @Override
    public void save() {
        if (id == null) {
            id = RandomUtil.string(8);    // 8-char random IDs
            createdAt = Timestamp.from(Instant.now());
        }
        updatedAt = Timestamp.from(Instant.now());
        super.save();

        // Async operations
        Pastely.getInstance().executeAsync(() -> {
            ElasticResource.store(this);
        });
    }

    @Override
    public void delete() {
        // Manual cascade delete
        Repo.get(RelatedModel.class).where("resourceId", id).delete();

        // Async cleanup
        Pastely.getInstance().executeAsync(() -> {
            ElasticResource.delete(this);
        });

        super.delete();
    }
}
```

**Key Conventions:**
- All models extend `Model`
- Use `@Dates` for automatic timestamp management
- 8-character random strings for primary keys
- Explicit getters/setters (Java beans)
- Override `save()` and `delete()` for lifecycle hooks
- Manual cascade deletes in `delete()` method
- Async operations for heavy tasks (Elasticsearch, etc.)

#### Repository Pattern

```java
// Query building
Repo.get(Resource.class)
    .where("userId", userId)
    .whereNotNull("expireAt")
    .whereLike("title", "%" + search + "%")
    .order("createdAt", true)              // true = DESC
    .limit(10)
    .offset(page * 10)
    .all();

// Single record
Repo.get(Resource.class)
    .where("key", key)
    .first();

// Count
long count = Repo.get(Resource.class)
    .where("userId", userId)
    .count();

// Delete
Repo.get(Resource.class)
    .where("userId", userId)
    .delete();

// Static helpers on models
Resource resource = Resource.get(key);
User user = User.get(userId);
```

#### Database Configuration

```java
// ORM setup with table prefix
ORMConfig ormConfig = new ORMConfig()
    .setTablePrefix("pastely_")
    .addTypeMapper(new AbstractDataTypeMapper());

ORM.register(Resource.class.getPackage(), sqlPool, ormConfig);

// Auto-migration
if (config.get("pastely.automigrate", "true").equals("true")) {
    ORM.autoMigrate();
}

// Connection pooling
SQLPool sqlPool = new SQLPool(
    new MinMaxScaler(
        config.getInt("database.pool.min", 20),
        config.getInt("database.pool.max", 50)
    ),
    factory
);
```

### Request/Response Models

#### Request DTOs

```java
public class CreateResourceRequest {
    @RequiredRule                         // Validation annotation
    public String name;

    public String description = "";       // Default values

    public ResourceType type = ResourceType.DEFAULT;

    public List<String> tags = null;      // Nullable lists

    public boolean enabled = false;
}
```

**Key Conventions:**
- Public fields for DTO simplicity
- Use validation annotations (`@RequiredRule`, etc.)
- Provide sensible defaults
- Suffix: `*Request`

#### Response DTOs

```java
public class ResourceResponse {
    public String id;
    public String name;

    @SerializedName("created_at")         // Gson snake_case
    public Timestamp createdAt;

    @SerializedName("raw_url")
    public String rawURL;

    // Factory method pattern
    public static ResourceResponse create(Resource resource, User user) {
        ResourceResponse response = new ResourceResponse();
        response.id = resource.getId();
        response.name = resource.getName();
        response.createdAt = resource.getCreatedAt();
        return response;
    }

    // Constructor for simple cases
    public ResourceResponse(Resource resource) {
        this.id = resource.getId();
        this.name = resource.getName();
    }
}
```

**Key Conventions:**
- Public fields for DTO simplicity
- Use `@SerializedName` for snake_case JSON keys
- Static factory methods for complex creation
- Suffix: `*Response`
- Include `exists` boolean for conditional responses

#### Standard Responses

```java
// Success/failure actions
public class ActionResponse {
    public boolean success;
    public String message;

    public ActionResponse(boolean success) {
        this.success = success;
    }
}

// Exception responses (auto-generated)
public class ExceptionResponse {
    public String error;
    public String message;

    public ExceptionResponse(Throwable throwable) {
        this.error = throwable.getClass().getSimpleName();
        this.message = throwable.getMessage();
    }
}
```

### Authentication & Middleware

#### Middleware Implementation

```java
public class AuthMiddleware implements RequestHandler {
    @Override
    public Object handle(Exchange exchange) {
        User user = exchange.attrib("user");
        if (user == null) {
            throw new AuthenticationException();
        }
        return null;  // null = continue to next handler
    }
}
```

#### Middleware Registration

```java
// In Pastely.setupServer()
httpRouter.middleware("auth", new AuthMiddleware());
httpRouter.middleware("admin", new AdminMiddleware());
httpRouter.middleware("awaiting-access", new AwaitingAccessMiddleware());
httpRouter.middleware("rate-limiter", new RateLimitMiddleware(rateLimiter));
```

#### Global Authentication Interceptor

```java
httpRouter.beforeInterceptor((exchange) -> {
    String authHeader = exchange.header("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        AuthKey authKey = Repo.get(AuthKey.class)
            .where("key", token)
            .first();

        if (authKey != null) {
            User user = authKey.getUser();
            exchange.attrib("user", user);
            exchange.attrib("authkey", authKey);
        }
    }
});
```

#### Permission Checking

```java
// In controller
@Attrib("authkey") AuthKey authKey

// Check permissions (any match)
authKey.checkPermission("resource:read", "resource:write");

// Check single permission (boolean)
if (authKey.hasPermission("resource:delete")) {
    // Allow
}

// Permission format: "resource:action"
// Examples: "pastes:create", "pastes:read", "admin:users"
```

### Exception Handling

#### Custom Exceptions

```java
public class HTTPException extends RuntimeException {
    public int status = 500;

    public HTTPException(int status, String message) {
        super(message);
        this.status = status;
    }
}

// Specific exceptions
public class AuthenticationException extends HTTPException {
    public AuthenticationException() {
        super(401, "Authentication required");
    }
}

public class NotFoundException extends HTTPException {
    public NotFoundException(String message) {
        super(404, message);
    }
}

public class PermissionsDeniedException extends HTTPException {
    public PermissionsDeniedException() {
        super(403, "Permission denied");
    }
}

public class FeatureDisabledException extends HTTPException {
    public FeatureDisabledException(String feature) {
        super(403, feature + " is disabled");
    }
}
```

#### Global Exception Handler

```java
httpRouter.exceptionHandler((exchange, throwable) -> {
    exchange.status(500);

    if (throwable instanceof HTTPException) {
        exchange.status(((HTTPException) throwable).status);
    }

    return new ExceptionResponse(throwable);
});
```

### Service Layer

**Pattern: Static Utility Services**

```java
public class ResourceService {

    public static List<ResourceResponse> getAll(Exchange exchange,
                                               ListQueryParameters params,
                                               User user) {
        return new ResourceQueryBuilder().get(exchange, params, user);
    }

    public static ResourceResponse create(CreateResourceRequest request,
                                         User user) {
        Resource resource = new Resource();
        resource.setName(request.name);
        resource.setUserId(user.getId());
        resource.save();
        return ResourceResponse.create(resource, user);
    }
}
```

**Key Conventions:**
- Static methods (utility-style)
- Thin layer over models and query builders
- Business logic primarily in models or controllers
- Use builder pattern for complex queries

### Configuration Management

#### Environment Variables

```java
// Mapping pattern
Mapping mapping = new Mapping()
    .map("DATABASE_DRIVER", "database.driver")
    .map("DATABASE_HOST", "database.host")
    .map("DATABASE_PORT", "database.port")
    .map("DATABASE_NAME", "database.name")
    .map("DATABASE_USER", "database.user")
    .map("DATABASE_PASSWORD", "database.password")
    .map("OAUTH2_GITHUB_CLIENT_ID", "oauth2.github.id")
    .map("OAUTH2_GITHUB_CLIENT_SECRET", "oauth2.github.secret");

// Load from .env file
config.add(new EnvFile(new File(".env")).withVariables(),
          new MappingTryout(mapping, Config::basicEnvMapping));
```

#### Configuration Access

```java
// String with default
String serverName = config.get("server.name", "http://localhost");

// Integer with default
int port = config.getInt("http.server.port", 80);

// Boolean check
boolean hasRedis = config.has("redis.host");

// Feature flags
boolean loginRequired = config.get("pastely.loginrequired", "false").equals("true");
```

### Async Operations

```java
private ExecutorService executor = Executors.newFixedThreadPool(30);

public void executeAsync(Runnable task) {
    executor.submit(task);
}

// Usage - async operations for heavy tasks
Pastely.getInstance().executeAsync(() -> {
    ElasticPaste.store(this);
    PublicPasteEngagement.updateScore(paste);
});
```

**When to Use Async:**
- Elasticsearch indexing
- MinIO/S3 uploads
- Redis cache updates
- Analytics tracking
- Email notifications
- Any I/O-heavy operation

### Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Controller | `*Controller` | `PasteController`, `UserController` |
| Middleware | `*Middleware` | `AuthMiddleware`, `RateLimitMiddleware` |
| Model | Singular noun | `Paste`, `User`, `Folder` |
| Request DTO | `Create*Request`, `Edit*Request` | `CreatePasteRequest` |
| Response DTO | `*Response` | `PasteResponse`, `ActionResponse` |
| Exception | `*Exception` | `AuthenticationException` |
| Service | `*Service` | `PasteService` |
| Helper | `*Helper` | `RequestHelper`, `MinioFolderHelper` |

---

## Frontend Standards (Vue 3 + TypeScript)

### Component Structure

#### Composition API Pattern

```vue
<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Resource } from '@/types/resource.ts'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import { client } from '@/utils/client.ts'

// Props
const props = defineProps<{
  resourceId: string
  editable?: boolean
}>()

// Emits
const emit = defineEmits<{
  save: [resource: Resource]
  cancel: []
}>()

// Composables
const router = useRouter()
const currentUser = useCurrentUserStore()

// Reactive state
const resource = ref<Resource | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)

// Computed properties
const canEdit = computed(() => {
  return props.editable && resource.value?.user?.id === currentUser.user?.id
})

// Methods
async function fetchResource() {
  isLoading.value = true
  try {
    const response = await client.get(`/api/v2/resource/${props.resourceId}`)
    resource.value = response.data
  } catch (e) {
    error.value = 'Failed to load resource'
  } finally {
    isLoading.value = false
  }
}

// Lifecycle
onMounted(() => {
  fetchResource()
})

// Watchers
watch(() => props.resourceId, () => {
  fetchResource()
})
</script>

<template>
  <div class="resource-view">
    <div v-if="isLoading">Loading...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else-if="resource">
      <!-- Component content -->
    </div>
  </div>
</template>
```

**Key Conventions:**
- Use `<script setup>` with TypeScript
- Group code logically: props → emits → composables → state → computed → methods → lifecycle
- Prefer `ref` over `reactive` for primitives
- Type all props, emits, and refs
- Use async/await for API calls
- Handle loading and error states

### File Structure

```
src/
├── App.vue                    # Root component
├── main.ts                    # Application entry
├── router/
│   └── index.ts              # Route definitions
├── stores/
│   ├── current-user.ts       # Pinia stores (kebab-case)
│   └── app-info.ts
├── views/
│   ├── HomeView.vue          # Page components (PascalCase + View)
│   └── ResourceView.vue
├── components/
│   ├── forms/
│   │   └── CreateResource.vue
│   ├── layout/
│   │   └── Sidebar.vue
│   ├── modals/
│   │   └── ConfirmModal.vue
│   └── cards/
│       └── ResourceCard.vue
├── types/
│   ├── resource.ts           # Type definitions (kebab-case)
│   └── user.ts
├── utils/
│   ├── client.ts             # Axios instance
│   └── highlight.ts          # Utility functions
├── composables/
│   └── config.ts             # Composable functions
└── assets/
    └── styles/               # Global styles
```

### State Management (Pinia)

```typescript
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { User } from '@/types/user.ts'
import { client } from '@/utils/client.ts'

export const useCurrentUserStore = defineStore('current-user', () => {
  // State
  const user = ref<User | undefined>(undefined)
  const isLoading = ref(false)

  // Getters
  const isLoggedIn = computed(() => user.value !== undefined)
  const isAdmin = computed(() => user.value?.type === 'ADMIN')

  // Actions
  async function fetchUser() {
    isLoading.value = true
    try {
      const response = await client.get('/api/v2/user')
      if (response.data.logged_in) {
        user.value = response.data
      }
    } catch (error) {
      console.error('Failed to fetch user:', error)
    } finally {
      isLoading.value = false
    }
  }

  function logout() {
    user.value = undefined
    // Clear tokens, etc.
  }

  return {
    user,
    isLoading,
    isLoggedIn,
    isAdmin,
    fetchUser,
    logout
  }
})
```

**Key Conventions:**
- Use composition API style (`defineStore` with setup function)
- Group: state → getters → actions
- Prefix store files with feature name
- Use kebab-case for store IDs
- Export typed store: `useCurrentUserStore`

### TypeScript Types

```typescript
// types/resource.ts
export type ResourceType = 'DOCUMENT' | 'CODE' | 'IMAGE'
export type ResourceVisibility = 'PUBLIC' | 'PRIVATE' | 'UNLISTED'

export type Resource = {
  id: string
  title: string
  content: string
  type: ResourceType
  visibility: ResourceVisibility
  created_at: string
  updated_at: string
  user?: PublicUser
  tags?: string[]
  starred?: boolean
}

export type PublicUser = {
  id: string
  name: string
  username: string
}

export type CreateResourceRequest = {
  title: string
  content: string
  type: ResourceType
  visibility: ResourceVisibility
  tags?: string[]
}
```

**Key Conventions:**
- Use `type` over `interface`
- Export all types
- Match backend field names (snake_case from API)
- Use union types for enums
- Separate request/response types when needed

### API Client

```typescript
// utils/client.ts
import axios from 'axios'
import { useConfig } from '@/composables/config'

const config = useConfig()

export const client = axios.create({
  baseURL: (import.meta.env.VITE_APP_BASE_URL as string) || undefined,
})

// Request interceptor for auth
client.interceptors.request.use((config) => {
  const apiKey = useConfig().value.apiKey
  if (apiKey) {
    config.headers.Authorization = `Bearer ${apiKey}`
  }
  return config
})

// Response interceptor for JSON parsing
client.interceptors.response.use((response) => {
  if (typeof response.data === 'string') {
    try {
      response.data = JSON.parse(response.data)
    } catch (e) {
      // Keep as string if not JSON
    }
  }
  return response
})
```

**Usage:**

```typescript
// GET request
const response = await client.get('/api/v2/resource')
const resources = response.data

// POST request with body
const response = await client.post('/api/v2/resource', {
  title: 'New Resource',
  content: 'Content here'
})

// Error handling
try {
  await client.delete(`/api/v2/resource/${id}`)
} catch (error) {
  if (axios.isAxiosError(error)) {
    console.error(error.response?.data.message)
  }
}
```

### Styling with Tailwind

```vue
<template>
  <!-- Container pattern -->
  <div class="mx-auto w-full max-w-[1200px] px-4">

    <!-- Flex layout -->
    <div class="flex items-center justify-between">
      <h1 class="text-3xl font-bold">Title</h1>
      <Button label="Action" />
    </div>

    <!-- Grid layout -->
    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
      <Card v-for="item in items" :key="item.id" />
    </div>

    <!-- Spacing -->
    <div class="mb-5 mt-8">
      <p class="text-sm text-gray-600 dark:text-gray-400">
        Description text
      </p>
    </div>

  </div>
</template>
```

**Key Patterns:**
- Max width containers: `max-w-[1200px]`
- Responsive design: `md:`, `lg:` prefixes
- Dark mode: `dark:` prefix for all color utilities
- Spacing scale: use Tailwind's spacing (mb-4, mt-8, etc.)
- Flexbox for layouts: `flex items-center justify-between`
- Grid for card layouts: `grid grid-cols-*`

### PrimeVue Components

```vue
<template>
  <!-- Button variants -->
  <Button
    label="Primary"
    @click="handleClick"
  />

  <Button
    label="Secondary"
    severity="secondary"
    outlined
  />

  <Button
    label="Small"
    size="small"
    text
  />

  <!-- Form inputs -->
  <InputText
    v-model="title"
    placeholder="Enter title"
  />

  <Textarea
    v-model="content"
    rows="10"
  />

  <Select
    v-model="selectedType"
    :options="types"
    placeholder="Select type"
  />

  <!-- Cards -->
  <Card>
    <template #title>Card Title</template>
    <template #content>
      Card content here
    </template>
  </Card>

  <!-- Dialogs -->
  <Dialog
    v-model:visible="showDialog"
    header="Dialog Title"
    :modal="true"
  >
    Dialog content
  </Dialog>
</template>
```

**Key Conventions:**
- Use PrimeVue for all UI components (buttons, inputs, dialogs)
- Customize with severity and size props
- Use v-model for two-way binding
- Leverage slots for complex content

### Router Patterns

```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/views/layouts/MainLayout.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/HomeView.vue'),
        },
        {
          path: '/resource/:id',
          name: 'resource',
          component: () => import('@/views/ResourceView.vue'),
        },
      ]
    },
    {
      path: '/admin',
      component: () => import('@/views/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        // Admin routes
      ]
    }
  ]
})

// Navigation guards
router.beforeEach(async (to, from) => {
  const currentUser = useCurrentUserStore()

  if (to.meta.requiresAuth && !currentUser.isLoggedIn) {
    return { name: 'home' }
  }

  if (to.meta.requiresAdmin && !currentUser.isAdmin) {
    return { name: 'home' }
  }
})

export default router
```

**Key Conventions:**
- Lazy load all route components
- Use nested routes with layouts
- Meta fields for route guards
- Named routes for type-safe navigation

### Composables

```typescript
// composables/config.ts
import { useLocalStorage } from '@vueuse/core'

export type Config = {
  apiKey?: string
  animations: boolean
  sideBarShown: boolean
  theme: 'light' | 'dark' | 'auto'
}

export const useConfig = () => {
  return useLocalStorage<Config>('config', {
    apiKey: undefined,
    animations: true,
    sideBarShown: true,
    theme: 'auto'
  })
}
```

**Key Conventions:**
- Use VueUse for common composables
- Export typed composable functions
- Prefix with `use*`
- Keep composables focused and reusable

### Event Bus

```typescript
// utils/event-bus.ts
import mitt, { type Emitter } from 'mitt'

export type Events = {
  'resource:created': string
  'resource:updated': string
  'resource:deleted': string
  'user:login': void
  'user:logout': void
}

export const eventBus = mitt<Events>()

// Usage
eventBus.emit('resource:created', resourceId)
eventBus.on('resource:created', (id) => {
  console.log('Resource created:', id)
})
```

**Key Conventions:**
- Type all events
- Use colon-separated namespacing
- Keep event names descriptive
- Clean up listeners in `onUnmounted`

### Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Component | PascalCase | `CreateResource.vue`, `ResourceCard.vue` |
| View | PascalCase + View | `HomeView.vue`, `ResourceView.vue` |
| Store | kebab-case | `current-user.ts`, `resource-list.ts` |
| Type file | kebab-case | `resource.ts`, `api-response.ts` |
| Composable | camelCase + use prefix | `useConfig()`, `useAuth()` |
| Util function | camelCase | `formatDate()`, `highlightCode()` |

---

## General Patterns

### ID Generation
- Use 8-character random alphanumeric strings: `RandomUtil.string(8)`
- Consistent across all resources (pastes, folders, etc.)

### Timestamps
- Use `@Dates` annotation on models for auto-management
- Field names: `createdAt`, `updatedAt` (camelCase in Java, snake_case in JSON)
- Type: `java.sql.Timestamp`

### Enums
- Use enums for type-safe constants
- Backend: Java enums
- Frontend: TypeScript union types
- Match enum names between backend and frontend

### Pagination
- Query parameters: `page` (0-indexed), `limit` (default 10)
- Response: Include `total`, `page`, `limit` fields
- Backend: Use `offset = page * limit`

### Error Handling
- Backend: Throw typed exceptions
- Frontend: Try-catch with user-friendly messages
- Always provide error context (what failed, why)

### Async Operations
- Backend: Use `ExecutorService` for heavy I/O
- Frontend: Use async/await with loading states
- Never block the main thread

### Feature Flags
- Configure via environment variables
- Graceful degradation when disabled
- Check at runtime, not compile time

### Security
- Validate all user input
- Check permissions before operations
- Use parameterized queries (ORM handles this)
- Hash sensitive data
- Rate limit API endpoints

---

## Code Style

### Java
- **Formatting**: 4-space indentation, K&R brace style
- **Naming**: camelCase for variables/methods, PascalCase for classes
- **Comments**: Javadoc for public APIs, inline for complex logic
- **Imports**: No wildcards, organize by package

### TypeScript
- **Formatting**: 2-space indentation, Prettier-compliant
- **Naming**: camelCase for variables/functions, PascalCase for types/components
- **Comments**: JSDoc for complex functions, inline sparingly
- **Imports**: Organize by external → internal → types

### Vue
- **Component order**: `<script setup>` → `<template>` → `<style>` (if needed)
- **Template**: Use directives (v-if, v-for), avoid complex expressions
- **Scoped CSS**: Only when component-specific styles needed

---

## Testing

### Backend Testing
- Unit tests: Test business logic in services
- Integration tests: Test API endpoints
- Use JUnit 5 and Mockito

### Frontend Testing
- Unit tests: Test composables and utilities
- Component tests: Test component behavior with Vitest
- E2E tests: Critical user flows with Playwright

---

## Documentation

### Code Comments
- **DO**: Explain WHY, not WHAT
- **DO**: Document complex algorithms
- **DO**: Add TODO/FIXME with context
- **DON'T**: State the obvious
- **DON'T**: Leave outdated comments

### API Documentation
- Document all public endpoints
- Include request/response examples
- Specify auth requirements
- List possible error responses

---

## Version Control

### Commit Messages
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code restructuring
- `docs`: Documentation
- `style`: Formatting
- `test`: Tests
- `chore`: Build/tooling

**Examples:**
```
feat(paste): add syntax highlighting for Rust

Adds Rust language support to the code editor with proper
syntax highlighting and theme integration.

Closes #123
```

### Branch Strategy
- `master`: Production-ready code
- `develop`: Integration branch
- `feature/*`: New features
- `fix/*`: Bug fixes
- `hotfix/*`: Production hotfixes

---

## Performance Guidelines

### Backend
- Use connection pooling (database, Redis)
- Async operations for I/O
- Cache frequently accessed data (Redis)
- Index searchable fields (Elasticsearch)
- Paginate large result sets

### Frontend
- Lazy load routes and components
- Debounce search inputs
- Virtual scrolling for long lists
- Optimize images (WebP, lazy loading)
- Code splitting

---

## Accessibility

- Use semantic HTML
- ARIA labels for interactive elements
- Keyboard navigation support
- Color contrast compliance (WCAG AA)
- Screen reader testing

---

## Deployment

### Backend
- Java 17+ required
- Set environment variables via `.env`
- Use auto-migration for databases
- Configure Redis/Elasticsearch if available

### Frontend
- Build: `npm run build`
- Output: `dist/` directory
- Serve from backend's `static/` directory
- Environment: `VITE_APP_BASE_URL` for API

---

## Plugin System

### Backend Plugins
```java
public abstract class PastelyBackendPlugin {
    PastelyBackendPlugin(Pastely pastely) {}
    void setupRoutes(HTTPRouter router);
}
```

### Frontend Plugins
```typescript
// Exposed via window.pastely
window.pastely = {
  app: VueApp,
  router: Router,
  client: AxiosInstance,
  eventBus: Emitter<Events>,
  // ... more APIs
}
```

---

## Additional Resources

- **Backend Framework**: [JavaWebStack Docs](https://javawebstack.org/)
- **Frontend Framework**: [Vue 3 Docs](https://vuejs.org/)
- **UI Components**: [PrimeVue Docs](https://primevue.org/)
- **Styling**: [Tailwind CSS Docs](https://tailwindcss.com/)

---

**Last Updated**: 2026-03-12
