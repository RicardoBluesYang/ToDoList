# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

待办清单（TodoList）— 全栈 Web 应用。前端 Vue 3 + Vite + Vue Router，后端 Spring Boot 3.2 + MyBatis，数据库 MySQL 8.0，通过 Docker Compose 一键部署。

## 常用命令

### 前端（`frontend/`）

```bash
cd frontend
npm install          # 安装依赖
npm run dev          # 启动开发服务器（Vite，默认 http://localhost:5173）
npm run build        # 生产构建，输出到 dist/
```

### 后端（`backend/`）

```bash
cd backend
# 开发时直接在 IDE 运行 TodolistApplication.java（application.yml 连接本地 MySQL）
mvn clean package    # 打包，输出 target/todolist-0.0.1-SNAPSHOT.jar
```

### Docker 部署（根目录）

```bash
docker compose up -d         # 后台启动全部服务（MySQL + 后端 + 前端 Nginx）
docker compose down          # 停止并删除容器（数据卷保留）
docker compose up -d --build backend  # 后端更新后重建并重启
docker compose logs --tail=200 -f backend  # 查看后端实时日志
```

## 架构

### 整体分层

```
浏览器 → Nginx(:80) → 前端静态文件（dist/）
                    → /api/* 反向代理 → Spring Boot(:8080) → MySQL(:3306)
```

- **开发模式**：Vite dev server 直接连 `http://localhost:8080/api`（通过 `VITE_API_BASE_URL` 环境变量配置）
- **生产模式**：Nginx 同时托管前端静态文件和反向代理后端 API

### 后端分层（Spring Boot）

- **Controller** — `controller/TodoController.java`（任务 CRUD + 子任务）、`UserController.java`（注册/登录）、`AiController.java`（AI 拆解）、`AdminController.java`（管理后台）
- **Service** — `TodoService` / `TodoServiceImpl`（业务逻辑 + 权限校验：只能操作自己的数据）
- **Mapper** — `TodoMapper`、`UserMapper`、`TodoSubtaskMapper`、`AdminMapper`、`AiCallStatMapper`，XML 映射文件在 `resources/mapper/`
- **Entity** — `TodoItem`、`TodoSubtask`、`User`
- **Config** — `WebConfig.java`：CORS 全局放行 + 登录拦截器注册
- **Interceptor** — `LoginInterceptor.java`：从 `Authorization: Bearer <token>` 头解析 JWT，注入 `userId` 和 `username` 到 request attribute。拦截 `/api/todos/**`、`/api/ai/**`、`/api/admin/**`，放行 `/api/users/login` 和 `/api/users/register`
- **Utils** — `JwtUtils.java`：JWT 生成与解析，密钥每次启动随机生成，有效期 24 小时

### 前端分层（Vue 3）

- **`router/index.js`** — 4 条路由：`/login`、`/register`、`/tasks`（需登录）、`/admin`（需 admin 用户）。`beforeEach` 守卫检查 token 和 admin 权限
- **`api/todo.js`** — axios 实例，baseURL 默认 `http://localhost:8080/api`，请求拦截器自动带 Bearer token，响应拦截器在 401 时清空存储并跳转登录页
- **`components/ToDoList.vue`** + **`TodoListScript.js`** — 主页面，选项式 API。任务列表、子任务展开/编辑、AI 拆解抽屉、过滤（全部/未完成/已完成）
- **`views/Login.vue`** / **`views/Register.vue`** / **`views/Admin.vue`** — 登录、注册、管理后台

### 数据库

4 张表：`user`、`todo_item`、`todo_subtask`、`ai_call_stat`。`todo_item` 和 `todo_subtask` 通过外键级联删除关联到 `user`。

- `db_init.sql` — 完整建库建表 + admin 初始数据
- `db_update_v2/v3/v4.sql` — 增量升级脚本，`v4` 新增 `ai_call_stat` 表

### 配置要点

- **application.yml**（本地开发）：数据库连 `localhost:3306`，AI 默认用智谱 GLM
- **application-prod.yml**（Docker 生产）：数据库 host 用 `db`（容器名），密码和 AI 配置通过环境变量注入
- **docker-compose.yml**：MySQL 字符集 `utf8mb4`，时区 `+08:00`，数据持久化到 `./mysql_data`
- 测试账号：`admin` / `123456`（硬编码在 `db_init.sql`）
