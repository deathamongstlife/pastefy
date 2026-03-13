<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { client } from '@/utils/client'
import type { AccessLog } from '@/types/components'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Button from 'primevue/button'
import Tag from 'primevue/tag'

const props = defineProps<{
  pasteId: string
}>()

const logs = ref<AccessLog[]>([])
const isLoading = ref(false)
const searchQuery = ref('')
const filterStatus = ref<'all' | 'granted' | 'denied'>('all')

const filteredLogs = computed(() => {
  return logs.value.filter(log => {
    const matchesSearch =
      log.ip_address.includes(searchQuery.value) ||
      log.user?.username.includes(searchQuery.value) ||
      log.action.includes(searchQuery.value)

    const matchesFilter =
      filterStatus.value === 'all' ||
      (filterStatus.value === 'granted' && log.granted) ||
      (filterStatus.value === 'denied' && !log.granted)

    return matchesSearch && matchesFilter
  })
})

async function fetchLogs() {
  isLoading.value = true
  try {
    const response = await client.get(`/api/v2/pastes/${props.pasteId}/access-logs`)
    logs.value = response.data.logs
  } catch (e) {
    console.error('Failed to fetch access logs:', e)
  } finally {
    isLoading.value = false
  }
}

function exportCSV() {
  const csv = [
    ['Timestamp', 'IP Address', 'User', 'Action', 'Status', 'Deny Reason'].join(','),
    ...filteredLogs.value.map(log => [
      log.timestamp,
      log.ip_address,
      log.user?.username || 'Anonymous',
      log.action,
      log.granted ? 'Granted' : 'Denied',
      log.deny_reason || ''
    ].join(','))
  ].join('\n')

  const blob = new Blob([csv], { type: 'text/csv' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `access-logs-${props.pasteId}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <Card class="access-logs-viewer">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Access Logs</span>
        <Button
          label="Export CSV"
          icon="pi pi-download"
          size="small"
          @click="exportCSV"
        />
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div class="flex gap-2">
          <InputText
            v-model="searchQuery"
            placeholder="Search by IP, user, or action..."
            class="flex-1"
          />
          <Select
            v-model="filterStatus"
            :options="[
              { label: 'All', value: 'all' },
              { label: 'Granted', value: 'granted' },
              { label: 'Denied', value: 'denied' }
            ]"
            option-label="label"
            option-value="value"
          />
        </div>

        <DataTable
          :value="filteredLogs"
          :loading="isLoading"
          paginator
          :rows="10"
          striped-rows
        >
          <Column field="timestamp" header="Timestamp">
            <template #body="{ data }">
              {{ new Date(data.timestamp).toLocaleString() }}
            </template>
          </Column>
          <Column field="ip_address" header="IP Address" />
          <Column field="user.username" header="User">
            <template #body="{ data }">
              {{ data.user?.username || 'Anonymous' }}
            </template>
          </Column>
          <Column field="action" header="Action" />
          <Column field="granted" header="Status">
            <template #body="{ data }">
              <Tag
                :severity="data.granted ? 'success' : 'danger'"
                :value="data.granted ? 'Granted' : 'Denied'"
              />
            </template>
          </Column>
          <Column field="deny_reason" header="Reason">
            <template #body="{ data }">
              <span v-if="data.deny_reason" class="text-sm">
                {{ data.deny_reason }}
              </span>
            </template>
          </Column>
        </DataTable>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.access-logs-viewer {
  width: 100%;
}
</style>
