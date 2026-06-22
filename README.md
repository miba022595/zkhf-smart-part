# smart-part 项目文档入口

## 1. 项目简介

smart-part（园区环境智慧化管控平台）是一个基于 Java 21、Spring Boot 3、Spring Cloud、MyBatis 的多模块后端项目，围绕认证授权、平台主数据、业务处理、协议接入等能力提供服务。

当前仓库以 Maven 聚合工程形式组织，包含 4 个可运行服务：

- `epmis-auth-service`：认证与系统管理服务，默认端口 `9531`
- `epmis-process-service`：数据处理与业务计算服务，默认端口 `9532`
- `epmis-platform-service`：平台主业务服务，默认端口 `9533`
- `epmis-protocol-service`：协议接入与设备连接服务，默认端口 `9534`

## 2. 仓库结构

```text
zkhf-epmis-ai-en
├─ pom.xml                         # 聚合工程
├─ epmis-parent                    # 统一依赖管理
├─ epmis-core                      # 公共工具、通用领域对象
├─ epmis-auth                      # 认证与系统管理
├─ epmis-platform                  # 平台业务模块
├─ epmis-process                   # 数据处理与统计分析
├─ epmis-protocol                  # 协议层与设备连接
├─ epmis-generator                 # 代码生成工具
├─ api                             # API 文档目录
└─ docs                            # 项目说明与操作手册
```

## 3. 文档导航

- 项目说明：`docs/项目说明.md`
- 部署与操作手册：`docs/部署与操作手册.md`
- API 总览：`api/API总览.md`
- 应急模块详细 API：`api/环境应急管理模块-API文档.md`

## 4. 技术栈

- JDK：`21`
- Maven：`3.9+`
- Spring Boot：`3.2.5`
- Spring Cloud：`2023.0.1`
- MyBatis：`3.0.4`
- MySQL：`8.x`
- Redis：`6.x/7.x`
- 其他组件：`JWT`、`PageHelper`、`Hutool`、`Apache POI`

## 5. 快速启动

### 5.1 环境准备

- 安装 `JDK 21`
- 安装 `Maven 3.9+`
- 准备 `MySQL` 与 `Redis`
- 按环境修改各服务的 `application.yml`

### 5.2 编译项目

在仓库根目录执行：

```bash
mvn clean install -DskipTests
```

### 5.3 启动服务

建议按以下顺序启动：

1. `epmis-auth-service`
2. `epmis-platform-service`
3. `epmis-process-service`
4. `epmis-protocol-service`

示例：

```bash
mvn -pl epmis-auth/epmis-auth-service -am spring-boot:run
mvn -pl epmis-platform/epmis-platform-service -am spring-boot:run
mvn -pl epmis-process/epmis-process-service -am spring-boot:run
mvn -pl epmis-protocol/epmis-protocol-service -am spring-boot:run
```

## 6. 联调入口

- 登录接口：`POST /login`
- 登录成功后，在请求头中携带 `Authorization: Bearer <token>`
- 平台服务主要接口前缀：`/platform/**`
- 数据处理服务主要接口前缀：`/process/**`
- 协议服务主要接口前缀：`/protocol/**`

## 7. 注意事项

- 仓库中的 `application.yml` 为环境化配置，请根据部署环境替换数据库、Redis、MQTT、企业微信等参数。
- 项目大量接口支持 Excel 模板导入导出，模板文件位于各服务 `src/main/resources/template/` 目录。
- 当前仓库未集成 Swagger/Knife4j，接口文档以 Markdown 方式维护。
