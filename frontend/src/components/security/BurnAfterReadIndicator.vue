<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import Message from 'primevue/message'
import Tag from 'primevue/tag'

const props = defineProps<{
  viewsRemaining: number
  totalViews: number
}>()

const currentViews = ref(props.viewsRemaining)
const isAnimating = ref(false)

const progressPercentage = computed(() => {
  return ((props.totalViews - currentViews.value) / props.totalViews) * 100
})

const severityLevel = computed(() => {
  if (currentViews.value === 1) return 'danger'
  if (currentViews.value <= 3) return 'warn'
  return 'info'
})

onMounted(() => {
  if (currentViews.value <= 3) {
    isAnimating.value = true
  }
})
</script>

<template>
  <Message
    :severity="severityLevel"
    :closable="false"
    class="burn-after-read-indicator"
    :class="{ 'pulse-animation': isAnimating }"
  >
    <div class="flex items-center gap-3">
      <i class="pi pi-fire text-2xl"></i>
      <div class="flex-1">
        <div class="mb-1 font-semibold">
          This paste will self-destruct
        </div>
        <div class="text-sm">
          <Tag
            :severity="severityLevel"
            :value="`${currentViews} ${currentViews === 1 ? 'view' : 'views'} remaining`"
          />
        </div>
      </div>
    </div>
  </Message>
</template>

<style scoped>
.burn-after-read-indicator {
  width: 100%;
}

.pulse-animation {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}
</style>
