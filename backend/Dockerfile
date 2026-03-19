# 基础镜像使用 JDK 17
FROM openjdk:17-jdk-slim

# 作者信息
MAINTAINER YourName

# 将打包好的 jar 文件复制到容器中
COPY target/todolist-0.0.1-SNAPSHOT.jar /app.jar

# 暴露端口
EXPOSE 8080

# 运行参数，指定使用 prod 配置文件
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]
