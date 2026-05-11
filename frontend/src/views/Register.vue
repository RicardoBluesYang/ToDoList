<template>
  <div class="auth-page">
    <section class="auth-card">
      <header class="auth-header">
        <p class="auth-kicker"><b>TICK DONE</b></p>
        <h2>创建账号</h2>
        <p class="auth-subtitle">注册后即可开始管理你的任务进度。</p>
      </header>

      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="field">
          <label for="username">用户名</label>
          <input id="username" type="text" v-model="username" required placeholder="Username" />
        </div>
        <div class="field">
          <label for="password">密码</label>
          <input id="password" type="password" v-model="password" required placeholder="Password" />
        </div>
        <div class="field">
          <label for="confirmPassword">确认密码</label>
          <input id="confirmPassword" type="password" v-model="confirmPassword" required placeholder="Confirm password" />
        </div>

        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <p class="auth-link">
          已有账号？<router-link to="/login">去登录</router-link>
        </p>

        <p v-if="errorMsg" class="error-msg" role="alert">{{ errorMsg }}</p>
        <p v-if="successMsg" class="success-msg" role="status">{{ successMsg }}</p>
      </form>
    </section>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Register',
  data() {
    return {
      username: '',
      password: '',
      confirmPassword: '',
      loading: false,
      errorMsg: '',
      successMsg: ''
    }
  },
  methods: {
    async handleRegister() {
      if (this.password !== this.confirmPassword) {
        this.errorMsg = '两次输入的密码不一致'
        return
      }

      this.loading = true
      this.errorMsg = ''
      this.successMsg = ''

      try {
        await axios.post((import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api') + '/users/register', {
          username: this.username,
          password: this.password
        })

        this.successMsg = '注册成功，即将跳转到登录页...'
        setTimeout(() => {
          this.$router.push('/login')
        }, 1500)
      } catch (error) {
        this.errorMsg = error.response?.data?.error || '注册失败，该用户名可能已被占用'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 120px);
  display: grid;
  place-items: center;
}

.auth-card {
  width: min(440px, 100%);
  border: 1px solid var(--color-line);
  padding: 48px 44px;
  animation: card-in var(--dur-slow) var(--ease-out);
}

.auth-header {
  margin-bottom: 36px;
}

.auth-kicker {
  font-family: var(--font-serif);
  font-size: 26px;
  font-style: italic;
  color: var(--color-ink);
  margin-bottom: 20px;
  letter-spacing: 0;
  text-transform: none;
}

.auth-header h2 {
  font-family: var(--font-serif);
  font-size: clamp(36px, 4vw, 48px);
  font-weight: 400;
  letter-spacing: -0.01em;
  margin-bottom: 14px;
  color: var(--color-ink);
}

.auth-subtitle {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--color-muted);
  line-height: 1.6;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.field label {
  display: block;
  margin-bottom: 10px;
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 400;
  color: var(--color-muted);
  text-transform: uppercase;
  letter-spacing: 0.18em;
}

.field input {
  width: 100%;
  height: 44px;
  padding: 0 0 10px;
  font-family: var(--font-mono);
  font-size: 14px;
  color: var(--color-ink);
  background: transparent;
  border: none;
  border-bottom: 1px solid var(--color-line);
  border-radius: 0;
  outline: none;
  transition: border-color var(--dur-fast);
}

.field input::placeholder {
  color: var(--color-muted);
  font-size: 11px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.field input:hover {
  border-bottom-color: var(--color-ink);
}

.field input:focus-visible {
  border-bottom-color: var(--color-accent);
}

.btn-primary {
  height: 52px;
  margin-top: 8px;
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 400;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-white);
  background: var(--color-ink);
  border: none;
  padding: 0 32px;
  transition: background-color var(--dur-fast);
}

.btn-primary:hover:enabled {
  background: var(--color-accent);
}

.btn-primary:disabled {
  opacity: 0.4;
}

.auth-link {
  text-align: center;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-muted);
}

.auth-link a {
  color: var(--color-ink);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.auth-link a:hover {
  color: var(--color-accent);
}

.error-msg {
  border: 1px solid var(--color-danger);
  padding: 12px 14px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-danger);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  background: var(--color-danger-dim);
}

.success-msg {
  border: 1px solid var(--color-accent);
  padding: 12px 14px;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-accent);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  background: var(--color-accent-dim);
}

@keyframes card-in {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 640px) {
  .auth-card {
    padding: 36px 24px;
    border-left: none;
    border-right: none;
  }
}
</style>
