import { createRouter, createWebHistory } from 'vue-router'
import ToDoList from '../components/ToDoList.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'

const routes = [
  {
    path: '/',
    redirect: '/tasks'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/tasks',
    name: 'Tasks',
    component: ToDoList,
    meta: { requiresAuth: true } // 需要登录才能访问
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查是否登录
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    // 已经登录，访问登录/注册页直接跳到任务页
    next('/tasks')
  } else {
    next()
  }
})

export default router
