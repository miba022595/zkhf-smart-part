# Docker 部署说明

## 1. 适用范围

本文档用于 EPMIS 四个服务的 Docker 镜像构建与容器启动说明，适用于测试环境和简单部署场景。

仓库中的脚本模板统一放在：

- `docs/docker/auth`
- `docs/docker/platform`
- `docs/docker/process`
- `docs/docker/protocol`

当前统一约定：

- 基础镜像：`eclipse-temurin:21-jre-alpine`
- 应用包挂载路径：`/app/app.jar`
- 日志目录挂载路径：`/app/logs`
- 运行方式：镜像只提供 Java 运行环境，业务 jar 通过宿主机挂载

## 2. 服务与端口

| 服务 | 模块目录 | 默认端口 | 镜像名 |
| --- | --- | --- | --- |
| `epmis-auth-service` | `epmis-auth/epmis-auth-service` | `9531` | `epmis-auth:latest` |
| `epmis-process-service` | `epmis-process/epmis-process-service` | `9532` | `epmis-process:latest` |
| `epmis-platform-service` | `epmis-platform/epmis-platform-service` | `9533` | `epmis-platform:latest` |
| `epmis-protocol-service` | `epmis-protocol/epmis-protocol-service` | `9534` | `epmis-protocol:latest` |

## 3. 打包准备

先在项目根目录打包：

```bash
mvn clean package -DskipTests
```

打包完成后，各服务 jar 默认位于：

- `epmis-auth/epmis-auth-service/target/epmis-auth-service.jar`
- `epmis-process/epmis-process-service/target/epmis-process-service.jar`
- `epmis-platform/epmis-platform-service/target/epmis-platform-service.jar`
- `epmis-protocol/epmis-protocol-service/target/epmis-protocol-service.jar`

## 4. 部署目录建议

建议每个服务先从 `docs/docker/` 复制对应脚本，再准备独立部署目录，例如：

```text
/data/epmis/
├─ auth/
│  ├─ Dockerfile              # 从 docs/docker/auth 复制
│  ├─ start.sh                # 从 docs/docker/auth 复制
│  ├─ epmis-auth-service.jar
│  ├─ logs/
│  └─ uploadPath/
├─ process/
│  ├─ Dockerfile              # 从 docs/docker/process 复制
│  ├─ start.sh                # 从 docs/docker/process 复制
│  ├─ epmis-process-service.jar
│  └─ logs/
├─ platform/
│  ├─ Dockerfile              # 从 docs/docker/platform 复制
│  ├─ start.sh                # 从 docs/docker/platform 复制
│  ├─ epmis-platform-service.jar
│  ├─ logs/
│  └─ uploadPath/
└─ protocol/
   ├─ Dockerfile              # 从 docs/docker/protocol 复制
   ├─ start.sh                # 从 docs/docker/protocol 复制
   ├─ epmis-protocol-service.jar
   └─ logs/
```

说明：

- `auth`、`platform` 建议额外准备 `uploadPath/`
- `process`、`protocol` 只需要 `jar + logs`
- `application.yml` 建议与 `jar` 放在同级目录，并挂载到容器内的 `/app/application.yml`

## 5. 通用说明

### 5.1 构建镜像

在各服务部署目录执行：

```bash
docker build --no-cache -t <image-name>:latest -f Dockerfile .
```

### 5.2 删除容器

```bash
docker rm -f <container-name>
```

### 5.3 删除镜像

```bash
docker rmi <image-name>:latest
```

### 5.4 JVM 参数说明

统一使用：

```text
-XX:MaxRAMPercentage=75.0
-XX:+UseG1GC
-Djava.security.egd=file:/dev/./urandom
-Dfile.encoding=UTF-8
```

含义：

- `-XX:MaxRAMPercentage=75.0`：JVM 最大堆内存约使用容器可用内存的 75%
- `-XX:+UseG1GC`：使用 G1 垃圾回收器
- `-Djava.security.egd=file:/dev/./urandom`：减少容器环境下安全随机数阻塞导致的启动变慢
- `-Dfile.encoding=UTF-8`：指定 JVM 默认字符编码为 UTF-8

## 6. 认证服务

目录参考：

```bash
cd /data/epmis/auth
```

重新构建镜像：

```bash
docker build --no-cache -t epmis-auth:latest -f Dockerfile .
```

删除容器：

```bash
docker rm -f epmis-auth
```

删除镜像：

```bash
docker rmi epmis-auth:latest
```

启动容器：

```bash
docker run -d --name epmis-auth -p 9531:9531 -m 1g -v "$(pwd)/epmis-auth-service.jar:/app/app.jar" -v "$(pwd)/application.yml:/app/application.yml" -v "$(pwd)/logs:/app/logs" -v "$(pwd)/uploadPath:/app/uploadPath" -e "EPMIS_PROFILE=/app/uploadPath" -e "JAVA_OPTS=-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8" epmis-auth:latest
```

说明：

- `auth` 存在上传目录配置，建议挂载 `uploadPath`
- 建议同时挂载外部 `application.yml` 到 `/app/application.yml`
- 如 Redis、MySQL 地址变更，请先修改外部配置或重新打包

## 7. 数据处理服务

目录参考：

```bash
cd /data/epmis/process
```

重新构建镜像：

```bash
docker build --no-cache -t epmis-process:latest -f Dockerfile .
```

删除容器：

```bash
docker rm -f epmis-process
```

删除镜像：

```bash
docker rmi epmis-process:latest
```

启动容器：

```bash
docker run -d --name epmis-process -p 9532:9532 -m 1g -v "$(pwd)/epmis-process-service.jar:/app/app.jar" -v "$(pwd)/application.yml:/app/application.yml" -v "$(pwd)/logs:/app/logs" -e "JAVA_OPTS=-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8" epmis-process:latest
```

说明：

- `process` 依赖 `auth` 与 `platform`
- 建议同时挂载外部 `application.yml` 到 `/app/application.yml`
- 如果需要 MQTT 订阅能力，请先确认 `application.yml` 中的 `mqtt.enabled` 和 Broker 配置

## 8. 平台服务

目录参考：

```bash
cd /data/epmis/platform
```

重新构建镜像：

```bash
docker build --no-cache -t epmis-platform:latest -f Dockerfile .
```

删除容器：

```bash
docker rm -f epmis-platform
```

删除镜像：

```bash
docker rmi epmis-platform:latest
```

启动容器：

```bash
docker run -d --name epmis-platform -p 9533:9533 -m 1g -v "$(pwd)/epmis-platform-service.jar:/app/app.jar" -v "$(pwd)/application.yml:/app/application.yml" -v "$(pwd)/logs:/app/logs" -v "$(pwd)/uploadPath:/app/uploadPath" -e "EPMIS_PROFILE=/app/uploadPath" -e "JAVA_OPTS=-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8" epmis-platform:latest
```

说明：

- `platform` 依赖 `auth`
- 也可能通过 Feign 调用 `protocol`
- 建议同时挂载外部 `application.yml` 到 `/app/application.yml`
- 存在文件上传与模板导入导出场景，建议保留 `uploadPath`

## 9. 协议服务

目录参考：

```bash
cd /data/epmis/protocol
```

重新构建镜像：

```bash
docker build --no-cache -t epmis-protocol:latest -f Dockerfile .
```

删除容器：

```bash
docker rm -f epmis-protocol
```

删除镜像：

```bash
docker rmi epmis-protocol:latest
```

启动容器：

```bash
docker run -d --name epmis-protocol -p 9534:9534 -m 1g -v "$(pwd)/epmis-protocol-service.jar:/app/app.jar" -v "$(pwd)/application.yml:/app/application.yml" -v "$(pwd)/logs:/app/logs" -e "JAVA_OPTS=-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=UTF-8" epmis-protocol:latest
```

说明：

- `protocol` 依赖 `platform`
- 建议同时挂载外部 `application.yml` 到 `/app/config/application.yml`
- 若启用 Netty、MQTT、Modbus，需要确认端口和网络放行策略

## 10. 推荐启动顺序

建议按以下顺序启动：

1. `epmis-auth`
2. `epmis-platform`
3. `epmis-process`
4. `epmis-protocol`

说明：

- `platform` 依赖 `auth`
- `process` 依赖 `auth` 与 `platform`
- `protocol` 依赖 `platform`

## 11. 常用检查命令

查看容器：

```bash
docker ps -a
```

查看日志：

```bash
docker logs -f epmis-auth
docker logs -f epmis-platform
docker logs -f epmis-process
docker logs -f epmis-protocol
```

进入容器：

```bash
docker exec -it epmis-process sh
```

检查挂载文件：

```bash
ls -l epmis-auth-service.jar
ls -l epmis-platform-service.jar
ls -l epmis-process-service.jar
ls -l epmis-protocol-service.jar
```

## 12. 注意事项

- 当前方案是“镜像不内置业务 jar，运行时挂载 jar”
- 如果宿主机 jar 被删除，镜像本身不受影响，但容器重启后会启动失败
- 不建议把整个宿主机目录直接挂载到 `/app`
- 若需要更稳妥的生产部署方式，可改回“jar 直接打入镜像”的发布模型
