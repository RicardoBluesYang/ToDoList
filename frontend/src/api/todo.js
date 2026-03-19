import axios from 'axios'

// 创建 axios 实例
const api = axios.create({
  // 开发环境使用 http://localhost:8080/api，生产环境使用 /api (会被 Nginx 转发)
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api', 
  timeout: 5000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：在发请求之前带上 Token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：处理错误
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 401 未授权，通常是 Token 过期或未登录
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      // 跳转到登录页 (稍微粗暴的跳转方式，如果是组件内最好用 router)
      window.location.href = '/login'
    }
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default {
  // 获取所有待办事项
  getTodos() {
    return api.get('/todos')
  },
  
  // 创建待办事项
  createTodo(title) {
    return api.post('/todos', { title })
  },
  
  // 更新待办事项状态
  updateTodo(id, isCompleted) {
    return api.put(`/todos/${id}`, { isCompleted })
  },
  
  // 删除待办事项
  deleteTodo(id) {
    return api.delete(`/todos/${id}`)
  }
}
