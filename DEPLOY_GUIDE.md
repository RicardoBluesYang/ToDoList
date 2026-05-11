# ToDoList 项目部署指南

恭喜你完成了毕设项目的开发！这是一份保姆级的部署指南，基于 Docker 实现一键部署。

## 1. 准备工作（在本地电脑操作）

### 1.1 打包后端代码
1. 打开 IDEA，在右侧找到 `Maven` 面板。
2. 展开 `todolist` -> `Lifecycle`。
3. 双击运行 `clean`，然后双击运行 `package`。
4. 确认在 `backend/target/` 目录下生成了 `todolist-0.0.1-SNAPSHOT.jar` 文件。

### 1.2 构建前端代码
1. 在终端中进入 `frontend` 目录：`cd frontend`
2. 运行构建命令：`npm run build`
3. 确认在 `frontend/` 目录下生成了 `dist` 文件夹。

### 1.3 准备上传包
你需要将以下文件/文件夹打包（或者通过 SFTP 直接上传）到你的云服务器上的一个新建文件夹中（比如 `/opt/todolist`）：
- `docker-compose.yml` (根目录下)
- `db_update_v2.sql` (根目录下，改名为 `init.sql` 更直观，但 docker-compose 里我已经写了映射，原名上传也可)
- `backend/` 文件夹（只需包含 `Dockerfile` 和 `target/todolist-0.0.1-SNAPSHOT.jar` 即可，源码不用传）
- `frontend/` 文件夹（只需包含 `Dockerfile`、`nginx.conf` 和 `dist/` 文件夹即可，源码不用传）

---

## 2. 服务器环境安装（在云服务器上操作）

假设你购买的是 Ubuntu 22.04 服务器。

### 2.1 安装 Docker 和 Docker Compose
通过 SSH 连接到服务器，依次执行以下命令：

```bash
# 更新软件包列表
sudo apt update

# 安装 Docker
sudo apt install -y docker.io

# 启动 Docker 并设置开机自启
sudo systemctl start docker
sudo systemctl enable docker

# 安装 Docker Compose
sudo apt install -y docker-compose
```

### 2.2 放行端口
**非常重要：** 请去你的云服务器控制台（阿里云/腾讯云），找到“安全组”或“防火墙”设置，添加规则放行 **80** 端口和 **3306** 端口。

---

## 3. 一键启动部署（在云服务器上操作）

1. 进入你上传文件的目录：
```bash
cd /opt/todolist
```

2. 检查目录结构，应该是这样的：
```text
/opt/todolist/
├── docker-compose.yml
├── db_update_v2.sql
├── backend/
│   ├── Dockerfile
│   └── target/
│       └── todolist-0.0.1-SNAPSHOT.jar
└── frontend/
    ├── Dockerfile
    ├── nginx.conf
    └── dist/
        ├── index.html
        └── assets/
```

3. **执行一键启动命令：**
```bash
sudo docker-compose up -d --build
```

这个命令会自动：
1. 拉取 MySQL 镜像并初始化你的数据库。
2. 将你的后端 Jar 包构建成 Docker 镜像并运行。
3. 将你的前端 Dist 文件和 Nginx 配置文件构建成镜像并运行，同时处理好跨域和路由转发。

### 4. 验收成果！
在你的电脑浏览器中，输入你服务器的**公网 IP 地址**（例如 `http://123.45.67.89`，不需要加端口号，默认就是 80）。

你应该能看到登录页面了！
使用 `admin` / `123456` 登录测试一下。

**毕设部署大功告成！🎉**
