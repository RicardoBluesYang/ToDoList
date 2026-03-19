<template>
  <div class="auth-container">
    <h2>待办事项 - 注册</h2>
    <form @submit.prevent="handleRegister" class="auth-form">
      <div class="form-group">
        <label>用户名：</label>
        <input type="text" v-model="username" required placeholder="请输入用户名" />
      </div>
      <div class="form-group">
        <label>密码：</label>
        <input type="password" v-model="password" required placeholder="请输入密码" />
      </div>
      <div class="form-group">
        <label>确认密码：</label>
        <input type="password" v-model="confirmPassword" required placeholder="请再次输入密码" />
      </div>
      <button type="submit" class="auth-btn" :disabled="loading">
        {{ loading ? '注册中...' : '注册' }}
      </button>
      <p class="auth-link">已有账号？ <router-link to="/login">去登录</router-link></p>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
      <p v-if="successMsg" class="success-msg">{{ successMsg }}</p>
    </form>
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
        
        this.successMsg = '注册成功！即将跳转到登录页...'
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
/* 样式与 Login.vue 相同，这里直接复用 */
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
.success-msg {
  color: #4caf50;
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
