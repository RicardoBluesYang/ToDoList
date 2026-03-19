<template>
  <div class="auth-container">
    <h2>待办事项 - 登录</h2>
    <form @submit.prevent="handleLogin" class="auth-form">
      <div class="form-group">
        <label>用户名：</label>
        <input type="text" v-model="username" required placeholder="请输入用户名" />
      </div>
      <div class="form-group">
        <label>密码：</label>
        <input type="password" v-model="password" required placeholder="请输入密码" />
      </div>
      <button type="submit" class="auth-btn" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <p class="auth-link">还没有账号？ <router-link to="/register">去注册</router-link></p>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    </form>
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
        const response = await axios.post((import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api') + '/users/login', {
          username: this.username,
          password: this.password
        })
        
        // 登录成功，保存 Token 到 localStorage
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('username', this.username)
        
        // 跳转到任务页
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
.auth-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 30px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  text-align: center;
}
.form-group {
  margin-bottom: 20px;
  text-align: left;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #333;
}
.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  box-sizing: border-box;
}
.auth-btn {
  width: 100%;
  padding: 10px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
}
.auth-btn:disabled {
  background-color: #a0d8bf;
}
.auth-link {
  margin-top: 15px;
  font-size: 14px;
}
.error-msg {
  color: #ff4d4f;
  margin-top: 10px;
}

/* 登录/注册页响应式 */
@media screen and (max-width: 600px) {
  .auth-container {
    margin: 40px 15px;
    padding: 20px;
  }
}
</style>
