import todoApi from '../api/todo'

export default {
  name: 'ToDoList',
  data() {
    return {
      newTodo: '',
      todos: [],
      filter: 'all', // 新增：过滤状态（all, active, completed）
      username: localStorage.getItem('username') || '用户'
    }
  },
  created() {
    this.fetchTodos()
  },
  watch: {
    // 移除 localStorage 监听
  },
  computed: {
    completedCount() {
      return this.todos.filter(todo => todo.isCompleted).length
    },
    filteredTodos() {
      // 根据过滤条件返回不同的待办事项列表
      switch(this.filter) {
        case 'active':
          return this.todos.filter(todo => !todo.isCompleted);
        case 'completed':
          return this.todos.filter(todo => todo.isCompleted);
        default:
          // 在"全部"视图中，将未完成的事项排在前面
          return [...this.todos.filter(todo => !todo.isCompleted), ...this.todos.filter(todo => todo.isCompleted)];
      }
    },
  
  },
  
  methods: {
    handleLogout() {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      this.$router.push('/login')
    },
    // 获取所有待办事项
    async fetchTodos() {
      try {
        this.todos = await todoApi.getTodos()
      } catch (error) {
        console.error('获取待办事项失败:', error)
      }
    },

    async addTodo() {
      if (this.newTodo.trim()) {
        try {
          const newTodo = await todoApi.createTodo(this.newTodo.trim())
          this.todos.unshift(newTodo) // 添加到列表头部
          this.newTodo = ''
        } catch (error) {
          console.error('添加待办事项失败:', error)
          alert('添加失败，请重试')
        }
      }
    },

    async removeTodo(index) {
      const todoToRemove = this.filteredTodos[index];
      if (!todoToRemove) return;
      
      try {
        await todoApi.deleteTodo(todoToRemove.id)
        // 从本地数组移除
        const originalIndex = this.todos.findIndex(t => t.id === todoToRemove.id)
        if (originalIndex !== -1) {
          this.todos.splice(originalIndex, 1)
        }
      } catch (error) {
        console.error('删除待办事项失败:', error)
        alert('删除失败，请重试')
      }
    },

    async toggleTodoStatus(todo) {
      try {
        // 乐观更新：先在前端切换状态
        todo.isCompleted = !todo.isCompleted
        // 发送请求更新后端
        await todoApi.updateTodo(todo.id, todo.isCompleted)
      } catch (error) {
        // 如果失败，回滚状态
        todo.isCompleted = !todo.isCompleted
        console.error('更新状态失败:', error)
        alert('更新状态失败，请重试')
      }
    },

    async clearCompleted() {
      // 注意：目前后端没有批量删除接口，这里暂时只能前端过滤显示或者循环调用删除
      // 为了演示，我们暂时只在前端隐藏，如果要彻底实现，建议后端增加批量删除接口
      const completedTodos = this.todos.filter(todo => todo.isCompleted)
      
      // 这是一个简单的批量删除实现（并不高效，建议后续优化后端接口）
      for (const todo of completedTodos) {
        try {
          await todoApi.deleteTodo(todo.id)
          const index = this.todos.findIndex(t => t.id === todo.id)
          if (index !== -1) this.todos.splice(index, 1)
        } catch (error) {
          console.error(`删除 ID ${todo.id} 失败`, error)
        }
      }
    }
  }
}