<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { client } from '@/utils/client'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'

const router = useRouter()

const email = ref('')
const error = ref('')
const success = ref(false)
const isLoading = ref(false)

async function sendResetEmail() {
  error.value = ''
  isLoading.value = true

  try {
    await client.post('/api/v2/auth/local/forgot-password', {
      email: email.value
    })

    success.value = true
  } catch (e: any) {
    error.value = e.response?.data?.message || 'Failed to send reset email'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="mx-auto mt-20 w-full max-w-md px-4">
    <div class="rounded-lg border border-gray-200 bg-white p-8 shadow-sm dark:border-gray-800 dark:bg-gray-900">
      <h1 class="mb-6 text-3xl font-bold">Reset Password</h1>

      <div v-if="success" class="mb-4 rounded-lg border border-green-200 bg-green-50 p-4 text-sm text-green-800 dark:border-green-800 dark:bg-green-900 dark:text-green-200">
        If an account exists with that email, we've sent password reset instructions.
      </div>

      <form v-else @submit.prevent="sendResetEmail" class="space-y-4">
        <div>
          <label for="email" class="mb-2 block text-sm font-medium">Email</label>
          <InputText
            id="email"
            v-model="email"
            type="email"
            placeholder="you@example.com"
            class="w-full"
            required
          />
        </div>

        <div v-if="error" class="text-sm text-red-600">
          {{ error }}
        </div>

        <Button
          type="submit"
          label="Send Reset Instructions"
          :loading="isLoading"
          class="w-full"
        />
      </form>

      <div class="mt-4 text-center text-sm">
        <router-link to="/auth/login" class="text-blue-600 hover:underline dark:text-blue-400">
          Back to sign in
        </router-link>
      </div>
    </div>
  </div>
</template>
