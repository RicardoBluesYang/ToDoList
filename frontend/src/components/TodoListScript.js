import todoApi from '../api/todo'

function createDraftItem(title = '') {
  return {
    id: `draft-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    title,
    checked: true
  }
}

export default {
  name: 'ToDoList',
  data() {
    return {
      newTodo: '',
      newTodoDueDate: '',
      todos: [],
      filter: 'all',
      username: localStorage.getItem('username') || '用户',
      editingTodoId: null,
      editForm: {
        title: '',
        dueDate: ''
      },
      expandedSubtasks: {},
      newSubtaskMap: {},
      editingSubtask: {
        todoId: null,
        subtaskId: null
      },
      editSubtaskForm: {
        title: ''
      },
      aiDrawerVisible: false,
      aiGoalTitle: '',
      aiGoalDueDate: '',
      aiLoading: false,
      aiError: '',
      aiDraftParentTitle: '',
      aiDraftSubtasks: []
    }
  },
  created() {
    this.fetchTodos()
  },
  computed: {
    completedCount() {
      return this.todos.filter(todo => todo.isCompleted).length
    },
    filteredTodos() {
      switch (this.filter) {
        case 'active':
          return this.todos.filter(todo => !todo.isCompleted)
        case 'completed':
          return this.todos.filter(todo => todo.isCompleted)
        default:
          return [
            ...this.todos.filter(todo => !todo.isCompleted),
            ...this.todos.filter(todo => todo.isCompleted)
          ]
      }
    },
    hasAiDraft() {
      return !!this.aiDraftParentTitle || this.aiDraftSubtasks.length > 0
    }
  },
  methods: {
    handleLogout() {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      this.$router.push('/login')
    },

    async fetchTodos() {
      try {
        const list = await todoApi.getTodos()
        this.todos = (list || []).map(todo => this.normalizeTodo(todo))
      } catch (error) {
        console.error('获取任务列表失败:', error)
      }
    },

    normalizeTodo(todo) {
      return {
        ...todo,
        subtasks: Array.isArray(todo.subtasks) ? todo.subtasks : []
      }
    },

    async addTodo() {
      const title = this.newTodo.trim()
      if (!title) return

      try {
        const created = await todoApi.createTodo(title, this.newTodoDueDate || null)
        this.todos.unshift(this.normalizeTodo(created))
        this.newTodo = ''
        this.newTodoDueDate = ''
      } catch (error) {
        console.error('添加任务失败:', error)
        alert(error.response?.data?.error || '添加任务失败')
      }
    },

    startEdit(todo) {
      this.editingTodoId = todo.id
      this.editForm.title = todo.title
      this.editForm.dueDate = this.toDateTimeInput(todo.dueDate)
    },

    cancelEdit() {
      this.editingTodoId = null
      this.editForm.title = ''
      this.editForm.dueDate = ''
    },

    async saveEdit(todoId) {
      const title = this.editForm.title.trim()
      if (!title) {
        alert('任务标题不能为空')
        return
      }

      try {
        const updated = await todoApi.updateTodo(todoId, {
          title,
          dueDate: this.editForm.dueDate || null
        })
        this.replaceTodo(updated)
        this.cancelEdit()
      } catch (error) {
        console.error('编辑任务失败:', error)
        alert(error.response?.data?.error || '编辑任务失败')
      }
    },

    async removeTodo(todoId) {
      try {
        await todoApi.deleteTodo(todoId)
        this.todos = this.todos.filter(todo => todo.id !== todoId)
        delete this.expandedSubtasks[todoId]
        delete this.newSubtaskMap[todoId]
      } catch (error) {
        console.error('删除任务失败:', error)
        alert(error.response?.data?.error || '删除任务失败')
      }
    },

    async toggleTodoStatus(todo) {
      const nextStatus = !todo.isCompleted
      todo.isCompleted = nextStatus
      try {
        await todoApi.updateTodo(todo.id, { isCompleted: nextStatus })
      } catch (error) {
        todo.isCompleted = !nextStatus
        console.error('更新任务状态失败:', error)
        alert(error.response?.data?.error || '更新任务状态失败')
      }
    },

    async clearCompleted() {
      const completedIds = this.todos.filter(todo => todo.isCompleted).map(todo => todo.id)
      for (const todoId of completedIds) {
        try {
          await todoApi.deleteTodo(todoId)
          this.todos = this.todos.filter(todo => todo.id !== todoId)
        } catch (error) {
          console.error(`删除任务 ${todoId} 失败:`, error)
        }
      }
    },

    isOverdue(todo) {
      if (todo.isCompleted || !todo.dueDate) return false
      const dueTime = new Date(todo.dueDate).getTime()
      if (Number.isNaN(dueTime)) return false
      return dueTime < Date.now()
    },

    formatDueDate(value) {
      if (!value) return ''
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return String(value)
      return date.toLocaleString('zh-CN', { hour12: false })
    },

    toDateTimeInput(value) {
      if (!value) return ''
      if (typeof value === 'string') {
        const normalized = value.includes(' ') ? value.replace(' ', 'T') : value
        if (normalized.length >= 16) return normalized.slice(0, 16)
      }

      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return ''

      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day}T${hours}:${minutes}`
    },

    toggleSubtasks(todoId) {
      this.expandedSubtasks[todoId] = !this.expandedSubtasks[todoId]
    },

    isSubtasksExpanded(todoId) {
      return !!this.expandedSubtasks[todoId]
    },

    async addSubtask(todoId) {
      const title = (this.newSubtaskMap[todoId] || '').trim()
      if (!title) return

      try {
        const created = await todoApi.createSubtask(todoId, title)
        const todo = this.findTodo(todoId)
        if (!todo) return
        if (!Array.isArray(todo.subtasks)) todo.subtasks = []
        todo.subtasks.push(created)
        this.newSubtaskMap[todoId] = ''
        this.expandedSubtasks[todoId] = true
      } catch (error) {
        console.error('添加子任务失败:', error)
        alert(error.response?.data?.error || '添加子任务失败')
      }
    },

    async toggleSubtaskStatus(todo, subtask) {
      const nextStatus = !subtask.isCompleted
      subtask.isCompleted = nextStatus
      try {
        await todoApi.updateSubtask(todo.id, subtask.id, { isCompleted: nextStatus })
      } catch (error) {
        subtask.isCompleted = !nextStatus
        console.error('更新子任务状态失败:', error)
        alert(error.response?.data?.error || '更新子任务状态失败')
      }
    },

    startEditSubtask(todoId, subtask) {
      this.editingSubtask.todoId = todoId
      this.editingSubtask.subtaskId = subtask.id
      this.editSubtaskForm.title = subtask.title
    },

    isEditingSubtask(todoId, subtaskId) {
      return this.editingSubtask.todoId === todoId && this.editingSubtask.subtaskId === subtaskId
    },

    cancelEditSubtask() {
      this.editingSubtask.todoId = null
      this.editingSubtask.subtaskId = null
      this.editSubtaskForm.title = ''
    },

    async saveEditSubtask(todoId, subtaskId) {
      const title = this.editSubtaskForm.title.trim()
      if (!title) {
        alert('子任务标题不能为空')
        return
      }

      try {
        const updated = await todoApi.updateSubtask(todoId, subtaskId, { title })
        const todo = this.findTodo(todoId)
        if (!todo || !Array.isArray(todo.subtasks)) return
        const idx = todo.subtasks.findIndex(item => item.id === subtaskId)
        if (idx !== -1) todo.subtasks.splice(idx, 1, updated)
        this.cancelEditSubtask()
      } catch (error) {
        console.error('编辑子任务失败:', error)
        alert(error.response?.data?.error || '编辑子任务失败')
      }
    },

    async removeSubtask(todoId, subtaskId) {
      try {
        await todoApi.deleteSubtask(todoId, subtaskId)
        const todo = this.findTodo(todoId)
        if (!todo || !Array.isArray(todo.subtasks)) return
        todo.subtasks = todo.subtasks.filter(item => item.id !== subtaskId)
      } catch (error) {
        console.error('删除子任务失败:', error)
        alert(error.response?.data?.error || '删除子任务失败')
      }
    },

    replaceTodo(updatedTodo) {
      const normalized = this.normalizeTodo(updatedTodo)
      const index = this.todos.findIndex(todo => todo.id === normalized.id)
      if (index !== -1) this.todos.splice(index, 1, normalized)
    },

    findTodo(todoId) {
      return this.todos.find(todo => todo.id === todoId)
    },

    openAiDrawer() {
      this.aiDrawerVisible = true
      this.aiError = ''
    },

    closeAiDrawer() {
      this.aiDrawerVisible = false
      this.aiError = ''
      this.aiLoading = false
      this.aiGoalTitle = ''
      this.aiGoalDueDate = ''
      this.aiDraftParentTitle = ''
      this.aiDraftSubtasks = []
    },

    async generateAiDecompose() {
      const goalTitle = this.aiGoalTitle.trim()
      if (!goalTitle) {
        this.aiError = '请输入目标任务'
        return
      }

      this.aiLoading = true
      this.aiError = ''
      try {
        const result = await todoApi.aiDecompose(goalTitle, this.aiGoalDueDate || null)
        this.aiDraftParentTitle = (result.parentTitle || '').trim()
        this.aiDraftSubtasks = (result.subtasks || []).map(title => createDraftItem(String(title || '').trim()))
      } catch (error) {
        this.aiError = error.response?.data?.error || '拆解失败，请稍后重试'
      } finally {
        this.aiLoading = false
      }
    },

    addAiDraftSubtask() {
      this.aiDraftSubtasks.push(createDraftItem(''))
    },

    removeAiDraftSubtask(index) {
      this.aiDraftSubtasks.splice(index, 1)
    },

    async commitAiDecompose() {
      const parentTitle = this.aiDraftParentTitle.trim()
      if (!parentTitle) {
        this.aiError = '主任务标题不能为空'
        return
      }

      const subtasks = this.aiDraftSubtasks
        .filter(item => item.checked)
        .map(item => item.title.trim())
        .filter(Boolean)

      if (subtasks.length === 0) {
        this.aiError = '至少保留一个子任务'
        return
      }

      this.aiLoading = true
      this.aiError = ''
      try {
        const result = await todoApi.aiCommit(parentTitle, this.aiGoalDueDate || null, subtasks)
        if (result && result.todo) {
          this.todos.unshift(this.normalizeTodo(result.todo))
        } else {
          await this.fetchTodos()
        }
        this.closeAiDrawer()
      } catch (error) {
        this.aiError = error.response?.data?.error || '写入任务失败，请重试'
      } finally {
        this.aiLoading = false
      }
    }
  }
}
