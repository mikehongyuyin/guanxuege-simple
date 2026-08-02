# 观学阁（GuanXueGe）

面向「学龄儿童家长」的一站式育儿管理平台：管理孩子的**学习计划**、记录**学习成绩**，并在**育儿社区**与万千家长交流经验。

本项目包含：

- **PRD 文档**：`docs/PRD.md`（产品定位、功能需求、信息架构、核心流程、数据模型、API、非功能需求、技术架构与里程碑）
- **后端工程**：Spring Cloud 微服务（`backend/`）
- **前端工程**：Vue 3 单页应用（`frontend/`）
- **设计稿**：Ardot 文件「观学阁」四屏（首页 / 学习计划 / 成绩管理 / 育儿社区）

---

## 一、技术架构

```
                        ┌─────────────────────────────┐
   浏览器 / 移动端 ─────►│  Vue 3 (Vite + Element Plus) │
                        └──────────────┬──────────────┘
                                       │  /api/**
                                       ▼
                        ┌─────────────────────────────┐
                        │   Spring Cloud Gateway :8080 │  JWT 鉴权 + 路由 + 跨域
                        │   （统一网关，注入 X-User-Id） │
                        └──────────────┬──────────────┘
                                       │  lb:// 服务名
              ┌──────────────┬─────────┴─────────┬──────────────┐
              ▼              ▼                    ▼              ▼
        guanxuege-auth  learning-plan         grade        community
        (登录/JWT/孩子)  (计划/任务)         (成绩统计)    (帖子/评论/话题)
              ▲              ▲                    ▲              ▲
              │  OpenFeign   │                    │              │ RocketMQ
              └──────────────┴────────────────────┴──────────────┘
                                       │
                  Nacos(注册/发现)  MySQL  Redis  RocketMQ
```

### 技术选型
| 层 | 技术 |
|---|---|
| 前端 | Vue 3 + Vite + Vue Router + Pinia + Axios + Element Plus |
| 网关 | Spring Cloud Gateway（全局 JWT 过滤器、`X-User-Id` 透传） |
| 注册中心 | Nacos（服务注册与发现） |
| 微服务 | Spring Boot 3.2 + Spring Cloud 2023.0（Spring Cloud Alibaba 2023.0.1） |
| 服务调用 | OpenFeign（community → auth 取作者信息） |
| 持久层 | MyBatis-Plus + MySQL 8（utf8mb4） |
| 缓存 | Redis |
| 消息队列 | RocketMQ（发帖事件异步更新热度 / 话题讨论数） |
| 鉴权 | JWT（HS256，密钥前后端一致） |
| 构建 | Maven（Java 17），父工程管理统一依赖版本 |

### 模块
- `guanxuege-common`：统一返回 `R`、全局异常、JWT 工具、Redis 配置、自动填充、OpenFeign 客户端 `AuthClient`、RocketMQ 常量
- `guanxuege-gateway`：网关 + 鉴权过滤器
- `guanxuege-auth`：`gx_user` / `gx_child`，登录签发 JWT，对外 Feign 接口
- `guanxuege-learning-plan`：`gx_plan` / `gx_plan_task`
- `guanxuege-grade`：`gx_grade`
- `guanxuege-community`：`gx_post` / `gx_comment` / `gx_topic`，RocketMQ 生产者 + 消费者

---

## 二、运行（Docker 一键启动）

本仓库提供 **一键脚本**（无需手敲命令）：

| 系统 | 启动 | 停止 |
|---|---|---|
| Windows | 双击 `start.bat`（或 cmd 中运行） | 双击 `stop.bat` |
| Linux / macOS / Git Bash | `./start.sh` | `./stop.sh` |

脚本会弹出菜单让你选 **[1] 仅中间件** 或 **[2] 全栈启动**（默认全栈）。以下为等价的手动命令，便于理解和排错。

本仓库提供 `docker-compose.yml`，用 **profile** 区分两种启动方式：

### 方式 A：仅启动中间件（默认，向后兼容）
```bash
cd guanxuege
docker compose up -d      # 启动 MySQL / Redis / RocketMQ / Nacos
```
随后后端用 `mvn spring-boot:run`、前端用 `npm run dev` 本地跑（见第三、四节）。

### 方式 B：全栈一键（中间件 + 5 个后端服务 + 前端）
```bash
cd guanxuege
docker compose --profile app up -d --build
```
- 首次会**构建镜像**：后端用 Maven 多阶段打包（需联网拉取依赖，较慢），前端用 Node 构建后由 Nginx 托管。
- 启动后访问 **http://localhost:8088** 即可打开前端（Nginx 已把 `/api` 反向代理到网关 `:8080`）。
- 各服务端口：gateway 8080 / auth 8801 / learning-plan 8802 / grade 8803 / community 8804（容器内），前端 8088（宿主机）。

### 前置与说明
- 需安装 **Docker（含 Compose v2，`docker compose` 命令）**，且构建时能访问公网（拉取 Maven / npm 依赖）。
- 容器间通过服务名互访：`mysql` / `redis` / `rocketmq-namesrv` / `nacos` / `gateway`。
- 业务服务连接地址已抽成环境变量（`MYSQL_HOST` / `REDIS_HOST` / `ROCKETMQ_ADDR` / `NACOS_ADDR`），默认值 `127.0.0.1`，因此**本地直接运行也完全兼容**，无需改动。
- 数据库已通过 `docker-entrypoint-initdb.d` 自动执行 `backend/sql/schema.sql` 建库建表，无需手动导入。
- 常用命令：
  ```bash
  docker compose logs -f <服务名>   # 看日志，如 gx-community
  docker compose down               # 停止并移除容器
  docker compose --profile app down # 停止全栈
  ```

> 注：RocketMQ 的 `broker.conf`（见 `rocketmq/broker.conf`）已把 `brokerIP1` 设为容器服务名 `rocketmq-broker`，容器间无需额外配置；若后端跑在宿主机（非容器），请把该值改为宿主机 IP。

---

## 三、启动后端
每个服务都是独立 Spring Boot 应用，依次启动（顺序：common 不需启动 → gateway → auth → 其余）：

```bash
cd backend
mvn -q -pl guanxuege-gateway,guanxuege-auth,guanxuege-learning-plan,guanxuege-grade,guanxuege-community -am install -DskipTests
# 或分别进入各模块执行 mvn spring-boot:run
```

各服务端口：gateway 8080 / auth 8801 / learning-plan 8802 / grade 8803 / community 8804。

网关路由（`/api/**` → 各微服务）：

| 前缀 | 服务 |
|---|---|
| `/api/auth/**`、`/api/child/**` | guanxuege-auth |
| `/api/plan/**` | guanxuege-learning-plan |
| `/api/grade/**` | guanxuege-grade |
| `/api/post/**`、`/api/comment/**`、`/api/topic/**` | guanxuege-community |

白名单：`/api/auth/login`、`/api/auth/register`、OPTIONS 预检直接放行；其余请求需在 Header 带 `Authorization: Bearer <token>`，网关校验后注入 `X-User-Id` 给下游。

---

## 四、启动前端
```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
```

### 本地 Mock（默认，无需后端）
`src/config.js` 中 `USE_MOCK = true` 时，前端直接读取 `src/api/mock.js`，界面开箱即用，方便先看效果。

### 对接真实后端
将 `src/config.js` 的 `USE_MOCK` 改为 `false`，前端通过 Vite 代理（`/api` → `http://localhost:8080`）走网关访问微服务；登录后即可看到真实数据。

---

## 五、关键约定
- **JWT 密钥**：`guanxuege-secret-key-2026-very-long-string-for-hs256`，定义在 `common/util/JwtUtil` 与 `gateway/config/JwtUtil`，务必保持一致；生产请改为环境变量注入。
- **统一返回**：所有接口返回 `R{ code, message, data }`，`code=0` 成功；网关 401 由全局异常处理返回。
- **逻辑删除**：所有业务表 `deleted` 字段由 MyBatis-Plus 自动维护（插入默认 0）。
- **RocketMQ**：发帖事件主题 `gxg-community-topic`，Tag `POST_CREATED`，消费组 `gxg-post-consumer`（见 `common/constant/MqConstant`）。
- **服务间调用**：community 通过 `AuthClient`（`@FeignClient("guanxuege-auth")`）按 id 获取作者脱敏信息，并对鉴权服务不可达做了降级（仅缺作者信息）。

---

## 六、目录速览
```
guanxuege/
├─ docs/PRD.md
├─ backend/                # Maven 多模块
│  ├─ pom.xml
│  ├─ guanxuege-common/  guanxuege-gateway/  guanxuege-auth/
│  ├─ guanxuege-learning-plan/  guanxuege-grade/  guanxuege-community/
│  └─ sql/schema.sql
├─ frontend/              # Vue 3
│  ├─ src/{api,store,views,layout,styles,router,config}
│  └─ package.json  vite.config.js
└─ docker-compose.yml
```

> 设计稿四屏（首页 / 学习计划 / 成绩管理 / 育儿社区）已在 Ardot 中完成，前端页面据此还原了古风温暖视觉与核心交互。
