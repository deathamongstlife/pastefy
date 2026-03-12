<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { client } from '@/utils/client'
import { useCurrentUserStore } from '@/stores/current-user'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'

const router = useRouter()
const currentUser = useCurrentUserStore()

const username = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const isLoading = ref(false)

async function register() {
  error.value = ''
  isLoading.value = true

  try {
    const response = await client.post('/api/v2/auth/local/register', {
      username: username.value,
      email: email.value,
      password: password.value
    })

    if (response.data.access_token) {
      localStorage.setItem('pastely_token', response.data.access_token)
      await currentUser.fetchUser()
      router.push('/')
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || 'Registration failed'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="mx-auto mt-20 w-full max-w-md px-4">
    <div class="rounded-lg border border-gray-200 bg-white p-8 shadow-sm dark:border-gray-800 dark:bg-gray-900">
      <h1 class="mb-6 text-3xl font-bold">Create Your Account</h1>

      <form @submit.prevent="register" class="space-y-4">
        <div>
          <label for="username" class="mb-2 block text-sm font-medium">Username</label>
          <InputText
            id="username"
            v-model="username"
            placeholder="johndoe"
            class="w-full"
            required
          />
        </div>

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

        <div>
          <label for="password" class="mb-2 block text-sm font-medium">Password</label>
          <Password
            id="password"
            v-model="password"
            toggleMask
            class="w-full"
            inputClass="w-full"
            required
          />
          <small class="text-gray-500 dark:text-gray-400">Minimum 8 characters</small>
        </div>

        <div v-if="error" class="text-sm text-red-600">
          {{ error }}
        </div>

        <Button
          type="submit"
          label="Create Account"
          :loading="isLoading"
          class="w-full"
        />
      </form>

      <div class="mt-4 text-center text-sm">
        Already have an account?
        <router-link to="/auth/login" class="text-blue-600 hover:underline dark:text-blue-400">
          Sign in
        </router-link>
      </div>
    </div>
  </div>
</template>
