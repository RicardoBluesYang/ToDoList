<template>
  <div class="admin-page">
    <section class="admin-shell">
      <header class="admin-header">
        <div>
          <p class="admin-kicker">数据后台</p>
          <h2>系统概览</h2>
          <p class="admin-subtitle">查看全站任务数据、AI调用情况和用户状态。</p>
        </div>
        <div class="admin-actions">
          <button class="secondary-action" @click="goTasks">返回任务页</button>
          <button class="danger-action" @click="handleLogout">退出登录</button>
        </div>
      </header>

      <p v-if="errorMsg" class="admin-error" role="alert">{{ errorMsg }}</p>

      <section class="stats-grid">
        <article class="stat-card">
          <span>用户总数</span>
          <strong>{{ overview.totalUsers || 0 }}</strong>
        </article>
        <article class="stat-card">
          <span>任务总数</span>
          <strong>{{ overview.totalTodos || 0 }}</strong>
        </article>
        <article class="stat-card">
          <span>任务完成率</span>
          <strong>{{ formatPercent(overview.completionRate) }}</strong>
        </article>
        <article class="stat-card">
          <span>逾期任务</span>
          <strong>{{ overview.overdueTodos || 0 }}</strong>
        </article>
        <article class="stat-card">
          <span>子任务总数</span>
          <strong>{{ overview.totalSubtasks || 0 }}</strong>
        </article>
        <article class="stat-card">
          <span>AI调用成功率</span>
          <strong>{{ formatPercent(aiStats.successRate) }}</strong>
        </article>
      </section>

      <section class="admin-panel">
        <div class="panel-heading">
          <div>
            <h3>AI调用统计</h3>
            <p>只统计调用结果，不保存输入内容。</p>
          </div>
        </div>
        <div class="ai-stats-row">
          <div>
            <span>总调用</span>
            <strong>{{ aiStats.totalCalls || 0 }}</strong>
          </div>
          <div>
            <span>成功</span>
            <strong>{{ aiStats.successCalls || 0 }}</strong>
          </div>
          <div>
            <span>失败</span>
            <strong>{{ aiStats.failedCalls || 0 }}</strong>
          </div>
          <div>
            <span>最近调用</span>
            <strong>{{ formatDate(aiStats.latestCallAt) }}</strong>
          </div>
        </div>
      </section>

      <section class="admin-panel">
        <div class="panel-heading">
          <div>
            <h3>用户管理</h3>
            <p>删除普通用户会同步删除该用户的任务和子任务。</p>
          </div>
          <button class="secondary-action" :disabled="loading" @click="fetchAdminData">
            {{ loading ? '刷新中...' : '刷新' }}
          </button>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>用户</th>
                <th>注册时间</th>
                <th>任务数</th>
                <th>已完成</th>
                <th>未完成</th>
                <th>逾期</th>
                <th>子任务</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td>
                  <span class="username-cell">{{ user.username }}</span>
                </td>
                <td>{{ formatDate(user.createdAt) }}</td>
                <td>{{ user.todoCount || 0 }}</td>
                <td>{{ user.completedTodoCount || 0 }}</td>
                <td>{{ user.activeTodoCount || 0 }}</td>
                <td>{{ user.overdueTodoCount || 0 }}</td>
                <td>{{ user.subtaskCount || 0 }}</td>
                <td>
                  <button
                    class="delete-user-btn"
                    :disabled="user.username === 'admin' || deletingUserId === user.id"
                    @click="deleteUser(user)"
                  >
                    {{ deletingUserId === user.id ? '删除中...' : '删除' }}
                  </button>
                </td>
              </tr>
              <tr v-if="!loading && users.length === 0">
                <td colspan="8" class="empty-table">暂无用户数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  </div>
</template>

<script>
import todoApi from '../api/todo'

export default {
  name: 'Admin',
  data() {
    return {
      loading: false,
      deletingUserId: null,
      errorMsg: '',
      overview: {},
      aiStats: {},
      users: []
    }
  },
  created() {
    if (localStorage.getItem('username') !== 'admin') {
      this.$router.replace('/tasks')
      return
    }
    this.fetchAdminData()
  },
  methods: {
    async fetchAdminData() {
      this.loading = true
      this.errorMsg = ''
      try {
        const [overview, aiStats, users] = await Promise.all([
          todoApi.getAdminOverview(),
          todoApi.getAdminAiStats(),
          todoApi.getAdminUsers()
        ])
        this.overview = overview || {}
        this.aiStats = aiStats || {}
        this.users = Array.isArray(users) ? users : []
      } catch (error) {
        if (error.response?.status === 403) {
          this.errorMsg = '当前账号没有后台权限'
          this.$router.replace('/tasks')
          return
        }
        this.errorMsg = error.response?.data?.error || '后台数据加载失败'
      } finally {
        this.loading = false
      }
    },

    async deleteUser(user) {
      if (user.username === 'admin') return
      const confirmed = window.confirm(`确认删除用户「${user.username}」？`)
      if (!confirmed) return

      this.deletingUserId = user.id
      this.errorMsg = ''
      try {
        await todoApi.deleteAdminUser(user.id)
        await this.fetchAdminData()
      } catch (error) {
        this.errorMsg = error.response?.data?.error || '删除用户失败'
      } finally {
        this.deletingUserId = null
      }
    },

    goTasks() {
      this.$router.push('/tasks')
    },

    handleLogout() {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      this.$router.push('/login')
    },

    formatPercent(value) {
      return `${Number(value || 0).toFixed(2)}%`
    },

    formatDate(value) {
      if (!value) return '-'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return String(value)
      return date.toLocaleString('zh-CN', { hour12: false })
    }
  }
}
</script>

<style scoped>
.admin-shell {
  border: 1px solid var(--color-line);
  padding: 44px 48px 28px;
}

.admin-header,
.panel-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
}

.admin-header {
  margin-bottom: 36px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-line);
}

.admin-kicker {
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 400;
  letter-spacing: 0.18em;
  color: var(--color-muted);
  text-transform: uppercase;
  margin-bottom: 10px;
}

.admin-header h2 {
  font-family: var(--font-serif);
  font-size: clamp(34px, 3.5vw, 48px);
  font-weight: 400;
  letter-spacing: -0.01em;
  color: var(--color-ink);
}

.admin-subtitle,
.panel-heading p {
  margin-top: 10px;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-muted);
  line-height: 1.6;
}

.admin-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.secondary-action,
.danger-action,
.delete-user-btn {
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 400;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  padding: 8px 18px;
  border: 1px solid var(--color-line);
  background: transparent;
  color: var(--color-ink);
  transition: background-color var(--dur-fast), border-color var(--dur-fast), color var(--dur-fast);
}

.secondary-action:hover:enabled {
  background: var(--color-ink);
  border-color: var(--color-ink);
  color: var(--color-white);
}

.danger-action {
  color: var(--color-danger);
  border-color: var(--color-line);
}

.danger-action:hover {
  background: var(--color-danger);
  border-color: var(--color-danger);
  color: var(--color-white);
}

.delete-user-btn {
  font-size: 10px;
  padding: 6px 14px;
  color: var(--color-danger);
  border-color: var(--color-line);
}

.delete-user-btn:hover:not(:disabled) {
  background: var(--color-danger);
  border-color: var(--color-danger);
  color: var(--color-white);
}

.delete-user-btn:disabled {
  opacity: 0.3;
}

.admin-error {
  margin-bottom: 20px;
  padding: 14px 16px;
  border: 1px solid var(--color-danger);
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-danger);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  background: var(--color-danger-dim);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0;
  border: 1px solid var(--color-line);
  margin-bottom: 20px;
}

.stat-card,
.admin-panel {
  border: none;
}

.stat-card {
  padding: 20px 24px;
  border-right: 1px solid var(--color-line);
  border-bottom: 1px solid var(--color-line);
}

.stat-card:nth-child(3n) {
  border-right: none;
}

.stat-card:nth-child(n+4) {
  border-bottom: none;
}

.stat-card span,
.ai-stats-row span {
  display: block;
  font-family: var(--font-mono);
  font-size: 10px;
  font-weight: 400;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: var(--color-muted);
  margin-bottom: 12px;
}

.stat-card strong {
  font-family: var(--font-serif);
  font-size: 32px;
  font-weight: 400;
  color: var(--color-ink);
}

.admin-panel {
  margin-top: 20px;
  border: 1px solid var(--color-line);
  padding: 24px;
}

.panel-heading h3 {
  font-family: var(--font-serif);
  font-size: 24px;
  font-weight: 400;
  color: var(--color-ink);
}

.ai-stats-row {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  border-top: 1px solid var(--color-line);
}

.ai-stats-row div {
  padding: 16px 18px;
  border-right: 1px solid var(--color-line);
}

.ai-stats-row div:last-child {
  border-right: none;
}

.ai-stats-row strong {
  font-family: var(--font-serif);
  font-size: 22px;
  font-weight: 400;
  color: var(--color-ink);
}

.table-wrap {
  margin-top: 18px;
  overflow-x: auto;
  border-top: 1px solid var(--color-line);
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

th, td {
  border-bottom: 1px solid var(--color-line);
  padding: 14px 12px;
  text-align: left;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-ink);
}

th {
  font-size: 10px;
  font-weight: 400;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-muted);
  padding-bottom: 16px;
}

td {
  color: var(--color-ink);
}

.username-cell {
  font-weight: 700;
  letter-spacing: 0.04em;
}

.empty-table {
  color: var(--color-muted);
  text-align: center;
  padding: 40px 16px;
  font-style: italic;
}

@media (max-width: 760px) {
  .admin-shell {
    padding: 28px 20px;
    border-left: none;
    border-right: none;
  }

  .admin-header,
  .panel-heading {
    flex-direction: column;
  }

  .admin-actions,
  .panel-heading .secondary-action {
    width: 100%;
  }

  .secondary-action,
  .danger-action {
    width: 100%;
  }

  .stats-grid,
  .ai-stats-row {
    grid-template-columns: 1fr;
  }

  .stat-card {
    border-right: none;
    border-bottom: 1px solid var(--color-line);
  }

  .stat-card:last-child {
    border-bottom: none;
  }

  .stat-card:nth-child(n+4) {
    border-bottom: 1px solid var(--color-line);
  }

  .stat-card:last-child {
    border-bottom: none;
  }

  .ai-stats-row div {
    border-right: none;
    border-bottom: 1px solid var(--color-line);
  }

  .ai-stats-row div:last-child {
    border-bottom: none;
  }
}
</style>
