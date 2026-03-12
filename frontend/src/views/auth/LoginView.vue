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

const email = ref('')
const password = ref('')
const error = ref('')
const isLoading = ref(false)

async function login() {
  error.value = ''
  isLoading.value = true

  try {
    const response = await client.post('/api/v2/auth/local/login', {
      email: email.value,
      password: password.value
    })

    if (response.data.access_token) {
      localStorage.setItem('pastely_token', response.data.access_token)
      await currentUser.fetchUser()
      router.push('/')
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || 'Login failed'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="mx-auto mt-20 w-full max-w-md px-4">
    <div class="rounded-lg border border-gray-200 bg-white p-8 shadow-sm dark:border-gray-800 dark:bg-gray-900">
      <h1 class="mb-6 text-3xl font-bold">Sign In to Pastely</h1>

      <form @submit.prevent="login" class="space-y-4">
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
            :feedback="false"
            toggleMask
            class="w-full"
            inputClass="w-full"
            required
          />
        </div>

        <div v-if="error" class="text-sm text-red-600">
          {{ error }}
        </div>

        <Button
          type="submit"
          label="Sign In"
          :loading="isLoading"
          class="w-full"
        />
      </form>

      <div class="mt-4 text-center text-sm">
        <router-link to="/auth/forgot-password" class="text-blue-600 hover:underline dark:text-blue-400">
          Forgot password?
        </router-link>
      </div>

      <div class="mt-4 text-center text-sm">
        Don't have an account?
        <router-link to="/auth/register" class="text-blue-600 hover:underline dark:text-blue-400">
          Sign up
        </router-link>
      </div>

      <div class="my-6 flex items-center">
        <div class="flex-1 border-t border-gray-300 dark:border-gray-700"></div>
        <span class="px-4 text-sm text-gray-500 dark:text-gray-400">Or continue with</span>
        <div class="flex-1 border-t border-gray-300 dark:border-gray-700"></div>
      </div>

      <div class="space-y-2">
        <Button label="GitHub" icon="pi pi-github" class="w-full" outlined @click="router.push('/api/v2/auth/oauth2/github')" />
        <Button label="Google" icon="pi pi-google" class="w-full" outlined @click="router.push('/api/v2/auth/oauth2/google')" />
      </div>
    </div>
  </div>
</template>
