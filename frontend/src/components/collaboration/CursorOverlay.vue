<script setup lang="ts">
import { computed } from 'vue'
import type { CursorPosition } from '@/services/collaborationWebSocket'

const props = defineProps<{
  cursors: Map<string, CursorPosition>
}>()

const cursorList = computed(() => {
  return Array.from(props.cursors.entries()).map(([userId, position]) => ({
    userId,
    position
  }))
})

const getUserColor = (userId: string): string => {
  const colors = [
    '#3B82F6',
    '#EF4444',
    '#10B981',
    '#F59E0B',
    '#8B5CF6',
    '#EC4899',
    '#14B8A6',
    '#F97316',
  ]

  let hash = 0
  for (let i = 0; i < userId.length; i++) {
    hash = userId.charCodeAt(i) + ((hash << 5) - hash)
  }

  return colors[Math.abs(hash) % colors.length]
}
</script>

<template>
  <div class="cursor-overlay">
    <div
      v-for="cursor in cursorList"
      :key="cursor.userId"
      class="cursor-marker"
      :style="{
        top: `${cursor.position.line * 1.5}em`,
        left: `${cursor.position.column * 0.6}em`,
        borderLeftColor: getUserColor(cursor.userId)
      }"
    >
      <div
        class="cursor-label"
        :style="{ backgroundColor: getUserColor(cursor.userId) }"
      >
        {{ cursor.userId.substring(0, 2) }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.cursor-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 10;
}

.cursor-marker {
  position: absolute;
  border-left: 2px solid;
  height: 1.2em;
  pointer-events: none;
  transition: all 0.1s ease-out;
}

.cursor-label {
  position: absolute;
  top: -1.5em;
  left: -0.25em;
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  color: white;
  white-space: nowrap;
  font-weight: 500;
}
</style>
