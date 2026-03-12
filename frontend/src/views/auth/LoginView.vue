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
  <div class="flex min-h-screen items-center justify-center bg-dark-bg scanlines p-4">
    <div class="cyber-card w-full max-w-md rounded-lg p-8">
      <h1 class="neon-text mb-8 text-center text-4xl font-bold text-cyber-blue">
        SIGN IN
      </h1>

      <form @submit.prevent="login" class="space-y-6">
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
            :feedback="false"
            toggleMask
            class="cyber-input w-full"
            inputClass="w-full"
            required
          />
        </div>

        <div v-if="error" class="rounded-lg border border-cyber-pink bg-red-900/20 p-3 text-center text-cyber-pink">
          {{ error }}
        </div>

        <Button
          type="submit"
          label="ACCESS SYSTEM"
          :loading="isLoading"
          class="cyber-button w-full"
        />
      </form>

      <div class="mt-6 text-center">
        <router-link to="/auth/forgot-password" class="text-cyber-blue transition-colors hover:text-cyber-pink">
          FORGOT PASSWORD?
        </router-link>
      </div>

      <div class="my-6 flex items-center">
        <div class="flex-1 border-t border-cyber-blue opacity-30"></div>
        <span class="px-4 text-sm text-text-muted">OR</span>
        <div class="flex-1 border-t border-cyber-blue opacity-30"></div>
      </div>

      <div class="space-y-2">
        <Button
          label="GitHub"
          icon="pi pi-github"
          class="w-full"
          outlined
          @click="router.push('/api/v2/auth/oauth2/github')"
        />
        <Button
          label="Google"
          icon="pi pi-google"
          class="w-full"
          outlined
          @click="router.push('/api/v2/auth/oauth2/google')"
        />
      </div>

      <div class="mt-6 text-center">
        <span class="text-text-secondary">NEW USER? </span>
        <router-link to="/auth/register" class="text-cyber-pink transition-colors hover:text-cyber-blue">
          REGISTER
        </router-link>
      </div>
    </div>
  </div>
</template>
