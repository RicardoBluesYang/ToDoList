<template>
  <div class="todo-container">
    <div class="header-actions">
      <span class="welcome-text">欢迎, {{ username }}</span>
      <button @click="handleLogout" class="logout-btn">退出登录</button>
    </div>
    <h2>待办事项列表</h2>
    <div class="todo-input">
      <input 
        type="text" 
        v-model="newTodo" 
        @keyup.enter="addTodo" 
        placeholder="请输入待办事项，按回车添加"
      />
      <button @click="addTodo">添加</button>
    </div>
    
    <!-- 添加过滤按钮 -->
    <div class="todo-filters">
      <button 
        @click="filter = 'all'" 
        :class="{ active: filter === 'all' }"
      >全部</button>
      <button 
        @click="filter = 'active'" 
        :class="{ active: filter === 'active' }"
      >未完成</button>
      <button 
        @click="filter = 'completed'" 
        :class="{ active: filter === 'completed' }"
      >已完成</button>
    </div>


     <!-- 使用过滤后的列表 -->
    <ul class="todo-list">
      <li v-for="(todo, index) in filteredTodos" :key="todo.id" :class="{ completed: todo.isCompleted }">
        <input type="checkbox" :checked="todo.isCompleted" @change="toggleTodoStatus(todo)">
        <span>{{ todo.title }}</span>
        <button @click="removeTodo(index)" class="delete-btn">删除</button>
      </li>
    </ul>
    
    <div class="todo-footer" v-if="todos.length > 0">
      <span>{{ completedCount }}/{{ todos.length }} 已完成</span>
      <button @click="clearCompleted">清除已完成</button>
    </div>
  </div>
</template>

<script>
import todoListScript from './TodoListScript.js';
export default todoListScript;
</script>

<style src="../assets/styles/todolist.css"></style>
<style scoped>
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.welcome-text {
  font-weight: bold;
  color: #333;
}
.logout-btn {
  padding: 5px 15px;
  background-color: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.logout-btn:hover {
  background-color: #ff7875;
}
</style>

