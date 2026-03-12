<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import Card from 'primevue/card'
import Button from 'primevue/button'
import type { Collection } from '@/types/collection'

const props = defineProps<{
  collection: Collection
}>()

const emit = defineEmits<{
  edit: [collection: Collection]
  delete: [collection: Collection]
}>()

const router = useRouter()

const pasteCountText = computed(() => {
  const count = props.collection.paste_count || 0
  return `${count} paste${count !== 1 ? 's' : ''}`
})

function viewCollection() {
  router.push(`/collection/${props.collection.id}`)
}
</script>

<template>
  <Card class="collection-card group relative overflow-hidden border border-cyan-500/30 bg-gray-900/50 backdrop-blur transition-all hover:border-cyan-400 hover:shadow-lg hover:shadow-cyan-500/20">
    <template #header>
      <div class="flex items-center justify-between border-b border-cyan-500/30 bg-gray-800/50 p-4">
        <div class="flex items-center gap-3">
          <div 
            v-if="collection.icon" 
            class="flex h-10 w-10 items-center justify-center rounded-lg text-2xl"
            :style="{ backgroundColor: collection.color || '#06b6d4' }"
          >
            {{ collection.icon }}
          </div>
          <div v-else class="h-10 w-10 rounded-lg bg-gradient-to-br from-cyan-500 to-purple-600"></div>
          <div>
            <h3 class="font-bold text-cyan-400">{{ collection.name }}</h3>
            <p class="text-xs text-gray-400">{{ pasteCountText }}</p>
          </div>
        </div>
        <div class="flex gap-2 opacity-0 transition-opacity group-hover:opacity-100">
          <Button
            icon="pi pi-pencil"
            text
            rounded
            severity="secondary"
            size="small"
            @click.stop="emit('edit', collection)"
          />
          <Button
            icon="pi pi-trash"
            text
            rounded
            severity="danger"
            size="small"
            @click.stop="emit('delete', collection)"
          />
        </div>
      </div>
    </template>

    <template #content>
      <div class="space-y-3">
        <p v-if="collection.description" class="text-sm text-gray-300">
          {{ collection.description }}
        </p>
        <p v-else class="text-sm italic text-gray-500">
          No description
        </p>

        <div class="flex items-center gap-2">
          <span v-if="collection.is_public" class="rounded-full bg-green-500/20 px-2 py-1 text-xs text-green-400">
            <i class="pi pi-globe mr-1"></i>
            Public
          </span>
          <span v-else class="rounded-full bg-gray-500/20 px-2 py-1 text-xs text-gray-400">
            <i class="pi pi-lock mr-1"></i>
            Private
          </span>
        </div>

        <Button
          label="View Collection"
          icon="pi pi-arrow-right"
          class="w-full"
          @click="viewCollection"
        />
      </div>
    </template>
  </Card>
</template>

<style scoped>
.collection-card {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
