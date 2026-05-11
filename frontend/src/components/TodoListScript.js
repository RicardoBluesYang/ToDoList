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
      newTodoPriority: '',
      todos: [],
      filter: 'all',
      username: localStorage.getItem('username') || '用户',
      editingTodoId: null,
      editForm: {
        title: '',
        dueDate: '',
        priority: 0,
        notes: ''
      },
      expandedSubtasks: {},
      expandedNotes: {},
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
      aiDraftSubtasks: [],
      dragItem: null,
      dragTarget: { todoId: null, index: -1 },
      touchDrag: null
    }
  },
  created() {
    this.fetchTodos()
  },
  computed: {
    isAdmin() {
      return this.username === 'admin'
    },
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
    goAdmin() {
      this.$router.push('/admin')
    },

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
        priority: todo.priority != null ? todo.priority : 0,
        notes: todo.notes || '',
        subtasks: this.applySubtaskOrder(todo.id, Array.isArray(todo.subtasks) ? todo.subtasks : [])
      }
    },

    applySubtaskOrder(todoId, subtasks) {
      try {
        const raw = localStorage.getItem(`subtask_order_${todoId}`)
        if (!raw) return subtasks
        const order = JSON.parse(raw)
        if (!Array.isArray(order) || order.length !== subtasks.length) return subtasks
        const map = new Map(subtasks.map(s => [s.id, s]))
        const ordered = order.map(id => map.get(id)).filter(Boolean)
        return ordered.length === subtasks.length ? ordered : subtasks
      } catch {
        return subtasks
      }
    },

    saveSubtaskOrder(todoId, subtasks) {
      const order = subtasks.map(s => s.id)
      localStorage.setItem(`subtask_order_${todoId}`, JSON.stringify(order))
    },

    onDragStart(e, todoId, subtaskId, index) {
      this.dragItem = { todoId, subtaskId, index }
      e.dataTransfer.effectAllowed = 'move'
      e.dataTransfer.setData('text/plain', String(subtaskId))
      e.currentTarget.classList.add('dragging')
    },

    onDragOver(e, todoId, index) {
      if (!this.dragItem || this.dragItem.todoId !== todoId) return
      this.dragTarget = { todoId, index }
    },

    onDragEnd(e) {
      e.currentTarget.classList.remove('dragging')
      this.dragItem = null
      this.dragTarget = { todoId: null, index: -1 }
    },

    onDrop(todoId, targetIndex) {
      if (!this.dragItem || this.dragItem.todoId !== todoId) return
      const fromIndex = this.dragItem.index
      if (fromIndex === targetIndex) {
        this.dragItem = null
        this.dragTarget = { todoId: null, index: -1 }
        return
      }
      const todo = this.findTodo(todoId)
      if (!todo || !todo.subtasks) return
      this.reorderSubtasks(todo, fromIndex, targetIndex)
      this.dragItem = null
      this.dragTarget = { todoId: null, index: -1 }
    },

    reorderSubtasks(todo, fromIndex, targetIndex) {
      const items = [...todo.subtasks]
      const [moved] = items.splice(fromIndex, 1)
      items.splice(targetIndex, 0, moved)
      todo.subtasks = items
      this.saveSubtaskOrder(todo.id, items)
    },

    onTouchStart(e, todoId, subtaskId, index) {
      const li = e.currentTarget.closest('li')
      this.touchDrag = { todoId, subtaskId, fromIndex: index, targetIndex: index, el: li }
      if (li) li.classList.add('touch-dragging')
      e.preventDefault()
    },

    onTouchMove(e, todoId) {
      if (!this.touchDrag || this.touchDrag.todoId !== todoId) return
      e.preventDefault()
      const touch = e.touches[0]
      let el = document.elementFromPoint(touch.clientX, touch.clientY)
      while (el && el.tagName !== 'LI') {
        el = el.parentElement
      }
      if (!el || !el.hasAttribute('data-subtask-index')) return
      const targetIndex = parseInt(el.getAttribute('data-subtask-index'))
      if (isNaN(targetIndex)) return
      this.touchDrag.targetIndex = targetIndex
      this.dragTarget = { todoId, index: targetIndex }
    },

    onTouchEnd(e, todoId) {
      if (!this.touchDrag || this.touchDrag.todoId !== todoId) return
      const { fromIndex, targetIndex, el } = this.touchDrag
      if (el) el.classList.remove('touch-dragging')
      this.touchDrag = null
      this.dragTarget = { todoId: null, index: -1 }
      if (fromIndex === targetIndex) return
      const todo = this.findTodo(todoId)
      if (!todo || !todo.subtasks) return
      this.reorderSubtasks(todo, fromIndex, targetIndex)
    },

    subtaskProgress(todo) {
      const subs = todo.subtasks
      if (!subs || subs.length === 0) {
        return todo.isCompleted ? 100 : 0
      }
      const done = subs.filter(s => s.isCompleted).length
      return Math.round((done / subs.length) * 100)
    },

    async addTodo() {
      const title = this.newTodo.trim()
      if (!title) return

      try {
        const created = await todoApi.createTodo(
          title,
          this.newTodoDueDate || null,
          this.newTodoPriority || 0,
          null
        )
        const todo = this.normalizeTodo(created)
        todo._justAdded = true
        this.todos.unshift(todo)
        this.newTodo = ''
        this.newTodoDueDate = ''
        this.newTodoPriority = ''
        setTimeout(() => { todo._justAdded = false }, 500)
      } catch (error) {
        console.error('添加任务失败:', error)
        alert(error.response?.data?.error || '添加任务失败')
      }
    },

    startEdit(todo) {
      this.editingTodoId = todo.id
      this.editForm.title = todo.title
      this.editForm.dueDate = this.toDateTimeInput(todo.dueDate)
      this.editForm.priority = todo.priority != null ? todo.priority : 0
      this.editForm.notes = todo.notes || ''
    },

    cancelEdit() {
      this.editingTodoId = null
      this.editForm.title = ''
      this.editForm.dueDate = ''
      this.editForm.priority = 0
      this.editForm.notes = ''
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
          dueDate: this.editForm.dueDate || null,
          priority: this.editForm.priority,
          notes: this.editForm.notes || null
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

      if (nextStatus) {
        todo._animating = true
        try {
          await todoApi.updateTodo(todo.id, { isCompleted: true })
        } catch (error) {
          todo._animating = false
          console.error('更新任务状态失败:', error)
          alert(error.response?.data?.error || '更新任务状态失败')
          return
        }

        await new Promise(resolve => setTimeout(resolve, 560))
        todo._animating = false

        const el = this.$el.querySelector(`[data-todo-id="${todo.id}"]`)
        const fromY = el ? el.getBoundingClientRect().top : 0

        todo.isCompleted = true
        await this.$nextTick()

        if (el) {
          const toY = el.getBoundingClientRect().top
          const delta = fromY - toY
          if (Math.abs(delta) > 2) {
            el.style.transition = 'none'
            el.style.transform = `translateY(${delta}px)`
            el.getBoundingClientRect()
            el.style.transition = 'transform 0.55s cubic-bezier(0.25, 0.1, 0.25, 1)'
            el.style.transform = 'translateY(0)'
            const onEnd = () => {
              el.removeEventListener('transitionend', onEnd)
              el.style.transition = ''
              el.style.transform = ''
            }
            el.addEventListener('transitionend', onEnd)
          }
        }
        return
      }

      todo.isCompleted = false
      try {
        await todoApi.updateTodo(todo.id, { isCompleted: false })
      } catch (error) {
        todo.isCompleted = true
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

    toggleNotes(todoId) {
      this.expandedNotes[todoId] = !this.expandedNotes[todoId]
    },

    isSubtasksExpanded(todoId) {
      return !!this.expandedSubtasks[todoId]
    },

    onExpandBeforeEnter(el) {
      el.style.height = '0'
      el.style.opacity = '0'
      el.style.overflow = 'hidden'
      el.style.willChange = 'height'
    },

    onExpandEnter(el, done) {
      const h = el.scrollHeight
      el.getBoundingClientRect()
      el.style.transition = 'height 0.30s cubic-bezier(0.25, 0.1, 0.25, 1), opacity 0.25s ease'
      el.style.height = h + 'px'
      el.style.opacity = '1'
      const cleanup = () => {
        el.removeEventListener('transitionend', cleanup)
        done()
      }
      el.addEventListener('transitionend', cleanup)
    },

    onExpandAfter(el) {
      el.style.height = ''
      el.style.overflow = ''
      el.style.transition = ''
      el.style.opacity = ''
      el.style.willChange = ''
    },

    onExpandBeforeLeave(el) {
      el.style.height = el.scrollHeight + 'px'
      el.style.overflow = 'hidden'
      el.style.willChange = 'height'
      el.getBoundingClientRect()
    },

    onExpandLeave(el, done) {
      el.style.transition = 'height 0.25s cubic-bezier(0.25, 0.1, 0.25, 1), opacity 0.20s ease'
      el.style.height = '0'
      el.style.opacity = '0'
      const cleanup = () => {
        el.removeEventListener('transitionend', cleanup)
        done()
      }
      el.addEventListener('transitionend', cleanup)
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
