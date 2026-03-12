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
  <div class="flex min-h-screen items-center justify-center bg-dark-bg scanlines p-4">
    <div class="cyber-card w-full max-w-md rounded-lg p-8">
      <h1 class="neon-text mb-8 text-center text-4xl font-bold text-cyber-pink">
        CREATE ACCOUNT
      </h1>

      <form @submit.prevent="register" class="space-y-6">
        <div>
          <label for="username" class="mb-2 block text-sm font-medium uppercase text-cyber-blue">Username</label>
          <InputText
            id="username"
            v-model="username"
            placeholder="johndoe"
            class="cyber-input w-full"
            required
          />
        </div>

        <div>
          <label for="email" class="mb-2 block text-sm font-medium uppercase text-cyber-blue">Email</label>
          <InputText
            id="email"
            v-model="email"
            type="email"
            placeholder="you@example.com"
            class="cyber-input w-full"
            required
          />
        </div>

        <div>
          <label for="password" class="mb-2 block text-sm font-medium uppercase text-cyber-blue">Password</label>
          <Password
            id="password"
            v-model="password"
            toggleMask
            class="cyber-input w-full"
            inputClass="w-full"
            required
          />
          <small class="text-text-muted">Minimum 8 characters</small>
        </div>

        <div v-if="error" class="rounded-lg border border-cyber-pink bg-red-900/20 p-3 text-center text-cyber-pink">
          {{ error }}
        </div>

        <Button
          type="submit"
          label="REGISTER"
          :loading="isLoading"
          class="cyber-button w-full"
        />
      </form>

      <div class="mt-6 text-center">
        <span class="text-text-secondary">ALREADY HAVE AN ACCOUNT? </span>
        <router-link to="/auth/login" class="text-cyber-blue transition-colors hover:text-cyber-pink">
          SIGN IN
        </router-link>
      </div>
    </div>
  </div>
</template>
