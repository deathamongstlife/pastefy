<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { client } from '@/utils/client'
import type { PasteRevision, PasteBranch } from '@/types/version-control'
import Timeline from 'primevue/timeline'
import Button from 'primevue/button'
import Select from 'primevue/select'
import Card from 'primevue/card'

const route = useRoute()
const router = useRouter()
const pasteKey = route.params.id as string

const branches = ref<PasteBranch[]>([])
const selectedBranch = ref<string>('main')
const history = ref<PasteRevision[]>([])
const isLoading = ref(false)

async function loadBranches() {
  try {
    const response = await client.get(`/api/v2/paste/${pasteKey}/branches`)
    branches.value = response.data

    const defaultBranch = branches.value.find((b) => b.is_default)
    if (defaultBranch) {
      selectedBranch.value = defaultBranch.name
    }

    await loadHistory()
  } catch (error) {
    console.error('Failed to load branches:', error)
  }
}

async function loadHistory() {
  isLoading.value = true
  try {
    const response = await client.get(
      `/api/v2/paste/${pasteKey}/branches/${selectedBranch.value}/history`
    )
    history.value = response.data
  } catch (error) {
    console.error('Failed to load history:', error)
  } finally {
    isLoading.value = false
  }
}

async function rollback(revisionId: string) {
  if (!confirm('Are you sure you want to rollback to this revision?')) return

  try {
    await client.post(`/api/v2/paste/${pasteKey}/rollback/${revisionId}`)
    alert('Rollback successful')
    loadHistory()
  } catch (error) {
    console.error('Failed to rollback:', error)
    alert('Rollback failed')
  }
}

function viewRevision(revisionId: string) {
  router.push(`/paste/${pasteKey}/revision/${revisionId}`)
}

function formatDate(dateString: string) {
  return new Date(dateString).toLocaleString()
}

onMounted(() => {
  loadBranches()
})
</script>

<template>
  <div class="min-h-screen bg-dark-bg p-8">
    <div class="mx-auto max-w-4xl">
      <Card class="cyber-card mb-8">
        <template #title>
          <h1 class="neon-text text-3xl font-bold text-cyber-blue">VERSION HISTORY</h1>
        </template>

        <template #content>
          <div class="mb-6 flex items-center gap-4">
            <label class="font-semibold text-cyber-blue">BRANCH:</label>
            <Select
              v-model="selectedBranch"
              :options="branches.map((b) => b.name)"
              @change="loadHistory"
              class="cyber-input w-48"
            />
          </div>

          <div v-if="isLoading" class="text-center text-text-secondary">Loading history...</div>

          <Timeline v-else-if="history.length > 0" :value="history" class="timeline-cyber">
            <template #content="{ item }">
              <div class="cyber-card mb-4 rounded-lg p-4">
                <div class="mb-2 flex items-center justify-between">
                  <span class="font-bold text-cyber-pink">v{{ item.revision_number }}</span>
                  <span class="text-sm text-text-muted">
                    {{ formatDate(item.created_at) }}
                  </span>
                </div>

                <p class="mb-2 text-text-primary">
                  {{ item.commit_message }}
                </p>

                <div class="flex items-center gap-4 text-sm">
                  <span class="text-text-secondary">by {{ item.author.name }}</span>

                  <Button
                    label="View"
                    size="small"
                    outlined
                    @click="viewRevision(item.id)"
                  />

                  <Button
                    label="Rollback"
                    size="small"
                    severity="danger"
                    outlined
                    @click="rollback(item.id)"
                  />
                </div>
              </div>
            </template>
          </Timeline>

          <div v-else class="text-center text-text-secondary">No revision history available</div>
        </template>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.timeline-cyber :deep(.p-timeline-event-connector) {
  background: linear-gradient(180deg, #8b00ff, #ff006e);
}

.timeline-cyber :deep(.p-timeline-event-marker) {
  background: #00f5ff;
  border-color: #00f5ff;
  box-shadow: 0 0 10px #00f5ff;
}

.cyber-card {
  background: rgba(13, 17, 23, 0.95);
  border: 1px solid #8b00ff;
  box-shadow: 0 0 20px rgba(139, 0, 255, 0.3);
}

.cyber-input {
  background: rgba(13, 17, 23, 0.8);
  border: 1px solid #00f5ff;
  color: #00f5ff;
}

.neon-text {
  text-shadow: 0 0 10px #00f5ff, 0 0 20px #00f5ff;
}
</style>
