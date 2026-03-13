<script setup lang="ts">
import { ref, computed } from 'vue'
import { client } from '@/utils/client'
import Button from 'primevue/button'
import Textarea from 'primevue/textarea'
import Select from 'primevue/select'
import Card from 'primevue/card'
import Message from 'primevue/message'

type Language = {
  label: string
  value: string
}

const props = defineProps<{
  pasteId?: string
  initialCode?: string
  initialLanguage?: string
}>()

const emit = defineEmits<{
  translated: [code: string, language: string]
}>()

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
  { label: 'Kotlin', value: 'kotlin' },
  { label: 'Swift', value: 'swift' },
]

const sourceCode = ref(props.initialCode || '')
const targetCode = ref('')
const sourceLanguage = ref<Language>(
  languages.find(l => l.value === props.initialLanguage) || languages[0]
)
const targetLanguage = ref<Language>(languages[1])
const isLoading = ref(false)
const error = ref<string | null>(null)
const copied = ref(false)

const canTranslate = computed(() => {
  return sourceCode.value.trim().length > 0 &&
         sourceLanguage.value.value !== targetLanguage.value.value
})

const hasTranslation = computed(() => targetCode.value.length > 0)

async function translateCode() {
  if (!canTranslate.value) return

  isLoading.value = true
  error.value = null
  targetCode.value = ''

  try {
    const response = await client.post('/api/v2/ai/translate', {
      code: sourceCode.value,
      from_language: sourceLanguage.value.value,
      to_language: targetLanguage.value.value,
      paste_id: props.pasteId
    })

    targetCode.value = response.data.translated_code
    emit('translated', targetCode.value, targetLanguage.value.value)
  } catch (e) {
    error.value = 'Failed to translate code. Please try again.'
    console.error('Translation error:', e)
  } finally {
    isLoading.value = false
  }
}

async function copyTranslatedCode() {
  try {
    await navigator.clipboard.writeText(targetCode.value)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch (e) {
    console.error('Copy failed:', e)
  }
}

function swapLanguages() {
  const temp = sourceLanguage.value
  sourceLanguage.value = targetLanguage.value
  targetLanguage.value = temp

  if (hasTranslation.value) {
    const tempCode = sourceCode.value
    sourceCode.value = targetCode.value
    targetCode.value = tempCode
  }
}

function clearTranslation() {
  targetCode.value = ''
  error.value = null
}
</script>

<template>
  <Card class="code-translator">
    <template #title>
      <div class="flex items-center justify-between">
        <span>Code Translator</span>
        <i class="pi pi-language text-blue-500"></i>
      </div>
    </template>

    <template #content>
      <div class="space-y-4">
        <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
          <div class="source-panel">
            <div class="mb-2 flex items-center justify-between">
              <label for="source-lang" class="text-sm font-medium">
                From
              </label>
              <Select
                id="source-lang"
                v-model="sourceLanguage"
                :options="languages"
                option-label="label"
                :disabled="isLoading"
                class="w-48"
              />
            </div>
            <Textarea
              v-model="sourceCode"
              :disabled="isLoading"
              rows="16"
              placeholder="Paste source code here..."
              class="font-mono text-sm"
            />
          </div>

          <div class="target-panel">
            <div class="mb-2 flex items-center justify-between">
              <label for="target-lang" class="text-sm font-medium">
                To
              </label>
              <Select
                id="target-lang"
                v-model="targetLanguage"
                :options="languages"
                option-label="label"
                :disabled="isLoading"
                class="w-48"
              />
            </div>
            <div class="relative">
              <Textarea
                v-model="targetCode"
                :disabled="true"
                rows="16"
                placeholder="Translated code will appear here..."
                class="font-mono text-sm"
              />
              <Button
                v-if="hasTranslation"
                :icon="copied ? 'pi pi-check' : 'pi pi-copy'"
                :label="copied ? 'Copied!' : ''"
                size="small"
                class="absolute right-2 top-2"
                @click="copyTranslatedCode"
              />
            </div>
          </div>
        </div>

        <div class="flex items-center justify-center gap-2">
          <Button
            label="Translate"
            icon="pi pi-arrow-right"
            :loading="isLoading"
            :disabled="!canTranslate"
            @click="translateCode"
          />
          <Button
            icon="pi pi-arrow-right-arrow-left"
            severity="secondary"
            outlined
            :disabled="isLoading"
            @click="swapLanguages"
            v-tooltip.top="'Swap languages'"
          />
          <Button
            v-if="hasTranslation"
            label="Clear"
            icon="pi pi-times"
            severity="secondary"
            outlined
            @click="clearTranslation"
          />
        </div>

        <Message v-if="error" severity="error" :closable="false">
          {{ error }}
        </Message>
      </div>
    </template>
  </Card>
</template>

<style scoped>
.code-translator {
  width: 100%;
}

.source-panel,
.target-panel {
  display: flex;
  flex-direction: column;
}
</style>
