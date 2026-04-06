<template>
  <div class="auth-page">
    <section class="auth-card">
      <header class="auth-header">
        <p class="auth-tag">待办清单系统</p>
        <h2>创建账号</h2>
        <p class="auth-subtitle">注册后即可开始管理你的任务节奏。</p>
      </header>

      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label for="username">用户名</label>
          <input id="username" type="text" v-model="username" required placeholder="请输入用户名" />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input id="password" type="password" v-model="password" required placeholder="请输入密码" />
        </div>
        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <input id="confirmPassword" type="password" v-model="confirmPassword" required placeholder="请再次输入密码" />
        </div>

        <button type="submit" class="auth-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <p class="auth-link">
          已有账号？<router-link to="/login">去登录</router-link>
        </p>

        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
        <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>
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

.error-msg,
.success-msg {
  border-radius: var(--radius-sm);
  font-size: 13px;
  padding: 9px 10px;
}

.error-msg {
  border: 1px solid var(--warn-border);
  background: var(--warn-bg);
  color: #b53b43;
}

.success-msg {
  border: 1px solid rgba(95, 185, 151, 0.5);
  background: rgba(233, 252, 244, 0.95);
  color: #1f7f63;
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
