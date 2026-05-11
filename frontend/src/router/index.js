import { createRouter, createWebHistory } from 'vue-router'
import ToDoList from '../components/ToDoList.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Admin from '../views/Admin.vue'

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
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('token')

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }

  if (to.meta.requiresAdmin && localStorage.getItem('username') !== 'admin') {
    next('/tasks')
    return
  }

  if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    next('/tasks')
    return
  }

  next()
})

export default router
