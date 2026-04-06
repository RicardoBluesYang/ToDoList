<template>
  <div class="auth-page">
    <section class="auth-card">
      <header class="auth-header">
        <p class="auth-tag">TICKDONE</p>
        <h2>欢迎</h2>
        <p class="auth-subtitle">登录后管理你的任务</p>
      </header>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label for="username">用户名</label>
          <input id="username" type="text" v-model="username" required placeholder="请输入用户名" />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input id="password" type="password" v-model="password" required placeholder="请输入密码" />
        </div>

        <button type="submit" class="auth-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <p class="auth-link">
          还没有账号？ <router-link to="/register">去注册</router-link>
        </p>
        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
      </form>
    </section>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Login',
  data() {
    return {
      username: '',
      password: '',
      loading: false,
      errorMsg: ''
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      this.errorMsg = ''
      try {
        const response = await axios.post(
          (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api') + '/users/login',
          {
            username: this.username,
            password: this.password
          }
        )

        localStorage.setItem('token', response.data.token)
        localStorage.setItem('username', this.username)
        this.$router.push('/tasks')
      } catch (error) {
        this.errorMsg = error.response?.data?.error || '登录失败，请检查网络或账号密码'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 60px);
  display: grid;
  place-items: center;
  padding: 18px 0;
  animation: auth-enter 0.36s var(--ease-main);
}

.auth-card {
  width: min(460px, 100%);
  border-radius: var(--radius-xl);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.9), rgba(245, 253, 252, 0.76));
  border: 1px solid var(--border-soft);
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(8px);
  padding: 28px 24px;
}

.auth-header {
  text-align: left;
  margin-bottom: 20px;
}

.auth-tag {
  display: inline-flex;
  align-items: center;
  margin-bottom: 8px;
  border-radius: 999px;
  background: rgba(11, 125, 186, 0.1);
  color: var(--brand-2);
  font-size: 12px;
  padding: 5px 10px;
}

.auth-header h2 {
  font-size: 30px;
  line-height: 1.15;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
  color: #12313c;
}

.auth-subtitle {
  color: var(--text-secondary);
  font-size: 14px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-group {
  text-align: left;
}

.form-group label {
  display: inline-block;
  margin-bottom: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

.form-group input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(104, 166, 180, 0.35);
  background: rgba(255, 255, 255, 0.92);
  transition: border-color var(--dur-fast) var(--ease-main), box-shadow var(--dur-fast) var(--ease-main);
}

.form-group input:hover {
  border-color: rgba(44, 145, 161, 0.58);
}

.auth-btn {
  height: 46px;
  border-radius: var(--radius-sm);
  font-weight: 700;
  letter-spacing: 1px;
  color: #fff;
  background: linear-gradient(120deg, var(--brand-1), var(--brand-2));
  cursor: pointer;
  margin-top: 2px;
}

.auth-btn:hover:enabled {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(11, 125, 186, 0.26);
  filter: brightness(1.03);
}

.auth-link {
  margin-top: 2px;
  color: var(--text-muted);
  font-size: 14px;
}

.error-msg {
  border-radius: var(--radius-sm);
  border: 1px solid var(--warn-border);
  background: var(--warn-bg);
  color: #b53b43;
  font-size: 13px;
  padding: 9px 10px;
}

@keyframes auth-enter {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 640px) {
  .auth-page {
    min-height: calc(100vh - 30px);
  }

  .auth-card {
    padding: 22px 16px;
    border-radius: var(--radius-lg);
  }

  .auth-header h2 {
    font-size: 26px;
  }
}
</style>
