<script setup lang="ts">
import markdownit from 'markdown-it'
import { defineAsyncComponent, defineCustomElement, onMounted, ref, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'

const props = defineProps<{
  markdown: string
}>()

const useMermaidViewer = () => {
  if (customElements.get('pastely-mermaid-viewer')) return
  customElements.define(
    'pastely-mermaid-viewer',
    defineCustomElement(
      defineAsyncComponent(() => import('@/components/previews/MermaidViewer.vue')),
      {
        shadowRoot: false,
      },
    ),
  )
}

const md = markdownit({
  html: false,
  breaks: true,
  highlight: (str, lang) => {
    const container = document.createElement('div')

    'border-neutral-300 dark:border-neutral-700 border rounded-md mb-8'.split(' ').forEach((c) => {
      container.classList.add(c.trim())
    })

    if (lang === 'mermaid' || lang === 'mmd') {
      useMermaidViewer()
      const pastelyMermaid = document.createElement('pastely-mermaid-viewer')
      pastelyMermaid.setAttribute('mermaid-code', str?.trim() || '')
      container.appendChild(pastefyMermaid)
      return container.outerHTML
    }

    const pastelyHighlighted = document.createElement('pastely-highlighted')

    pastelyHighlighted.setAttribute('language', `.${lang}`)
    pastelyHighlighted.setAttribute('contents', str?.trim() || '')
    pastelyHighlighted.setAttribute('show-copy-button', 'true')

    container.appendChild(pastefyHighlighted)
    return container.outerHTML
  },
})

const output = ref('')

const render = () => {
  output.value = md.render(props.markdown)
}

onMounted(() => {
  render()
})

watch(
  () => props.markdown,
  useDebounceFn(() => {
    render()
  }, 500),
)
</script>
<template>
  <div class="markdown-prev" v-html="output" />
</template>
