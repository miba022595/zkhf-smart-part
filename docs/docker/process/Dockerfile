# 使用与项目一致的 Java 21 运行时镜像
FROM eclipse-temurin:21-jre-alpine

# ----- 基础配置 -----
WORKDIR /app

# 设置时区（中国时区）
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# ----- 应用挂载目录 -----
# 不在构建阶段将业务包和依赖直接打入镜像，运行时通过挂载提供。
# 示例：
#   docker run -v /host/app/epmis-process-service.jar:/app/app.jar ...

# ----- 启动命令 -----
# 使用 exec 避免创建子进程，减少内存占用
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

# 允许运行时覆盖参数（示例：docker run -e "JAVA_OPTS=-Xmx512m"）
CMD [""]
