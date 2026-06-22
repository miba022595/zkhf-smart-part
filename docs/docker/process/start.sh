# 复制到部署目录后执行，例如：
# cd /data/epmis/process
#
# 重新构建镜像
# docker build --no-cache -t epmis-process:latest -f Dockerfile .
#
# 删除容器
# docker rm -f epmis-process
#
# 删除镜像
# docker rmi epmis-process:latest
#
# 启动前准备
# 1. 将业务包放到当前目录，并命名为 epmis-process-service.jar
# 2. 将当前环境的 application.yml 放到当前目录
# 3. 确保当前目录下存在 logs 文件夹
# 4. 启动后 jar 会挂载到容器内的 /app/app.jar，配置文件挂载到 /app/application.yml，日志目录挂载到 /app/logs
#
# JAVA_OPTS 参数说明
# -XX:MaxRAMPercentage=75.0：JVM 最大堆内存约使用容器可用内存的 75%
# -XX:+UseG1GC：使用 G1 垃圾回收器
# -Djava.security.egd=file:/dev/./urandom：减少容器环境下安全随机数阻塞导致的启动变慢
# -Dfile.encoding=UTF-8：指定 JVM 默认字符编码为 UTF-8
#
# 启动容器
# docker run -d --name epmis-process -p 9532:9532 -m 1g -v "$(pwd)/epmis-process-service.jar:/app/app.jar" -v "$(pwd)/application.yml:/app/application.yml" -v "$(pwd)/logs:/app/logs" -e "JAVA_OPTS=-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8" epmis-process:latest
