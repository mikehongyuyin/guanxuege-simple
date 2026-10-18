# 观学阁（GuanXueGe）

> 面向学龄儿童家长的育儿管理平台 —— 学习计划、成绩追踪、家长社区三件事，用一个单体应用完整跑通。

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-6DB33F?logo=springboot&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Tests](https://img.shields.io/badge/Tests-153%20passing-4CAF50)
[![CI](https://github.com/mikehongyuyin/guanxuege-simple/actions/workflows/ci.yml/badge.svg)](https://github.com/mikehongyuyin/guanxuege-simple/actions/workflows/ci.yml)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 界面预览

**品牌落地页** —— 免登录，负责讲清产品价值并把访客带入注册流程
（图中的运营数据当前为占位值，见第十节「已知问题」）

![观学阁 · 品牌落地页](docs/screenshots/home.png)

**育儿社区** —— 分层圈子（幼小衔接 / 小学低年级 / 小学高年级 / 初中生 / 二胎家庭）、
话题榜、活跃家长，帖子底部「转为学习计划」即闭环入口
（图中帖子为演示数据，可用 `backend/sql/seed/` 下的脚本一键复现）

![观学阁 · 育儿社区](docs/screenshots/community.png)

**心声社区** —— 与育儿社区**共用同一套帖子 / 评论 / 点赞能力**，靠 `community` 维度切分场景；
圈子为 工作 / 副业 / 创业 / 生活，话题榜与活跃榜均按社区独立统计

![观学阁 · 心声社区](docs/screenshots/voice.png)

---

## 一、它解决什么问题

家长在育儿过程中会同时面对三件互相纠缠的事：

- **孩子的学** —— 定了计划却执行不下去，缺一个能被孩子自己看见的进度反馈
- **家长的记录** —— 成绩散落在纸质试卷和聊天记录里，看不出趋势
- **家长的情绪** —— 越刷育儿内容越焦虑，社区很容易变成"比成绩"的放大器

观学阁把这三件事放进同一个数据模型里：**孩子是数据边界**，计划、成绩挂在孩子下，社区内容又能一键转成孩子的执行计划，形成闭环。

---

## 二、八个值得说的技术决策

这部分是项目的重心。每个决策都写清了「遇到什么问题 → 怎么解的 → 为什么这么选」。

### 1. 鉴权 ≠ 授权：横切的归属校验

JWT 过滤器只能证明**请求者是谁**，证明不了**资源是不是请求者的**。早期版本所有接口只校验了「已登录」，结果任意登录用户传别人的 `childId` 就能读写数据。

解法是把归属校验抽成横切能力 `OwnershipService`，沿链路回溯：

```
task → plan → child → parent_id == 当前 userId
```

任一层不匹配即抛 `ForbiddenException` → HTTP **403**。这里刻意用 403 而非 400：越权是安全事件，需要独立的语义让监控和告警能抓到。

### 2. 幂等靠数据库约束，不靠应用层判断

打卡和点赞是最容易被重复提交的两处（用户连点、网络重试）。

- **打卡**：`uk_task_date(task_id, date)` 唯一键，同一任务同一天只能有一条记录
- **点赞**：`gx_post_like(post_id, user_id)` 唯一键，点赞变 **toggle** —— 已赞则取消并回退计数

应用层判断在并发下会失效，唯一约束才是幂等的最后一道防线。

### 3. 计数用原子自增，不做「读-改-写」

```java
// 早期：先 select，内存里 +1，再 update —— 并发下会丢更新
// 现在：交给数据库原子执行
baseMapper.update(null, Wrappers.<Post>lambdaUpdate()
        .setSql("like_count = like_count + 1, hot_score = hot_score + 1")
        .eq(Post::getId, id));
```

计数漂移是这类功能的典型坑，改成原子操作从根上避掉了。

### 4. 连续打卡：最容易被写错的一行

统计连续天数时，「今天还没打卡」不等于「连续已中断」——如果昨天及之前是连续的，今天只是还没打，此时连续天数应当保持不变。

```java
// 今天已打卡从今天起算；否则从昨天起算，避免把「今天未打」误判为断签
LocalDate cursor = dates.contains(today) ? today : today.minusDays(1);
```

这个边界有专门的测试用例锁死。

### 5. 反焦虑内容护栏：拦截不是唯一的答案

内容治理常见的做法是**命中即拦截**，但育儿场景里这会制造新的焦虑。这里做成两级：

| 级别 | 词库 | 行为 |
|---|---|---|
| 违禁词 | 16 个 | 直接拒绝发布 |
| 焦虑词 | 14 个 | **放行**，但标记 + 返回一段温和提示 |

「鸡娃」「别人家孩子」「成绩垫底」这类词，说的是家长的普遍处境，不该被当作违规。产品上更合理的方式是：允许表达，但给一点缓冲。

### 6. 儿童信息合规：同意是状态，不是一个勾选框

涉及未成年人信息，只做"用户勾选了同意"是不够的。

- 添加孩子是**强同意门禁**：未声明监护关系，后端直接拒绝
- 同意记录独立成表、带文档版本号，可审计
- 删除孩子时**自动撤回同意**，但保留审计轨迹

### 7. 第二个社区：加一个维度，而不是复制一套表

后来需要再做一个「心声社区」（工作 / 副业 / 创业 / 生活）。最省事的做法是复制一份帖子表 —— 那是技术债的开始：点赞幂等、词库护栏、@ 解析、分页排序都要写两遍，任何一处修复都要改两次。

实际做法是给 `gx_post` / `gx_circle` 加一个 `community` 维度：

```sql
ALTER TABLE gx_post ADD COLUMN community VARCHAR(16) NOT NULL DEFAULT 'parenting';
-- 类型收敛在 Java 常量里，非法值一律回落 parenting
```

收益是**能力只有一份**：新增社区等于新增一个枚举值 + 一组圈子，不需要动任何业务逻辑。
代价是每个查询都必须记得带上过滤条件——所以话题榜与活跃榜也一并加了 `community` 参数，否则心声社区会看到育儿话题。

前端同理：`Community.vue` 被抽成参数化的 `CommunityFeed.vue`，两个页面只是传不同的 `community` 与文案配置。

### 8. 隐私默认收紧：联系方式只有本人看得到

社区里点开别人的资料，需要展示昵称、签名、发帖数，但**不该展示手机号、微信号、邮箱**——公开社区的联系方式很容易被批量抓取。

做法是在 VO 层做裁剪，而不是靠前端不渲染（前端藏起来的数据照样在响应体里）：

```java
boolean self = viewerId != null && viewerId.equals(targetId);
if (self) {            // 仅本人
    vo.setPhone(...); vo.setWechat(...); vo.setEmail(...);
}
```

配套两个细节：

- **展示名优先级**：社区昵称 > 账号昵称。用户改了社区昵称，帖子、评论、活跃榜、@ 提及全部跟着变，但登录账号不受影响。
- **@ 提及存 user_id 不存昵称字符串**：昵称是可变的，存字符串会让历史提及在改名后全部失效。

---

## 三、架构

```
浏览器 / 移动端
       │
       ▼
┌─────────────────────────────────────────────────┐
│  Vue 3 (Vite)  ·  Pinia  ·  Element Plus       │
└─────────────────────────────────────────────────┘
       │  /api/**   （Nginx 同域反代，无跨域）
       ▼
┌─────────────────────────────────────────────────┐
│  Controller  ──  X-User-Id（过滤器注入）        │
├─────────────────────────────────────────────────┤
│  Service                                       │
│   ├ OwnershipService   归属校验（横切）          │
│   ├ AuthService / ChildService / FamilyService  │
│   ├ PlanService / PlanTaskService / Checkin    │
│   ├ GradeService                                │
│   └ PostService / CommentService / CircleService │
├─────────────────────────────────────────────────┤
│  Mapper (MyBatis-Plus)  ·  雪花 ID  ·  Caffeine │
└─────────────────────────────────────────────────┘
       │                        │
       ▼                        ▼
   MySQL 8.0            Caffeine 本地缓存
```

**为什么从微服务退回单体**：这个项目最初是 Spring Cloud 全家桶（Gateway / Nacos / OpenFeign / RocketMQ）。实际跑下来发现，单机部署下这些中间件带来的运维成本远大于收益——服务间调用退化成本地调用，消息队列只用来做一件同步能做完的事。

重构为单体后：部署从 6 个容器降到 3 个，故障排查路径大幅缩短。完整的取舍过程见 [ADR-001](docs/ADR-001-微服务重构为单体.md)。

---

## 四、技术栈

| 层 | 技术 | 说明 |
|---|---|---|
| 前端 | Vue 3 + Vite + Vue Router + Pinia | 组合式 API |
| UI | Element Plus | — |
| 后端 | Spring Boot 3.2.1（Java 17） | 单模块 |
| ORM | MyBatis-Plus 3.5.7 | 分页插件、逻辑删除 |
| 鉴权 | JWT (HS256) | 2 小时有效期，密钥走环境变量 |
| 缓存 | Caffeine | 本地缓存，10 分钟过期 |
| 数据库 | MySQL 8.0 | utf8mb4 |
| 测试 | JUnit 5 + Mockito + AssertJ | 76 个用例 |
| 部署 | Docker Compose | mysql + app + frontend |

---

## 五、快速开始

### Docker 一键启动（推荐）

```bash
# 1. 配置环境变量
cp .env.example .env

# 2. 生成 JWT 密钥并写入 .env 的 JWT_SECRET
openssl rand -base64 48

# 3. 启动
docker compose up -d --build
```

| 服务 | 地址 |
|---|---|
| 前端 | http://localhost:8088 |
| 后端 API | http://localhost:8080 |

首次启动会自动执行 `backend/sql/schema.sql` 建库建表。

### 本地开发

```bash
# 后端（:8080）
cd backend && mvn spring-boot:run

# 前端（:5173，Vite 已配置代理 /api → :8080）
cd frontend && npm install && npm run dev
```

---

## 六、数据模型

16 张表，按模块划分：

| 模块 | 表 |
|---|---|
| 账户 | `gx_user` `gx_child` `gx_family` `gx_consent_record` |
| 学习 | `gx_plan` `gx_plan_task` `gx_checkin` `gx_contract` |
| 成绩 | `gx_grade` |
| 社区 | `gx_circle` `gx_user_circle` `gx_post` `gx_post_like` `gx_comment` `gx_topic` `gx_mention` |

关键约束：

- `gx_checkin`：`uk_task_date(task_id, date)` —— 打卡幂等
- `gx_post_like`：`uk_post_user(post_id, user_id)` —— 点赞幂等
- `gx_mention`：`uk_biz_user(biz_type, biz_id, user_id)` —— 同一内容不重复记录同一人
- `gx_post.community` / `gx_circle.community`：社区维度，`parenting` 育儿 / `voice` 心声，默认 `parenting`
- `gx_user` 的 `community_nickname` / `wechat` / `email` / `signature`：社区资料，其中联系方式仅本人可见
- 业务表统一 `deleted` 逻辑删除，由 MyBatis-Plus 自动维护

---

## 七、API 概览

| 模块 | 主要接口 |
|---|---|
| 认证 | `POST /api/auth/register` `POST /api/auth/login` |
| 孩子 | `GET/POST /api/child` `PUT /api/child` `DELETE /api/child/{id}` |
| 家庭 | `GET/PUT /api/family` |
| 同意 | `POST /api/consent/grant` `POST /api/consent/revoke` |
| 计划 | `GET/POST /api/plan` `PUT /api/plan` `GET /api/plan/overview` |
| 任务 | `GET/POST /api/plan/task` `POST /api/plan/task/{id}/checkin` |
| 成绩 | `GET/POST /api/grade` `GET /api/grade/overview` |
| 社区 | `GET/POST /api/post` `POST /api/post/{id}/like` `POST /api/post/{id}/to-plan` |
| 圈子 | `GET /api/circle?community=` `POST /api/circle/{id}/join` |
| 用户资料 | `GET/PUT /api/user/profile` `GET /api/user/{id}` `GET /api/user/search` |
| 侧栏统计 | `GET /api/topic/hot?community=` `GET /api/community/active-parents?community=` |

统一返回 `R { code, message, data }`，`code = 0` 为成功。

两个社区靠 `community` 查询参数切分：`parenting`（育儿）/ `voice`（心声）。
不传时圈子返回全部、帖子与统计回落 `parenting`。

---

## 八、测试

```bash
cd backend && mvn test
```

153 个单元测试，聚焦核心业务不变量：

| 测试类 | 数量 | 覆盖内容 |
|---|---|---|
| `PostServiceImplTest` | 17 | 点赞 toggle 幂等、liked 回填、内容安全 |
| `CheckinServiceImplTest` | 11 | 连续天数、今日未打卡边界、断签重开 |
| `OwnershipServiceTest` | 15 | 四层归属校验链路、403 / 400 语义 |
| `WordFilterUtilTest` | 23 | 违禁词拦截、焦虑词放行与标记 |
| `ResourceUpdateGuardTest` | 10 | 更新接口防越权改挂 |
| `UserServiceImplTest` | 23 | **联系方式仅本人可见**、展示名优先级、资料校验 |
| `MentionParserTest` | 16 | @ 昵称提取、中英文/全角标点截断 |
| `MentionServiceImplTest` | 17 | 容错解析、先清后插、批量抗 N+1 |
| `PostCommunityIsolationTest` | 13 | 两个社区的读写隔离与 SQL 条件 |
| `CircleServiceImplTest` | 8 | 圈子按社区过滤、joined 回填 |

不连真实数据库，全部基于 Mockito 隔离。

写「断言 SQL 条件」这类测试时有个坑：MyBatis-Plus 的 LambdaWrapper 是**懒解析**的，`eq(Post::getCommunity, ...)` 只暂存方法引用，要等生成 SQL 才翻译列名，而翻译依赖 `TableInfo` 缓存——Spring 启动时自动建好，纯 Mockito 单测里是空的，直接调 `getCustomSqlSegment()` 会抛 `can not find lambda cache for this entity`。`MybatisPlusSqlFixture` 就是补这一步，让测试真的能断言 WHERE 条件，而不是只验证「方法被调用了」。

---

## 九、目录结构

```
guanxuege-simple/
├─ backend/
│  ├─ src/main/java/com/guanxuege/
│  │   ├─ common/        # R、全局异常、JWT、缓存、过滤器、词库、归属校验
│  │   ├─ auth/          # 用户 / 孩子 / 家庭 / 同意记录 / 资料与隐私
│  │   ├─ learningplan/  # 计划 / 任务 / 打卡 / 契约
│  │   ├─ grade/         # 成绩
│  │   └─ community/     # 帖子 / 评论 / 点赞 / 圈子 / 话题 / @提及
│  ├─ sql/
│  │   ├─ schema.sql               # 建库建表
│  │   ├─ migration/               # 增量迁移脚本（按日期命名）
│  │   └─ seed/                    # 演示数据（可复现截图中的社区内容）
│  └─ Dockerfile / Dockerfile.local
├─ frontend/
│  ├─ src/
│  │   ├─ components/    # CommunityFeed（参数化社区） / UserProfileDialog / MentionText
│  │   └─ views/         # Community（育儿） / VoiceCommunity（心声） 共用 CommunityFeed
│  └─ Dockerfile / Dockerfile.local
├─ docs/
│  ├─ PRODUCT_DOC.md     # 产品文档（现状说明书）
│  ├─ ARCHITECTURE.md    # 架构设计说明
│  ├─ ADR-001-微服务重构为单体.md
│  ├─ architecture.svg   # 分层架构图
│  └─ screenshots/       # 界面截图（home / community / voice）
├─ docker-compose.yml
└─ docker-compose.local.yml  # 本机快速更新（复用已编译产物）
```

---

## 十、已知问题与后续规划

项目目前仍有明确的改进空间，列在这里以便评估：

**待修复**

- 点赞计数在高并发下依赖事务隔离，极端场景可考虑乐观锁版本字段
- 帖子与评论的内容安全口径不完全一致（帖子校验标题+正文，评论仅校验正文）
- 计划进度百分比使用整数截断，无四舍五入
- `MentionParser` 不排除英文句号，`a@b.com` 会被解析出域名片段（昵称需精确匹配，不会产生真实提及）
- `/api/circle/list` 不传 `community` 时返回全部圈子，而帖子列表不传时回落 `parenting`——口径不统一，前端必须显式传参

**规划中**

- 接入 Spring Boot Actuator + Prometheus，补齐可观测性
- 首页运营数据改为真实统计，替换占位数据
- 敏感词库外置为配置或数据表，支持运营后台维护
- @提及通知（当前只记录关系，未推送提醒）
- 补充集成测试与 API 文档（OpenAPI）

---

## 十一、License

[MIT](LICENSE) © 2026

---

## 附：本地开发说明

- **构建加速**：`docker-compose.local.yml` 直接复用本机已编译的 jar 与 dist，跳过容器内联网拉依赖，
  适合频繁改代码后快速验证：
  ```bash
  docker compose -f docker-compose.yml -f docker-compose.local.yml up -d --build
  ```
- **数据库变更**：`schema.sql` 仅在数据卷为空时自动执行。已运行的库需手动执行
  `backend/sql/migration/` 下的增量脚本。
- **数据保护**：容器可随意重建，数据持久化在命名卷 `mysql-data`。
  **切勿执行 `docker compose down -v`**，该命令会删除数据卷。
