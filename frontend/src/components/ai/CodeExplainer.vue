<script setup lang="ts">
import { ref, computed } from 'vue'
import { client } from '@/utils/client'
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import Select from 'primevue/select'
import Card from 'primevue/card'
import Message from 'primevue/message'

const props = defineProps<{
  pasteId?: string
  initialCode?: string
}>()

const emit = defineEmits<{
  explained: [explanation: string]
}>()

type Language = {
  label: string
  value: string
}

const languages: Language[] = [
  { label: 'JavaScript', value: 'javascript' },
  { label: 'TypeScript', value: 'typescript' },
  { label: 'Python', value: 'python' },
  { label: 'Java', value: 'java' },
  { label: 'C++', value: 'cpp' },
  { label: 'Rust', value: 'rust' },
  { label: 'Go', value: 'go' },
  { label: 'PHP', value: 'php' },
  { label: 'Ruby', value: 'ruby' },
  { label: 'C#', value: 'csharp' },
]

const code = ref(props.initialCode || '')
const selectedLanguage = ref<Language>(languages[0])
const explanation = ref('')
const isLoading = ref(false)
const error = ref<string | null>(null)
const copied = ref(false)

const hasExplanation = computed(() => explanation.value.length > 0)
const canExplain = computed(() => code.value.trim().length > 0)

async function explainCode() {
  if (!canExplain.value) return

  isLoading.value = true
  error.value = null

  try {
    const response = await client.post('/api/v2/ai/explain', {
      code: code.value,
      language: selectedLanguage.value.value,
      paste_id: props.pasteId
    })

    explanation.value = response.data.explanation
    emit('explained', explanation.value)
  } catch (e) {
    error.value = 'Failed to explain code. Please try again.'
    console.error('Code explanation error:', e)
  } finally {
    isLoading.value = false
  }
}

async function copyToClipboard() {
  try {
    await navigator.clipboard.writeText(explanation.value)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch (e) {
    console.error('Copy failed:', e)
  }
}

function clearExplanation() {
  explanation.value = ''
  error.value = null
}
</script>

<template>
  <Card class="code-explainer">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Code Explainer</span>
        <i class="pi pi-lightbulb text-yellow-500"></i>
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div class="flex flex-col">
            <label for="code-input" class="mb-2 text-sm font-medium">
              Code to Explain
            </label>
            <Textarea
              id="code-input"
              v-model="code"
              :disabled="isLoading"
              rows="10"
              placeholder="Paste your code here..."
              class="font-mono text-sm"
            />
          </div>

          <div class="flex flex-col">
            <label for="language-select" class="mb-2 text-sm font-medium">
              Language
            </label>
            <Select
              id="language-select"
              v-model="selectedLanguage"
              :options="languages"
              option-label="label"
              placeholder="Select language"
              :disabled="isLoading"
            />
          </div>
        </div>

        <div class="flex gap-2">
          <Button
            label="Explain Code"
            icon="pi pi-lightbulb"
            :loading="isLoading"
            :disabled="!canExplain"
            @click="explainCode"
          />
          <Button
            v-if="hasExplanation"
            label="Clear"
            icon="pi pi-times"
            severity="secondary"
            outlined
            @click="clearExplanation"
          />
        </div>

        <Message v-if="error" severity="error" :closable="false">
          {{ error }}
        </Message>

        <div v-if="hasExplanation" class="explanation-result">
          <div class="mb-2 flex items-center justify-between">
            <h3 class="text-lg font-semibold">Explanation</h3>
            <Button
              :icon="copied ? 'pi pi-check' : 'pi pi-copy'"
              :label="copied ? 'Copied!' : 'Copy'"
              size="small"
              text
              @click="copyToClipboard"
            />
          </div>

          <Card>
            <template #content>
              <div
                class="prose dark:prose-invert max-w-none"
                v-html="explanation"
              ></div>
            </template>
          </Card>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.code-explainer {
  width: 100%;
}

.explanation-result {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--surface-border);
}
</style>
