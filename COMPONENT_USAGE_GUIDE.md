# Pastely Component Usage Guide

Quick reference for using all implemented Vue 3 components in your Pastely application.

## 🤖 AI Features

### CodeExplainer

Explains code using AI with syntax highlighting.

```vue
<script setup lang="ts">
import CodeExplainer from '@/components/ai/CodeExplainer.vue'

function handleExplained(explanation: string) {
  console.log('Explanation:', explanation)
}
</script>

<template>
  <CodeExplainer
    paste-id="abc123"
    :initial-code="myCode"
    @explained="handleExplained"
  />
</template>
```

**Props:**
- `pasteId?: string` - Optional paste ID
- `initialCode?: string` - Pre-filled code

**Events:**
- `explained: (explanation: string)` - Emitted when code is explained

---

### BugDetector

Detects and categorizes bugs in code.

```vue
<script setup lang="ts">
import BugDetector from '@/components/ai/BugDetector.vue'
import type { Bug } from '@/types/components'

function handleBugs(bugs: Bug[]) {
  console.log(`Found ${bugs.length} bugs`)
}
</script>

<template>
  <BugDetector
    paste-id="abc123"
    :initial-code="myCode"
    @bugs-detected="handleBugs"
  />
</template>
```

**Props:**
- `pasteId?: string`
- `initialCode?: string`

**Events:**
- `bugsDetected: (bugs: Bug[])` - Emitted when bugs are found

---

### CodeTranslator

Translates code between programming languages.

```vue
<script setup lang="ts">
import CodeTranslator from '@/components/ai/CodeTranslator.vue'

function handleTranslation(code: string, language: string) {
  console.log(`Translated to ${language}:`, code)
}
</script>

<template>
  <CodeTranslator
    paste-id="abc123"
    :initial-code="sourceCode"
    initial-language="javascript"
    @translated="handleTranslation"
  />
</template>
```

**Props:**
- `pasteId?: string`
- `initialCode?: string`
- `initialLanguage?: string`

**Events:**
- `translated: (code: string, language: string)`

---

### TagGenerator

Generates relevant tags for code using AI.

```vue
<script setup lang="ts">
import TagGenerator from '@/components/ai/TagGenerator.vue'

const myTags = ['javascript', 'react']

function handleTagsApplied(tags: string[]) {
  console.log('Tags applied:', tags)
}
</script>

<template>
  <TagGenerator
    paste-id="abc123"
    :code="myCode"
    :existing-tags="myTags"
    @tags-applied="handleTagsApplied"
  />
</template>
```

**Props:**
- `pasteId: string` - Required paste ID
- `code: string` - Code to analyze
- `existingTags?: string[]`

**Events:**
- `tagsGenerated: (tags: string[])`
- `tagsApplied: (tags: string[])`

---

### QualityAnalyzer

Analyzes code quality with metrics and suggestions.

```vue
<script setup lang="ts">
import QualityAnalyzer from '@/components/ai/QualityAnalyzer.vue'
import type { QualityAnalysis } from '@/types/components'

function handleAnalysis(analysis: QualityAnalysis) {
  console.log('Quality score:', analysis.overall_score)
}
</script>

<template>
  <QualityAnalyzer
    paste-id="abc123"
    :initial-code="myCode"
    @analyzed="handleAnalysis"
  />
</template>
```

**Props:**
- `pasteId?: string`
- `initialCode?: string`

**Events:**
- `analyzed: (analysis: QualityAnalysis)`

---

### CodeImprover

Suggests and applies code improvements.

```vue
<script setup lang="ts">
import CodeImprover from '@/components/ai/CodeImprover.vue'

function handleApplied(improvedCode: string) {
  myCode.value = improvedCode
}
</script>

<template>
  <CodeImprover
    paste-id="abc123"
    :initial-code="myCode"
    @applied="handleApplied"
  />
</template>
```

**Props:**
- `pasteId?: string`
- `initialCode?: string`

**Events:**
- `improved: (code: string)`
- `applied: (code: string)`

---

### DocumentationGenerator

Generates documentation from code.

```vue
<script setup lang="ts">
import DocumentationGenerator from '@/components/ai/DocumentationGenerator.vue'

function handleExport(format: string) {
  console.log('Exported as:', format)
}
</script>

<template>
  <DocumentationGenerator
    paste-id="abc123"
    :initial-code="myCode"
    @exported="handleExport"
  />
</template>
```

**Props:**
- `pasteId?: string`
- `initialCode?: string`

**Events:**
- `generated: (documentation: string)`
- `exported: (format: string)`

---

### AIStatusIndicator

Shows real-time AI service status.

```vue
<script setup lang="ts">
import AIStatusIndicator from '@/components/ai/AIStatusIndicator.vue'
</script>

<template>
  <AIStatusIndicator />
</template>
```

**Props:** None

**Events:** None (Auto-updates every 30 seconds)

---

## 📝 Version Control

### RevisionTimeline

Shows revision history as a timeline.

```vue
<script setup lang="ts">
import RevisionTimeline from '@/components/revisions/RevisionTimeline.vue'

function viewRevision(revisionId: string) {
  router.push(`/revisions/${revisionId}`)
}

function rollback(revisionId: string) {
  // Handle rollback
}
</script>

<template>
  <RevisionTimeline
    paste-id="abc123"
    @view-revision="viewRevision"
    @rollback="rollback"
  />
</template>
```

**Props:**
- `pasteId: string`

**Events:**
- `viewRevision: (revisionId: string)`
- `rollback: (revisionId: string)`

---

### RevisionViewer

Displays detailed revision with diff view.

```vue
<script setup lang="ts">
import RevisionViewer from '@/components/revisions/RevisionViewer.vue'

function handleClose() {
  router.back()
}
</script>

<template>
  <RevisionViewer
    paste-id="abc123"
    revision-id="rev456"
    @close="handleClose"
  />
</template>
```

**Props:**
- `pasteId: string`
- `revisionId: string`

**Events:**
- `rollback: (revisionId: string)`
- `download: (revisionId: string)`
- `close: ()`

---

### BranchManager

Manages branches (create, merge, delete).

```vue
<script setup lang="ts">
import BranchManager from '@/components/revisions/BranchManager.vue'
import type { Branch } from '@/types/components'

function handleBranchCreated(branch: Branch) {
  console.log('New branch:', branch.name)
}
</script>

<template>
  <BranchManager
    paste-id="abc123"
    @branch-created="handleBranchCreated"
    @branch-merged="handleMerged"
    @branch-deleted="handleDeleted"
  />
</template>
```

**Props:**
- `pasteId: string`

**Events:**
- `branchCreated: (branch: Branch)`
- `branchMerged: (source: string, target: string)`
- `branchDeleted: (branchName: string)`
- `branchSwitched: (branchName: string)`

---

## 👥 Collaboration

### CollaborationPanel

Shows active collaborators and their presence.

```vue
<script setup lang="ts">
import CollaborationPanel from '@/components/collaboration/CollaborationPanel.vue'
import type { CollaborationUser } from '@/types/components'

const sessionId = 'session-123'

function handleUserJoined(user: CollaborationUser) {
  console.log('User joined:', user.username)
}
</script>

<template>
  <CollaborationPanel
    paste-id="abc123"
    :session-id="sessionId"
    @user-joined="handleUserJoined"
    @user-left="handleUserLeft"
  />
</template>
```

**Props:**
- `pasteId: string`
- `sessionId: string`

**Events:**
- `userJoined: (user: CollaborationUser)`
- `userLeft: (userId: string)`
- `inviteSent: (email: string)`

---

### CursorOverlay

Displays real-time cursor positions.

```vue
<script setup lang="ts">
import CursorOverlay from '@/components/collaboration/CursorOverlay.vue'
import type { CollaborationUser } from '@/types/components'

const users = ref<CollaborationUser[]>([])
const editorRef = ref<HTMLElement | null>(null)
</script>

<template>
  <div ref="editorRef" class="editor">
    <CursorOverlay
      :users="users"
      :container-element="editorRef"
    />
  </div>
</template>
```

**Props:**
- `users: CollaborationUser[]`
- `containerElement: HTMLElement | null`

**Events:** None

---

### CollaborationChat

Real-time chat for collaborators.

```vue
<script setup lang="ts">
import CollaborationChat from '@/components/collaboration/CollaborationChat.vue'

const sessionId = 'session-123'
</script>

<template>
  <CollaborationChat
    :session-id="sessionId"
  />
</template>
```

**Props:**
- `sessionId: string`

**Events:** None (Messages handled internally)

---

## 🔒 Security

### AccessControlPanel

Configure access restrictions for pastes.

```vue
<script setup lang="ts">
import AccessControlPanel from '@/components/security/AccessControlPanel.vue'
import type { AccessSettings } from '@/components/security/AccessControlPanel.vue'

const initialSettings = {
  password_protected: true,
  password: 'secret123',
  ip_whitelist: ['192.168.1.1'],
  ip_blacklist: [],
  max_views: 100,
  expire_at: '2024-12-31T23:59:59Z'
}

function handleSaved(settings: AccessSettings) {
  console.log('Settings saved:', settings)
}
</script>

<template>
  <AccessControlPanel
    paste-id="abc123"
    :initial-settings="initialSettings"
    @saved="handleSaved"
  />
</template>
```

**Props:**
- `pasteId: string`
- `initialSettings?: AccessSettings`

**Events:**
- `saved: (settings: AccessSettings)`

---

### AccessLogsViewer

View and filter access logs.

```vue
<script setup lang="ts">
import AccessLogsViewer from '@/components/security/AccessLogsViewer.vue'
</script>

<template>
  <AccessLogsViewer
    paste-id="abc123"
  />
</template>
```

**Props:**
- `pasteId: string`

**Events:** None (Export handled internally)

---

### BurnAfterReadIndicator

Warning for pastes that self-destruct.

```vue
<script setup lang="ts">
import BurnAfterReadIndicator from '@/components/security/BurnAfterReadIndicator.vue'

const viewsRemaining = ref(3)
const totalViews = ref(5)
</script>

<template>
  <BurnAfterReadIndicator
    :views-remaining="viewsRemaining"
    :total-views="totalViews"
  />
</template>
```

**Props:**
- `viewsRemaining: number`
- `totalViews: number`

**Events:** None

---

## 💬 Social

### CommentThread

Nested comment system with replies.

```vue
<script setup lang="ts">
import CommentThread from '@/components/social/CommentThread.vue'
import type { Comment } from '@/types/components'

function handleCommented(comment: Comment) {
  console.log('New comment:', comment.content)
}
</script>

<template>
  <CommentThread
    paste-id="abc123"
    @commented="handleCommented"
  />
</template>
```

**Props:**
- `pasteId: string`

**Events:**
- `commented: (comment: Comment)`

---

## 🔔 Notifications

### NotificationBell

Notification bell with dropdown.

```vue
<script setup lang="ts">
import NotificationBell from '@/components/notifications/NotificationBell.vue'
</script>

<template>
  <NotificationBell />
</template>
```

**Props:** None

**Events:** None (Navigation handled internally)

---

## 🎨 Styling

All components use:
- **Tailwind CSS** for utility classes
- **PrimeVue** for UI components
- **Dark mode** support via `dark:` prefix

### Common Tailwind Patterns

```html
<!-- Container -->
<div class="mx-auto w-full max-w-[1200px] px-4">

<!-- Flex Layout -->
<div class="flex items-center justify-between gap-4">

<!-- Grid Layout -->
<div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">

<!-- Spacing -->
<div class="mb-4 mt-8 space-y-4">

<!-- Typography -->
<h1 class="text-3xl font-bold text-gray-900 dark:text-white">

<!-- Buttons via PrimeVue -->
<Button label="Action" icon="pi pi-check" />
```

---

## 📦 Importing Types

```typescript
import type {
  Bug,
  Improvement,
  QualityAnalysis,
  Revision,
  Branch,
  CollaborationUser,
  ChatMessage,
  AccessLog,
  UserNotification,
  Comment
} from '@/types/components'
```

---

## 🔧 Common Patterns

### Loading State

```vue
<script setup lang="ts">
const isLoading = ref(false)

async function fetchData() {
  isLoading.value = true
  try {
    // API call
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div v-if="isLoading" class="text-center">
    <i class="pi pi-spin pi-spinner text-4xl"></i>
  </div>
</template>
```

### Error Handling

```vue
<script setup lang="ts">
const error = ref<string | null>(null)

async function doSomething() {
  error.value = null
  try {
    // API call
  } catch (e) {
    error.value = 'Something went wrong'
  }
}
</script>

<template>
  <Message v-if="error" severity="error" :closable="true" @close="error = null">
    {{ error }}
  </Message>
</template>
```

### API Calls

```typescript
import { client } from '@/utils/client'

// GET
const response = await client.get('/api/v2/endpoint')
const data = response.data

// POST
const response = await client.post('/api/v2/endpoint', {
  key: 'value'
})

// PUT
await client.put('/api/v2/endpoint', payload)

// DELETE
await client.delete('/api/v2/endpoint')
```

---

## 📱 Responsive Design

All components are mobile-first with breakpoints:

- `sm:` - 640px and up
- `md:` - 768px and up
- `lg:` - 1024px and up
- `xl:` - 1280px and up

Example:
```html
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
```

---

## ♿ Accessibility

All components include:
- ARIA labels
- Keyboard navigation
- Focus management
- Screen reader support

Example:
```html
<Button
  aria-label="Close dialog"
  @click="close"
/>
```

---

## 🎯 Best Practices

1. **Always handle loading states**
2. **Always handle error states**
3. **Use TypeScript types for all props/events**
4. **Emit events for parent communication**
5. **Use computed for derived state**
6. **Clean up in onUnmounted when needed**
7. **Test dark mode appearance**
8. **Test responsive behavior**

---

## 🚀 Performance Tips

```vue
<!-- Use v-show for frequent toggles -->
<div v-show="isVisible">Content</div>

<!-- Use v-if for conditional rendering -->
<div v-if="shouldRender">Content</div>

<!-- Use v-once for static content -->
<div v-once>{{ staticText }}</div>

<!-- Use computed for expensive operations -->
const filtered = computed(() => {
  return items.value.filter(/* expensive filter */)
})
```

---

## 📚 Additional Resources

- [Vue 3 Documentation](https://vuejs.org/)
- [PrimeVue Components](https://primevue.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)

---

For complete implementation details, see:
- `COMPONENTS_FINAL_SUMMARY.md` - Full component list
- `CLAUDE.md` - Coding standards
- `frontend/src/types/components.ts` - Type definitions
