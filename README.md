# 观学阁（GuanXueGe）— 单体版

面向「学龄儿童家长」的一站式育儿管理平台：管理孩子的**学习计划**、记录**学习成绩**，并在**育儿社区**与万千家长交流经验。

> 本目录是 `guanxuege` 的**单体精简版**：后端由 Spring Cloud 微服务（Gateway / Nacos / OpenFeign / RocketMQ）重构为**单个 Spring Boot 应用**，仅保留 **Spring Boot + MyBatis-Plus + Redis + MySQL**。功能与原版完全一致，部署与维护更轻量。

---

## 一、技术架构（单体）

```
浏览器 / 移动端 ──► Vue 3 (Vite) ── /api/** ──► 单体 Spring Boot :8080
                                              │
                          ┌───────────────────┼───────────────────┐
                          ▼                   ▼                   ▼
                     MySQL 8             Redis            (不再有 MQ/Nacos/网关)
```

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 + Vite + Vue Router + Pinia + Axios + Element Plus |
| 后端 | 单体 Spring Boot 3.2 + MyBatis-Plus（含分页插件） |
| 鉴权 | JWT（HS256，密钥前后端一致）；单体应用内由 `JwtAuthenticationFilter` 完成校验并注入 `X-User-Id` |
| 持久层 | MyBatis-Plus + MySQL 8（utf8mb4，逻辑删除） |
| 缓存 | Redis |
| 构建 | Maven（Java 17），单模块 |

### 与原分布式版的区别
- ❌ 已移除：Spring Cloud Gateway、Nacos 注册中心、OpenFeign 服务调用、RocketMQ 消息队列。
- ✅ 发帖事件原本异步投递 RocketMQ，现**同步完成**（落库即更新热度/话题，无需消息中间件）。
- ✅ 社区取作者信息原本走 Feign 调 auth，现**同进程直接调用** `UserService`。
- ✅ 鉴权原本由网关做，现由单体内的 `JwtAuthenticationFilter` + CORS 配置承担，Controller 仍从 `X-User-Id` 头取当前用户，契约不变。

---

## 二、目录结构

```
guanxuege-simple/
├─ docs/PRD.md                      # 产品需求文档（同原版）
├─ backend/                         # 单体 Spring Boot
│  ├─ pom.xml                       # 单模块 Maven
│  ├─ src/main/java/com/guanxuege/
│  │   ├─ SimpleApplication.java    # 启动类
│  │   ├─ common/                   # R / 异常 / JWT / Redis / 过滤器 / CORS
│  │   ├─ auth/                     # 用户、孩子、登录
│  │   ├─ learningplan/             # 学习计划 / 任务
│  │   ├─ grade/                    # 成绩
│  │   └─ community/                # 帖子 / 评论 / 话题
│  ├─ src/main/resources/application.yml
│  ├─ sql/schema.sql               # 7 张表 DDL（自动建库建表）
│  └─ Dockerfile
├─ frontend/                        # Vue 3（同原版）
├─ docker-compose.yml               # mysql + redis + app + frontend
├─ start.bat / start.sh            # 一键启动
└─ stop.bat / stop.sh
```

> 注：`backend/_legacy_modules/`、`backend/_trash/` 为重构过程中的归档/废弃文件，不参与构建，可删除。

---

## 三、本地运行（不依赖 Docker）

### 1. 中间件
自行安装并启动 MySQL 8（库名 `guanxuege`，utf8mb4）与 Redis；或：
```bash
docker compose up -d mysql redis
```
导入表结构（若未用自动初始化）：
```bash
mysql -uroot -proot < backend/sql/schema.sql
```

### 2. 启动后端
```bash
cd backend
mvn spring-boot:run        # 或 mvn clean package && java -jar target/guanxuege-simple.jar
```
默认端口 **8080**。配置见 `application.yml`，可用环境变量覆盖：`MYSQL_HOST` / `MYSQL_USER` / `MYSQL_PASSWORD` / `REDIS_HOST`。

### 3. 启动前端
```bash
cd frontend
npm install
npm run dev                 # http://localhost:5173
```
- `src/config.js` 的 `USE_MOCK = true` 时走本地 mock（开箱即用）；改为 `false` 后通过 Vite 代理 `/api` → `http://localhost:8080` 对接真实后端。

---

## 四、Docker 一键启动

```bash
# 首次（构建镜像，需联网拉 Maven/npm 依赖）
docker compose up -d --build
# 之后
docker compose up -d
```
访问：前端 **http://localhost:8088**（Nginx 已把 `/api` 反代到后端 `:8080`）、后端 API **http://localhost:8080**。
数据库经 `docker-entrypoint-initdb.d` 自动执行 `schema.sql`，无需手动导入。

---

## 五、关键约定
- **JWT 密钥**：`guanxuege-secret-key-2026-very-long-string-for-hs256`（见 `common/util/JwtUtil`），生产请改为环境变量。
- **统一返回**：`R{ code, message, data }`，`code=0` 成功。过滤器对未登录/过期返回 `{"code":401,...}`。
- **逻辑删除**：业务表 `deleted` 由 MyBatis-Plus 自动维护。
- **CORS**：`common/config/WebConfig` 允许 `localhost:5173` / `localhost:8088` 带凭据访问。
