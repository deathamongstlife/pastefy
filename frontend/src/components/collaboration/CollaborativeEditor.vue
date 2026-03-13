<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useCollaboration } from '@/composables/useCollaboration'
import type { TextOperation } from '@/services/collaborationWebSocket'
import { eventBus } from '@/utils/event-bus'
import Textarea from 'primevue/textarea'
import Badge from 'primevue/badge'

const props = defineProps<{
  sessionId: string
  pasteId: string
  initialContent: string
  editable?: boolean
}>()

const emit = defineEmits<{
  contentChange: [content: string]
}>()

const content = ref(props.initialContent)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const version = ref(0)
const isApplyingRemoteEdit = ref(false)

const {
  users,
  cursors,
  isConnected,
  connectionError,
  connect,
  sendCursorPosition,
  sendEdit,
  getOtherUsers
} = useCollaboration(props.sessionId, props.pasteId)

onMounted(async () => {
  await connect()

  // Listen for remote edits
  eventBus.on('collaboration:edit', handleRemoteEdit)
})

const handleInput = (event: Event) => {
  if (isApplyingRemoteEdit.value) {
    return
  }

  const target = event.target as HTMLTextAreaElement
  const newContent = target.value

  // Calculate operation from diff
  const operation = calculateOperation(content.value, newContent)
  if (operation) {
    operation.version = version.value
    sendEdit(operation)
    version.value++
  }

  content.value = newContent
  emit('contentChange', newContent)
}

const handleCursorMove = () => {
  if (!textareaRef.value) return

  const textarea = textareaRef.value
  const position = textarea.selectionStart
  const textBeforeCursor = content.value.substring(0, position)
  const lines = textBeforeCursor.split('\n')
  const line = lines.length
  const column = lines[lines.length - 1].length

  sendCursorPosition({
    line,
    column
  })
}

const handleRemoteEdit = (operation: TextOperation) => {
  if (!operation) return

  isApplyingRemoteEdit.value = true

  // Apply operation to local content
  const newContent = applyOperation(content.value, operation)
  content.value = newContent
  version.value = operation.version + 1
  emit('contentChange', newContent)

  // Wait for next tick before allowing local edits
  setTimeout(() => {
    isApplyingRemoteEdit.value = false
  }, 0)
}

const calculateOperation = (oldText: string, newText: string): TextOperation | null => {
  if (oldText === newText) return null

  // Simple diff algorithm
  let position = 0
  while (position < oldText.length && position < newText.length && oldText[position] === newText[position]) {
    position++
  }

  if (newText.length > oldText.length) {
    // Insert operation
    const insertedText = newText.substring(position, position + (newText.length - oldText.length))
    return {
      type: 'insert',
      position,
      text: insertedText,
      version: version.value
    }
  } else if (newText.length < oldText.length) {
    // Delete operation
    const deletedText = oldText.substring(position, position + (oldText.length - newText.length))
    return {
      type: 'delete',
      position,
      text: deletedText,
      version: version.value
    }
  } else {
    // Replace operation
    let endPosition = oldText.length - 1
    while (endPosition > position && oldText[endPosition] === newText[endPosition]) {
      endPosition--
    }
    const replacedText = newText.substring(position, endPosition + 1)
    return {
      type: 'replace',
      position,
      text: replacedText,
      version: version.value
    }
  }
}

const applyOperation = (text: string, operation: TextOperation): string => {
  const { type, position, text: opText } = operation

  try {
    switch (type) {
      case 'insert':
        return text.substring(0, position) + opText + text.substring(position)
      case 'delete':
        return text.substring(0, position) + text.substring(position + opText.length)
      case 'replace': {
        const end = position + opText.length
        return text.substring(0, position) + opText + text.substring(end)
      }
      default:
        return text
    }
  } catch (error) {
    console.error('Error applying operation:', error)
    return text
  }
}

watch(() => props.initialContent, (newContent) => {
  if (!isConnected.value) {
    content.value = newContent
  }
})
</script>

<template>
  <div class="collaborative-editor">
    <div v-if="connectionError" class="mb-4 rounded bg-red-100 p-3 text-sm text-red-800 dark:bg-red-900 dark:text-red-200">
      {{ connectionError }}
    </div>

    <div class="mb-3 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <Badge
          :value="isConnected ? 'Connected' : 'Disconnected'"
          :severity="isConnected ? 'success' : 'danger'"
        />
        <span v-if="getOtherUsers().length > 0" class="text-sm text-gray-600 dark:text-gray-400">
          {{ getOtherUsers().length }} other {{ getOtherUsers().length === 1 ? 'user' : 'users' }} editing
        </span>
      </div>

      <div v-if="getOtherUsers().length > 0" class="flex -space-x-2">
        <div
          v-for="user in getOtherUsers()"
          :key="user.connectionId"
          :title="user.username"
          class="flex h-8 w-8 items-center justify-center rounded-full bg-blue-500 text-sm font-medium text-white ring-2 ring-white dark:ring-gray-800"
        >
          {{ user.username.charAt(0).toUpperCase() }}
        </div>
      </div>
    </div>

    <div class="relative">
      <Textarea
        ref="textareaRef"
        v-model="content"
        :disabled="!editable"
        :rows="20"
        class="font-mono text-sm"
        @input="handleInput"
        @click="handleCursorMove"
        @keyup="handleCursorMove"
      />

      <!-- Cursor overlay would go here for advanced cursor display -->
    </div>
  </div>
</template>

<style scoped>
.collaborative-editor {
  width: 100%;
}
</style>
