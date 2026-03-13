<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { client } from '@/utils/client'
import Card from 'primevue/card'
import Button from 'primevue/button'
import Message from 'primevue/message'
import Tag from 'primevue/tag'

type DiffLine = {
  line_number: number
  type: 'added' | 'removed' | 'unchanged'
  content: string
}

type RevisionData = {
  id: string
  commit_message: string
  author: {
    username: string
    avatar_url?: string
  }
  branch: string
  created_at: string
  content: string
  diff: DiffLine[]
  parent_content?: string
}

const props = defineProps<{
  pasteId: string
  revisionId: string
}>()

const emit = defineEmits<{
  rollback: [revisionId: string]
  download: [revisionId: string]
  close: []
}>()

const revision = ref<RevisionData | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)
const viewMode = ref<'diff' | 'current' | 'parent'>('diff')

const hasDiff = computed(() => {
  return revision.value?.diff && revision.value.diff.length > 0
})

async function fetchRevision() {
  isLoading.value = true
  error.value = null

  try {
    const response = await client.get<RevisionData>(
      `/api/v2/pastes/${props.pasteId}/revisions/${props.revisionId}`
    )
    revision.value = response.data
  } catch (e) {
    error.value = 'Failed to load revision details'
    console.error('Revision fetch error:', e)
  } finally {
    isLoading.value = false
  }
}

function getLineClass(type: string): string {
  switch (type) {
    case 'added':
      return 'bg-green-50 text-green-900 dark:bg-green-900/20 dark:text-green-300'
    case 'removed':
      return 'bg-red-50 text-red-900 dark:bg-red-900/20 dark:text-red-300'
    default:
      return ''
  }
}

function getLinePrefix(type: string): string {
  switch (type) {
    case 'added':
      return '+ '
    case 'removed':
      return '- '
    default:
      return '  '
  }
}

function handleRollback() {
  emit('rollback', props.revisionId)
}

function downloadRevision() {
  if (!revision.value) return

  const blob = new Blob([revision.value.content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `revision-${props.revisionId}.txt`
  link.click()
  URL.revokeObjectURL(url)

  emit('download', props.revisionId)
}

onMounted(() => {
  fetchRevision()
})
</script>

<template>
  <Card class="revision-viewer">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Revision Details</span>
        <Button
          icon="pi pi-times"
          text
          @click="emit('close')"
        />
      </div>
    </template>

    <template #content>
      <div v-if="isLoading" class="text-center">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <Message v-else-if="error" severity="error">
        {{ error }}
      </Message>

      <div v-else-if="revision" class="space-y-4">
        <div class="metadata rounded border border-gray-200 bg-gray-50 p-4 dark:border-gray-700 dark:bg-gray-900">
          <div class="mb-2 flex items-start justify-between">
            <h3 class="text-lg font-semibold">{{ revision.commit_message }}</h3>
            <Tag severity="info" :value="revision.branch" />
          </div>
          <div class="space-y-1 text-sm text-gray-600 dark:text-gray-400">
            <div>
              <span class="font-medium">Author:</span> {{ revision.author.username }}
            </div>
            <div>
              <span class="font-medium">Date:</span> {{ new Date(revision.created_at).toLocaleString() }}
            </div>
            <div>
              <span class="font-medium">Revision ID:</span> {{ revision.id }}
            </div>
          </div>
        </div>

        <div class="actions flex gap-2">
          <Button
            label="Download"
            icon="pi pi-download"
            outlined
            @click="downloadRevision"
          />
          <Button
            label="Rollback to This Version"
            icon="pi pi-history"
            severity="warn"
            @click="handleRollback"
          />
        </div>

        <div class="view-mode-selector">
          <div class="mb-2 flex gap-2">
            <Button
              :label="'Diff View'"
              :outlined="viewMode !== 'diff'"
              size="small"
              @click="viewMode = 'diff'"
            />
            <Button
              :label="'Current'"
              :outlined="viewMode !== 'current'"
              size="small"
              @click="viewMode = 'current'"
            />
            <Button
              v-if="revision.parent_content"
              :label="'Parent'"
              :outlined="viewMode !== 'parent'"
              size="small"
              @click="viewMode = 'parent'"
            />
          </div>

          <div class="content-view">
            <div v-if="viewMode === 'diff' && hasDiff" class="diff-view">
              <div class="overflow-x-auto rounded border border-gray-300 dark:border-gray-700">
                <div
                  v-for="(line, idx) in revision.diff"
                  :key="idx"
                  :class="['flex font-mono text-sm', getLineClass(line.type)]"
                >
                  <div class="w-16 flex-shrink-0 border-r border-gray-300 px-2 text-center text-gray-500 dark:border-gray-700">
                    {{ line.line_number }}
                  </div>
                  <div class="flex-1 px-3 py-1">
                    <span class="font-bold">{{ getLinePrefix(line.type) }}</span>{{ line.content }}
                  </div>
                </div>
              </div>
            </div>

            <div v-else-if="viewMode === 'current'">
              <pre class="overflow-x-auto rounded border border-gray-300 bg-white p-4 font-mono text-sm dark:border-gray-700 dark:bg-gray-950">{{ revision.content }}</pre>
            </div>

            <div v-else-if="viewMode === 'parent' && revision.parent_content">
              <pre class="overflow-x-auto rounded border border-gray-300 bg-white p-4 font-mono text-sm dark:border-gray-700 dark:bg-gray-950">{{ revision.parent_content }}</pre>
            </div>

            <div v-else class="text-center text-gray-500">
              No diff available
            </div>
          </div>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.revision-viewer {
  width: 100%;
}

.diff-view {
  max-height: 600px;
  overflow-y: auto;
}
</style>
