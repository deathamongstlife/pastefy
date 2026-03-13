<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { client } from '@/utils/client'
import Card from 'primevue/card'
import Timeline from 'primevue/timeline'
import Avatar from 'primevue/avatar'
import Tag from 'primevue/tag'
import Button from 'primevue/button'
import Select from 'primevue/select'
import Calendar from 'primevue/datepicker'

type Revision = {
  id: string
  commit_message: string
  author: {
    id: string
    username: string
    avatar_url?: string
  }
  branch: string
  created_at: string
  changes_count: number
  parent_revision_id?: string
}

const props = defineProps<{
  pasteId: string
}>()

const emit = defineEmits<{
  viewRevision: [revisionId: string]
  rollback: [revisionId: string]
}>()

const revisions = ref<Revision[]>([])
const isLoading = ref(false)
const selectedBranch = ref<string>('all')
const selectedAuthor = ref<string>('all')
const dateFilter = ref<Date | null>(null)

const branches = computed(() => {
  const branchSet = new Set(revisions.value.map(r => r.branch))
  return [
    { label: 'All Branches', value: 'all' },
    ...Array.from(branchSet).map(b => ({ label: b, value: b }))
  ]
})

const authors = computed(() => {
  const authorMap = new Map<string, string>()
  revisions.value.forEach(r => {
    authorMap.set(r.author.id, r.author.username)
  })
  return [
    { label: 'All Authors', value: 'all' },
    ...Array.from(authorMap).map(([id, username]) => ({
      label: username,
      value: id
    }))
  ]
})

const filteredRevisions = computed(() => {
  return revisions.value.filter(revision => {
    const branchMatch = selectedBranch.value === 'all' || revision.branch === selectedBranch.value
    const authorMatch = selectedAuthor.value === 'all' || revision.author.id === selectedAuthor.value
    const dateMatch = !dateFilter.value || new Date(revision.created_at) >= dateFilter.value

    return branchMatch && authorMatch && dateMatch
  })
})

async function fetchRevisions() {
  isLoading.value = true
  try {
    const response = await client.get(`/api/v2/pastes/${props.pasteId}/revisions`)
    revisions.value = response.data.revisions
  } catch (e) {
    console.error('Failed to fetch revisions:', e)
  } finally {
    isLoading.value = false
  }
}

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)

  if (diffMins < 60) return `${diffMins} minutes ago`
  if (diffMins < 1440) return `${Math.floor(diffMins / 60)} hours ago`
  if (diffMins < 10080) return `${Math.floor(diffMins / 1440)} days ago`

  return date.toLocaleDateString()
}

function viewRevision(revisionId: string) {
  emit('viewRevision', revisionId)
}

function handleRollback(revisionId: string) {
  emit('rollback', revisionId)
}

onMounted(() => {
  fetchRevisions()
})
</script>

<template>
  <Card class="revision-timeline">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Revision History</span>
        <Button
          icon="pi pi-refresh"
          size="small"
          text
          :loading="isLoading"
          @click="fetchRevisions"
        />
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div class="filters grid grid-cols-1 gap-2 md:grid-cols-3">
          <Select
            v-model="selectedBranch"
            :options="branches"
            option-label="label"
            option-value="value"
            placeholder="Filter by branch"
          />
          <Select
            v-model="selectedAuthor"
            :options="authors"
            option-label="label"
            option-value="value"
            placeholder="Filter by author"
          />
          <Calendar
            v-model="dateFilter"
            placeholder="Filter by date"
            show-icon
            :show-button-bar="true"
          />
        </div>

        <div v-if="filteredRevisions.length === 0" class="text-center text-gray-500">
          No revisions found
        </div>

        <Timeline v-else :value="filteredRevisions" class="customized-timeline">
          <template #marker="{ item }">
            <Avatar
              :image="item.author.avatar_url"
              :label="item.author.username[0].toUpperCase()"
              shape="circle"
            />
          </template>

          <template #content="{ item }">
            <Card class="revision-card">
              <template #content>
                <div class="flex flex-col gap-2">
                  <div class="flex items-start justify-between">
                    <div class="flex-1">
                      <div class="mb-1 flex items-center gap-2">
                        <h4 class="font-semibold">{{ item.commit_message }}</h4>
                        <Tag severity="info" :value="item.branch" />
                      </div>
                      <div class="text-sm text-gray-600 dark:text-gray-400">
                        by <span class="font-medium">{{ item.author.username }}</span>
                        · {{ formatDate(item.created_at) }}
                        · {{ item.changes_count }} change(s)
                      </div>
                    </div>
                  </div>

                  <div class="flex gap-2">
                    <Button
                      label="View"
                      icon="pi pi-eye"
                      size="small"
                      text
                      @click="viewRevision(item.id)"
                    />
                    <Button
                      label="Rollback"
                      icon="pi pi-history"
                      size="small"
                      severity="warn"
                      text
                      @click="handleRollback(item.id)"
                    />
                  </div>
                </div>
              </template>
            </Card>
          </template>
        </Timeline>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.revision-timeline {
  width: 100%;
}

.revision-card {
  margin-top: 0.5rem;
}

.customized-timeline :deep(.p-timeline-event-content) {
  padding-left: 1rem;
}
</style>
