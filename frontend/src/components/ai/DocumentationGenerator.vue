<script setup lang="ts">
import { ref, computed } from 'vue'
import { client } from '@/utils/client'
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import Card from 'primevue/card'
import Message from 'primevue/message'
import Select from 'primevue/select'

type DocStyle = 'jsdoc' | 'javadoc' | 'pydoc' | 'rustdoc' | 'godoc'

const props = defineProps<{
  pasteId?: string
  initialCode?: string
}>()

const emit = defineEmits<{
  generated: [documentation: string]
  exported: [format: string]
}>()

const docStyles = [
  { label: 'JSDoc', value: 'jsdoc' },
  { label: 'JavaDoc', value: 'javadoc' },
  { label: 'PyDoc', value: 'pydoc' },
  { label: 'RustDoc', value: 'rustdoc' },
  { label: 'GoDoc', value: 'godoc' },
]

const code = ref(props.initialCode || '')
const documentation = ref('')
const selectedStyle = ref<DocStyle>('jsdoc')
const isLoading = ref(false)
const isEditing = ref(false)
const error = ref<string | null>(null)
const copied = ref(false)

const canGenerate = computed(() => code.value.trim().length > 0)
const hasDocumentation = computed(() => documentation.value.length > 0)

async function generateDocs() {
  if (!canGenerate.value) return

  isLoading.value = true
  error.value = null

  try {
    const response = await client.post('/api/v2/ai/generate-docs', {
      code: code.value,
      style: selectedStyle.value,
      paste_id: props.pasteId
    })

    documentation.value = response.data.documentation
    isEditing.value = false
    emit('generated', documentation.value)
  } catch (e) {
    error.value = 'Failed to generate documentation. Please try again.'
    console.error('Documentation generation error:', e)
  } finally {
    isLoading.value = false
  }
}

async function copyToClipboard() {
  try {
    await navigator.clipboard.writeText(documentation.value)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch (e) {
    console.error('Copy failed:', e)
  }
}

function exportAsMarkdown() {
  const blob = new Blob([documentation.value], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'documentation.md'
  link.click()
  URL.revokeObjectURL(url)
  emit('exported', 'markdown')
}

function clearDocumentation() {
  documentation.value = ''
  error.value = null
  isEditing.value = false
}

function toggleEdit() {
  isEditing.value = !isEditing.value
}
</script>

<template>
  <Card class="documentation-generator">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Documentation Generator</span>
        <i class="pi pi-book text-blue-500"></i>
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div class="flex flex-col">
            <label for="code-input" class="mb-2 text-sm font-medium">
              Code to Document
            </label>
            <Textarea
              id="code-input"
              v-model="code"
              :disabled="isLoading"
              rows="14"
              placeholder="Paste your code here..."
              class="font-mono text-sm"
            />
          </div>

          <div class="flex flex-col">
            <label for="style-select" class="mb-2 text-sm font-medium">
              Documentation Style
            </label>
            <Select
              id="style-select"
              v-model="selectedStyle"
              :options="docStyles"
              option-label="label"
              option-value="value"
              placeholder="Select style"
              :disabled="isLoading"
            />
          </div>
        </div>

        <div class="flex gap-2">
          <Button
            label="Generate Documentation"
            icon="pi pi-file-edit"
            :loading="isLoading"
            :disabled="!canGenerate"
            @click="generateDocs"
          />
          <Button
            v-if="hasDocumentation"
            label="Clear"
            icon="pi pi-times"
            severity="secondary"
            outlined
            @click="clearDocumentation"
          />
        </div>

        <Message v-if="error" severity="error" :closable="false">
          {{ error }}
        </Message>

        <div v-if="hasDocumentation" class="documentation-result">
          <div class="mb-2 flex items-center justify-between">
            <h3 class="text-lg font-semibold">Generated Documentation</h3>
            <div class="flex gap-2">
              <Button
                :icon="isEditing ? 'pi pi-eye' : 'pi pi-pencil'"
                :label="isEditing ? 'Preview' : 'Edit'"
                size="small"
                text
                @click="toggleEdit"
              />
              <Button
                :icon="copied ? 'pi pi-check' : 'pi pi-copy'"
                :label="copied ? 'Copied!' : 'Copy'"
                size="small"
                text
                @click="copyToClipboard"
              />
              <Button
                icon="pi pi-download"
                label="Export MD"
                size="small"
                text
                @click="exportAsMarkdown"
              />
            </div>
          </div>

          <Card>
            <template #content>
              <div v-if="isEditing">
                <Textarea
                  v-model="documentation"
                  rows="20"
                  class="font-mono text-sm"
                />
              </div>
              <div v-else class="prose dark:prose-invert max-w-none">
                <pre class="whitespace-pre-wrap font-mono text-sm">{{ documentation }}</pre>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.documentation-generator {
  width: 100%;
}

.documentation-result {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--surface-border);
}
</style>
