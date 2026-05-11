# 使用 Nginx 镜像托管静态文件
FROM nginx:alpine

# 复制前端构建后的文件到 Nginx 默认目录
COPY dist/ /usr/share/nginx/html/

# 复制 Nginx 配置文件
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
