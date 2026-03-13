<script setup lang="ts">
import { ref, computed } from 'vue'
import { client } from '@/utils/client'
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import Card from 'primevue/card'
import Tag from 'primevue/tag'
import Message from 'primevue/message'
import Select from 'primevue/select'
import Accordion from 'primevue/accordion'
import AccordionPanel from 'primevue/accordionpanel'

type BugType = 'BUG' | 'SECURITY' | 'SMELL' | 'PERFORMANCE'
type Severity = 'HIGH' | 'MEDIUM' | 'LOW'

type Bug = {
  id: string
  type: BugType
  severity: Severity
  line: number
  description: string
  details: string
  suggestion: string
}

type BugDetectionResponse = {
  bugs: Bug[]
  total_count: number
}

const props = defineProps<{
  pasteId?: string
  initialCode?: string
}>()

const emit = defineEmits<{
  bugsDetected: [bugs: Bug[]]
}>()

const code = ref(props.initialCode || '')
const bugs = ref<Bug[]>([])
const isLoading = ref(false)
const error = ref<string | null>(null)
const selectedType = ref<BugType | 'ALL'>('ALL')
const selectedSeverity = ref<Severity | 'ALL'>('ALL')
const highlightedLine = ref<number | null>(null)

const bugTypes = [
  { label: 'All Types', value: 'ALL' },
  { label: 'Bug', value: 'BUG' },
  { label: 'Security', value: 'SECURITY' },
  { label: 'Code Smell', value: 'SMELL' },
  { label: 'Performance', value: 'PERFORMANCE' },
]

const severityLevels = [
  { label: 'All Severities', value: 'ALL' },
  { label: 'High', value: 'HIGH' },
  { label: 'Medium', value: 'MEDIUM' },
  { label: 'Low', value: 'LOW' },
]

const filteredBugs = computed(() => {
  return bugs.value.filter(bug => {
    const typeMatch = selectedType.value === 'ALL' || bug.type === selectedType.value
    const severityMatch = selectedSeverity.value === 'ALL' || bug.severity === selectedSeverity.value
    return typeMatch && severityMatch
  })
})

const canDetect = computed(() => code.value.trim().length > 0)

const bugStats = computed(() => {
  const stats = {
    total: bugs.value.length,
    high: bugs.value.filter(b => b.severity === 'HIGH').length,
    medium: bugs.value.filter(b => b.severity === 'MEDIUM').length,
    low: bugs.value.filter(b => b.severity === 'LOW').length,
  }
  return stats
})

function getBugTypeSeverity(type: BugType): 'danger' | 'warn' | 'info' | 'secondary' {
  switch (type) {
    case 'SECURITY':
      return 'danger'
    case 'BUG':
      return 'warn'
    case 'PERFORMANCE':
      return 'info'
    case 'SMELL':
      return 'secondary'
  }
}

function getSeverityClass(severity: Severity): string {
  switch (severity) {
    case 'HIGH':
      return 'text-red-600 dark:text-red-400'
    case 'MEDIUM':
      return 'text-yellow-600 dark:text-yellow-400'
    case 'LOW':
      return 'text-blue-600 dark:text-blue-400'
  }
}

async function detectBugs() {
  if (!canDetect.value) return

  isLoading.value = true
  error.value = null
  bugs.value = []

  try {
    const response = await client.post<BugDetectionResponse>('/api/v2/ai/detect-bugs', {
      code: code.value,
      paste_id: props.pasteId
    })

    bugs.value = response.data.bugs
    emit('bugsDetected', bugs.value)
  } catch (e) {
    error.value = 'Failed to detect bugs. Please try again.'
    console.error('Bug detection error:', e)
  } finally {
    isLoading.value = false
  }
}

function highlightLine(line: number) {
  highlightedLine.value = line
}

function exportBugs() {
  const dataStr = JSON.stringify(bugs.value, null, 2)
  const dataUri = 'data:application/json;charset=utf-8,' + encodeURIComponent(dataStr)
  const exportFileDefaultName = 'bugs-report.json'

  const linkElement = document.createElement('a')
  linkElement.setAttribute('href', dataUri)
  linkElement.setAttribute('download', exportFileDefaultName)
  linkElement.click()
}
</script>

<template>
  <Card class="bug-detector">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Bug Detector</span>
        <i class="pi pi-search text-red-500"></i>
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div class="flex flex-col">
          <label for="code-input" class="mb-2 text-sm font-medium">
            Code to Analyze
          </label>
          <Textarea
            id="code-input"
            v-model="code"
            :disabled="isLoading"
            rows="12"
            placeholder="Paste your code here..."
            class="font-mono text-sm"
          />
        </div>

        <Button
          label="Detect Bugs"
          icon="pi pi-search"
          :loading="isLoading"
          :disabled="!canDetect"
          @click="detectBugs"
        />

        <Message v-if="error" severity="error" :closable="false">
          {{ error }}
        </Message>

        <div v-if="bugs.length > 0" class="results-section">
          <div class="mb-4 flex items-center justify-between">
            <div class="flex gap-4">
              <div class="text-sm">
                <span class="font-semibold">{{ bugStats.total }}</span> issues found
              </div>
              <div class="flex gap-2 text-sm">
                <span class="text-red-600 dark:text-red-400">
                  <i class="pi pi-exclamation-circle"></i> {{ bugStats.high }} High
                </span>
                <span class="text-yellow-600 dark:text-yellow-400">
                  <i class="pi pi-exclamation-triangle"></i> {{ bugStats.medium }} Medium
                </span>
                <span class="text-blue-600 dark:text-blue-400">
                  <i class="pi pi-info-circle"></i> {{ bugStats.low }} Low
                </span>
              </div>
            </div>

            <Button
              label="Export JSON"
              icon="pi pi-download"
              size="small"
              text
              @click="exportBugs"
            />
          </div>

          <div class="mb-4 grid grid-cols-1 gap-2 md:grid-cols-2">
            <Select
              v-model="selectedType"
              :options="bugTypes"
              option-label="label"
              option-value="value"
              placeholder="Filter by type"
            />
            <Select
              v-model="selectedSeverity"
              :options="severityLevels"
              option-label="label"
              option-value="value"
              placeholder="Filter by severity"
            />
          </div>

          <Accordion :multiple="true">
            <AccordionPanel
              v-for="bug in filteredBugs"
              :key="bug.id"
            >
              <template #header>
                <div class="flex w-full items-center justify-between">
                  <div class="flex items-center gap-2">
                    <Tag :severity="getBugTypeSeverity(bug.type)">
                      {{ bug.type }}
                    </Tag>
                    <span
                      class="cursor-pointer font-mono text-sm hover:underline"
                      @click.stop="highlightLine(bug.line)"
                    >
                      Line {{ bug.line }}
                    </span>
                    <span class="text-sm">{{ bug.description }}</span>
                  </div>
                  <span :class="['text-xs font-semibold', getSeverityClass(bug.severity)]">
                    {{ bug.severity }}
                  </span>
                </div>
              </template>

              <div class="space-y-3">
                <div>
                  <h4 class="mb-1 text-sm font-semibold">Details</h4>
                  <p class="text-sm text-gray-700 dark:text-gray-300">
                    {{ bug.details }}
                  </p>
                </div>

                <div>
                  <h4 class="mb-1 text-sm font-semibold">Suggested Fix</h4>
                  <div class="rounded bg-gray-100 p-3 dark:bg-gray-800">
                    <code class="text-sm">{{ bug.suggestion }}</code>
                  </div>
                </div>
              </div>
            </AccordionPanel>
          </Accordion>

          <div v-if="filteredBugs.length === 0" class="text-center text-gray-500">
            No bugs match the current filters
          </div>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.bug-detector {
  width: 100%;
}

.results-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--surface-border);
}
</style>
