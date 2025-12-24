# ========================================
# 简化版 Dockerfile（使用本地构建的jar）
# 构建命令：mvnw clean package -DskipTests
# ========================================

FROM amazoncorretto:17-alpine

LABEL maintainer="bookstore-team"
LABEL description="Online Bookstore Backend"

WORKDIR /app

# 创建上传目录
RUN mkdir -p /app/uploads/avatars

# 复制本地构建的 jar 文件
COPY target/online-bookstore-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=docker"]
