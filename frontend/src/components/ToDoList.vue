<template>
  <div class="todo-page">
    <section class="todo-container">
      <header class="todo-header">
        <div class="header-texts">
          <p class="welcome-text">你好，{{ username }}</p>
          <h2>任务清单</h2>
          <p class="header-subtitle">把复杂目标拆成清晰小步，按节奏推进。</p>
        </div>
        <div class="header-actions">
          <button v-if="isAdmin" class="admin-entry-btn" @click="goAdmin">数据后台</button>
          <button class="ai-entry-btn" @click="openAiDrawer">AI任务工坊</button>
          <button @click="handleLogout" class="logout-btn">退出登录</button>
        </div>
      </header>

      <section class="overview-bar">
        <div class="overview-chip">
          <span class="chip-label">总任务</span>
          <span class="chip-value">{{ todos.length }}</span>
        </div>
        <div class="overview-chip">
          <span class="chip-label">已完成</span>
          <span class="chip-value">{{ completedCount }}</span>
        </div>
        <div class="overview-chip">
          <span class="chip-label">完成率</span>
          <span class="chip-value">{{ todos.length ? Math.round((completedCount / todos.length) * 100) : 0 }}%</span>
        </div>
      </section>

      <div class="todo-input">
        <input
          type="text"
          v-model="newTodo"
          @keyup.enter="addTodo"
          placeholder="请输入待办事项，回车可快速添加"
        />
        <select v-model="newTodoPriority" class="priority-select" aria-label="优先级">
          <option value="" disabled hidden>优先级</option>
          <option :value="0">低</option>
          <option :value="1">中</option>
          <option :value="2">高</option>
        </select>
        <input type="datetime-local" v-model="newTodoDueDate" class="due-input" placeholder="截止日期" aria-label="截止时间" />
        <button @click="addTodo">添加</button>
      </div>

      <div class="todo-filters">
        <button @click="filter = 'all'" :class="{ active: filter === 'all' }">全部</button>
        <button @click="filter = 'active'" :class="{ active: filter === 'active' }">未完成</button>
        <button @click="filter = 'completed'" :class="{ active: filter === 'completed' }">已完成</button>
      </div>

      <div v-if="filteredTodos.length === 0" class="empty-state">
        <p class="empty-title">当前筛选下暂无任务</p>
        <p class="empty-desc">
          {{
            filter === 'all'
              ? '先创建一条任务，开始今天的计划。'
              : filter === 'active'
                ? '当前没有未完成任务。'
                : '还没有已完成任务，先去完成一条吧。'
          }}
        </p>
      </div>

      <ul v-else class="todo-list">
        <li
          v-for="todo in filteredTodos"
          :key="todo.id"
          :data-todo-id="todo.id"
          :class="{
            completed: todo.isCompleted,
            animating: todo._animating,
            overdue: isOverdue(todo),
            'just-added': todo._justAdded,
            'priority-high': todo.priority === 2,
            'priority-mid': todo.priority === 1,
            'priority-low': todo.priority === 0
          }"
        >
          <div class="todo-row">
            <input type="checkbox" :checked="todo.isCompleted" @change="toggleTodoStatus(todo)" />

            <div class="todo-main">
              <div v-if="editingTodoId === todo.id" class="todo-edit-form">
                <input
                  type="text"
                  v-model="editForm.title"
                  @keyup.enter="saveEdit(todo.id)"
                  placeholder="任务标题"
                />
                <div class="edit-row">
                  <select v-model="editForm.priority" class="priority-select" aria-label="优先级">
                    <option :value="0">低</option>
                    <option :value="1">中</option>
                    <option :value="2">高</option>
                  </select>
                  <input type="datetime-local" v-model="editForm.dueDate" class="due-input" />
                </div>
                <textarea
                  v-model="editForm.notes"
                  class="notes-input"
                  placeholder="备注（可选）"
                  rows="2"
                ></textarea>
              </div>

              <div v-else class="todo-display">
                <span class="todo-title">{{ todo.title }}</span>
                <span
                  v-if="todo.dueDate"
                  class="due-chip"
                  :class="{ overdue: isOverdue(todo), done: todo.isCompleted }"
                >
                  截止：{{ formatDueDate(todo.dueDate) }}
                </span>
                <span v-if="todo.notes" class="notes-indicator" :class="{ active: expandedNotes[todo.id] }" @click.stop="toggleNotes(todo.id)">···</span>
              </div>

              <div v-if="expandedNotes[todo.id] && !editingTodoId" class="notes-inline">
                <p>{{ todo.notes }}</p>
              </div>

              <div class="progress-wrap" v-if="!editingTodoId || editingTodoId !== todo.id">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: subtaskProgress(todo) + '%' }"></div>
                </div>
                <span class="progress-label">{{ subtaskProgress(todo) }}%</span>
              </div>
            </div>

            <div class="todo-actions">
              <button v-if="editingTodoId === todo.id" @click="saveEdit(todo.id)" class="edit-btn">保存</button>
              <button v-if="editingTodoId === todo.id" @click="cancelEdit" class="secondary-btn">取消</button>
              <button v-else @click="startEdit(todo)" class="edit-btn">编辑</button>
              <button @click="removeTodo(todo.id)" class="delete-btn">删除</button>
              <button @click="toggleSubtasks(todo.id)" class="secondary-btn">
                {{ isSubtasksExpanded(todo.id) ? '收起子任务' : '子任务' }} ({{ todo.subtasks ? todo.subtasks.length : 0 }})
              </button>
            </div>
          </div>

          <transition
            @before-enter="onExpandBeforeEnter"
            @enter="onExpandEnter"
            @after-enter="onExpandAfter"
            @before-leave="onExpandBeforeLeave"
            @leave="onExpandLeave"
            @after-leave="onExpandAfter"
          >
            <div v-if="isSubtasksExpanded(todo.id)" class="subtask-panel">
              <ul class="subtask-list">
                <li
                  v-for="(subtask, index) in todo.subtasks"
                  :key="subtask.id"
                  :data-subtask-index="index"
                  :class="{
                    completed: subtask.isCompleted,
                    'drag-over': dragTarget.todoId === todo.id && dragTarget.index === index
                  }"
                  draggable="true"
                  @dragstart.stop="onDragStart($event, todo.id, subtask.id, index)"
                  @dragover.prevent="onDragOver($event, todo.id, index)"
                  @dragend="onDragEnd"
                  @drop.stop="onDrop(todo.id, index)"
                >
                  <span
                    class="drag-handle"
                    title="拖动排序"
                    @touchstart="onTouchStart($event, todo.id, subtask.id, index)"
                    @touchmove="onTouchMove($event, todo.id)"
                    @touchend="onTouchEnd($event, todo.id)"
                  >⋮⋮</span>
                  <input type="checkbox" :checked="subtask.isCompleted" @change="toggleSubtaskStatus(todo, subtask)" />

                  <div class="subtask-content">
                    <input
                      v-if="isEditingSubtask(todo.id, subtask.id)"
                      type="text"
                      v-model="editSubtaskForm.title"
                      @keyup.enter="saveEditSubtask(todo.id, subtask.id)"
                      class="subtask-edit-input"
                    />
                    <span v-else class="subtask-title">{{ subtask.title }}</span>
                  </div>

                  <div class="subtask-actions">
                    <button
                      v-if="isEditingSubtask(todo.id, subtask.id)"
                      @click="saveEditSubtask(todo.id, subtask.id)"
                      class="edit-btn"
                    >
                      保存
                    </button>
                    <button
                      v-if="isEditingSubtask(todo.id, subtask.id)"
                      @click="cancelEditSubtask"
                      class="secondary-btn"
                    >
                      取消
                    </button>
                    <button v-else @click="startEditSubtask(todo.id, subtask)" class="edit-btn">编辑</button>
                    <button @click="removeSubtask(todo.id, subtask.id)" class="delete-btn">删除</button>
                  </div>
                </li>
              </ul>

              <div class="subtask-input">
                <input
                  type="text"
                  v-model="newSubtaskMap[todo.id]"
                  @keyup.enter="addSubtask(todo.id)"
                  placeholder="请输入子任务"
                />
                <button @click="addSubtask(todo.id)">添加子任务</button>
              </div>
            </div>
          </transition>
        </li>
      </ul>

      <div class="todo-footer" v-if="todos.length > 0">
        <span>已完成 {{ completedCount }}/{{ todos.length }}</span>
        <button @click="clearCompleted">清除已完成</button>
      </div>
    </section>

    <transition name="drawer-fade">
      <div v-if="aiDrawerVisible" class="ai-drawer-mask" @click.self="closeAiDrawer">
        <aside class="ai-drawer">
          <header class="ai-drawer-header">
            <div>
              <h3>AI任务工坊</h3>
              <p>输入目标任务，生成 5-8 条可执行子任务。</p>
            </div>
            <button class="drawer-close-btn" @click="closeAiDrawer">关闭</button>
          </header>

          <section class="ai-input-section">
            <label>目标任务</label>
            <textarea
              v-model="aiGoalTitle"
              placeholder="例如：完成毕业论文第三章并准备答辩PPT"
              rows="3"
            />

            <label>截止时间（可选）</label>
            <input type="datetime-local" v-model="aiGoalDueDate" />

            <button class="ai-generate-btn" :disabled="aiLoading" @click="generateAiDecompose">
              {{ aiLoading ? '拆解中...' : '开始AI拆解' }}
            </button>
          </section>

          <section v-if="aiError" class="ai-error-box" role="alert" aria-live="assertive">
            <p>{{ aiError }}</p>
            <button class="secondary-btn" :disabled="aiLoading" @click="generateAiDecompose">重试</button>
          </section>

          <section v-if="hasAiDraft" class="ai-result-section">
            <label>主任务标题（可编辑）</label>
            <input type="text" v-model="aiDraftParentTitle" />

            <div class="ai-subtask-header">
              <span>子任务（可勾选、可编辑）</span>
              <button class="secondary-btn" @click="addAiDraftSubtask">新增一条</button>
            </div>

            <ul class="ai-subtask-list">
              <li v-for="(item, index) in aiDraftSubtasks" :key="item.id">
                <input type="checkbox" v-model="item.checked" />
                <input type="text" v-model="item.title" />
                <button class="delete-btn" @click="removeAiDraftSubtask(index)">删除</button>
              </li>
            </ul>

            <button class="ai-commit-btn" :disabled="aiLoading" @click="commitAiDecompose">确认添加到清单</button>
          </section>
        </aside>
      </div>
    </transition>
  </div>
</template>

<script>
import todoListScript from './TodoListScript.js'

export default todoListScript
</script>

<style src="../assets/styles/todolist.css"></style>
