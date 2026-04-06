import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      window.location.href = '/login'
    }
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default {
  getTodos() {
    return api.get('/todos')
  },

  createTodo(title, dueDate = null) {
    return api.post('/todos', { title, dueDate })
  },

  updateTodo(id, payload) {
    return api.put(`/todos/${id}`, payload)
  },

  deleteTodo(id) {
    return api.delete(`/todos/${id}`)
  },

  createSubtask(todoId, title) {
    return api.post(`/todos/${todoId}/subtasks`, { title })
  },

  updateSubtask(todoId, subtaskId, payload) {
    return api.put(`/todos/${todoId}/subtasks/${subtaskId}`, payload)
  },

  deleteSubtask(todoId, subtaskId) {
    return api.delete(`/todos/${todoId}/subtasks/${subtaskId}`)
  },

  aiDecompose(goalTitle, dueDate = null) {
    return api.post('/ai/decompose', { goalTitle, dueDate })
  },

  aiCommit(parentTitle, dueDate, subtasks) {
    return api.post('/ai/decompose/commit', { parentTitle, dueDate, subtasks })
  }
}
