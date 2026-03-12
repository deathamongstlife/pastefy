<script setup lang="ts">
import PasteList from '@/components/lists/PasteList.vue'
import { useTitle } from '@vueuse/core'
useTitle('Pastes - Admin | Pastely')
</script>
<template>
  <PasteList route="/api/v2/paste" />
</template>
