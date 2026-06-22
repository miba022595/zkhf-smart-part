# 使用与项目一致的 Java 21 运行时镜像
FROM eclipse-temurin:21-jre-alpine

# ----- 基础配置 -----
WORKDIR /app

# 设置时区（中国时区）
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 不在构建阶段将业务包直接打入镜像，运行时通过挂载提供。

# ----- 启动命令 -----
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

CMD [""]
