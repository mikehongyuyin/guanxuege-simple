# 观学阁 · 产品需求文档（PRD）

> 版本：v1.0 ｜ 状态：评审稿 ｜ 日期：2026-08-01
> 关联设计稿：Ardot 文件 `观学阁`（首页 / 学习计划管理 / 成绩管理 / 育儿社区，四屏，古风温暖风格）
> 关联工程：`guanxuege/backend`（Spring Cloud 微服务）、`guanxuege/frontend`（Vue 3）

---

## 1. 产品概述

### 1.1 定位
**观学阁** 是一款面向「家中有学龄儿童家长」的一站式育儿与学业管理社区产品。它将三类场景收敛到一个产品里：
- **学业管理**：帮家长规划孩子的学习计划、追踪完成情况、记录与分析考试成绩；
- **经验社区**：让家长围绕育儿与学业管理发帖、互动、讨论，沉淀可复用的育儿经验；
- **品牌信任**：通过古风温暖的设计语言与专业内容，建立「用心陪伴每一个家庭」的品牌心智。

### 1.2 目标用户
| 用户 | 画像 | 核心诉求 |
|---|---|---|
| 主要用户：家长 | 25–45 岁，孩子 6–15 岁（小学到初中） | 省心管理孩子学习、获取靠谱育儿经验、缓解焦虑 |
| 次要用户：孩子（被管理者） | 学龄儿童 | 在家长引导下使用计划/成绩模块（本期以家长视角为主） |

### 1.3 核心价值主张
> 一个 App，把「规划—执行—记录—交流」串成闭环：计划管执行，成绩看结果，社区补经验。

---

## 2. 设计语言与视觉规范（古风温暖）

本产品视觉以「宣纸 + 水墨 + 朱印」为基调，温暖而不轻佻、专业而不冰冷。

| 角色 | 色值 | 用途 |
|---|---|---|
| 宣纸底 paper | `#F6F1E7` | 页面/卡片外背景 |
| 暖白 surface | `#FCF9F1` | 卡片、面板底色 |
| 墨 ink | `#2B2620` | 主文字、标题 |
| 次级文字 muted | `#6B6256` | 辅助说明、元信息 |
| 分隔线 divider | `#E5DCCB` | 边框、分割线 |
| 朱红 vermillion | `#B5392E` | 主强调、主按钮、品牌印 |
| 青绿 celadon | `#205C49` | 次强调、正向状态 |
| 赭石 ochre | `#8A6440` | 三级强调、等级/徽章 |

- **字体**：标题 `Noto Serif SC`（衬线，古风）；正文 `Source Han Sans CN`（黑体，易读）。
- **圆角**：卡片 12px，按钮 8px，头像 50%（圆形）。
- **栅格**：后台类页面采用「240 侧边栏 + 自适应主区」；落地页采用居中单栏 / 分栏。

---

## 3. 功能需求总览

```
观学阁
├─ 首页 / 品牌落地页（未登录可见，转化入口）
├─ 账号中心（家长注册/登录、家长—孩子关系绑定）【支撑模块】
├─ 学习计划管理（M2）
├─ 成绩管理（M3）
└─ 育儿社区（M4）
```

| 模块 | 优先级 | 一句话描述 |
|---|---|---|
| 首页 | P0 | 品牌叙事 + 功能预览 + 免费开始 CTA |
| 账号/关系 | P0 | 家长账号、JWT 登录、一个家长绑定多个孩子 |
| 学习计划 M2 | P0 | 计划（待开始/进行中/已完成）× 任务（科目标签 + 进度条） |
| 成绩管理 M3 | P0 | 成绩录入、科目卡片、趋势图、班级/年级对比 |
| 育儿社区 M4 | P1 | 帖子流、话题榜、活跃家长、发帖互动（赞/评/回） |

---

## 4. 信息架构（站点地图）

### 4.1 落地页（首页）
- 顶部导航：Logo（朱印）/ 功能 / 价格 / 关于 / 「免费开始」按钮
- Hero：主张 + 双 CTA（免费开始 / 观看演示）+ 信任背书
- 功能特性：3 张卡片（学习计划 / 成绩管理 / 育儿社区）
- 使用流程：3 步（一二三）
- 社会证明：数据指标 + 家长证言
- 结尾 CTA Banner（朱红）
- 页脚：品牌 + 三列链接

### 4.2 后台（登录后）
- 左侧导航：首页 / 学习计划 / 成绩管理 / 育儿社区 / 设置
- 顶栏：页面标题 + 当前孩子切换 + 搜索/发帖入口
- 主区随导航切换（对应 M2/M3/M4）

---

## 5. 模块详细需求

### M0 账号与家长—孩子关系（支撑模块）
- **注册/登录**：手机号 + 验证码（或密码）；登录成功后签发 JWT。
- **家长档案**：昵称、城市、头像。
- **孩子档案**：姓名、年级、学校（可选）；一个家长可绑定多个孩子。
- **当前孩子上下文**：后台顶栏可切换「当前孩子」，学习计划/成绩均挂在孩子维度。

验收标准：未登录访问后台接口返回 401；JWT 过期或非法被网关拦截。

### M1 首页 / 品牌落地页
- 作为营销与转化页，功能同 4.1；不依赖登录。
- 「免费开始」跳转注册/登录。
- 文案与设计稿保持一致（中文）。

### M2 学习计划管理
- **计划 Plan**：标题、关联孩子、周期（起止日期）、状态（待开始/进行中/已完成，由时间或手动流转）、备注。
- **任务 Task**：归属计划、科目标签（语文/数学/英语/科学…）、内容、截止日、完成状态、进度（0–100%）。
- **看板视图**：三列（待开始 / 进行中 / 已完成），卡片显示科目标签 + 进度条。
- **概览卡片**：进行中计划数、本周待完成任务、整体完成率、连续打卡天数。
- **操作**：新建计划/任务、编辑、标记完成、删除（软删）。

验收：新建计划后看板即时出现；任务进度修改联动计划完成率。

### M3 成绩管理
- **成绩记录 Grade**：关联孩子、科目、考试类型（单元测/期中/期末）、分数、满分、班级排名、年级排名、录入时间。
- **科目卡片**：语文/数学/英语/科学… 显示最近一次分数与目标线对比。
- **趋势图**：按考试批次展示单科/总分折线或柱状趋势（前端图表）。
- **统计**：平均分、最高分、进步幅度；与班级/年级均值对比。
- **概览卡片**：总分、平均分、最佳科目、待关注科目。

验收：录入一条成绩后科目卡片与趋势图更新；排名缺省为空不报错。

### M4 育儿社区
- **帖子 Post**：作者（家长）、标题、正文、标签（#习惯养成/#成绩交流/#双减政策…）、点赞数、回复数、热度值、状态。
- **评论/回复 Comment**：归属帖子、作者、内容、层级（楼中楼）。
- **话题 Topic / 热议榜**：聚合标签或固定话题，按讨论数排序。
- **信息流**：推荐 / 最新 / 热议 三个 Tab；分页。
- **右侧栏**：发帖入口、活跃家长榜、热议话题榜。
- **互动**：发帖、点赞（幂等）、评论；发帖后通过 MQ 异步刷新热度与通知。

验收：发帖后信息流可见；点赞重复点击不重复计数；热议榜按讨论数排序。

---

## 6. 核心业务流程

### 6.1 注册登录
```
家长提交手机号+验证码 → auth-service 校验/创建用户 → 签发 JWT
→ 前端存 token → 后续请求带 Authorization → 网关校验 JWT → 路由到业务服务
```

### 6.2 制定并执行学习计划
```
新建计划(learning-plan) → 新增任务(含科目/截止日) → 看板展示
→ 家长更新任务进度 → 计划完成率重算 → 连续打卡统计(redis)
```

### 6.3 录入与分析成绩
```
录入成绩(grade) → 科目卡片/趋势数据更新 → 前端渲染图表
→ 可选：成绩异常(大幅下降) 发 MQ 事件 → 社区/通知 异步处理
```

### 6.4 社区发帖与互动
```
发帖(community) → 落库 → 发 MQ「POST_CREATED」事件
→ consumer 刷新话题热度(redis) + 触发@通知
→ 信息流/热议榜读 redis 缓存
点赞 → redis 记录 userId+postId 幂等 → 计数 +1
```

---

## 7. 数据模型（关键实体）

> 完整 DDL 见 `backend/sql/schema.sql`。以下为实体与关系摘要。

- **user（家长）**：id, phone, password(加密), nickname, avatar, city, status, create_time
- **child（孩子）**：id, parent_id, name, grade, school, create_time
- **learning_plan（计划）**：id, parent_id, child_id, title, start_date, end_date, status, progress, create_time
- **plan_task（任务）**：id, plan_id, subject, title, content, due_date, status, progress, create_time
- **grade（成绩）**：id, parent_id, child_id, subject, exam_type, score, full_score, class_rank, grade_rank, exam_date, create_time
- **post（帖子）**：id, author_id, title, content, tag, like_count, reply_count, hot_score, status, create_time
- **comment（评论）**：id, post_id, parent_comment_id, author_id, content, create_time
- **topic（话题）**：id, name, post_count, hot_score

关系：user 1—N child；user 1—N learning_plan；child 1—N learning_plan；learning_plan 1—N plan_task；
user 1—N post；post 1—N comment；post N—N topic（经 post_tag 或聚合）。

---

## 8. API 设计（摘要）

> 统一前缀 `/api`；网关层做路由与鉴权。以下为代表性端点，完整契约由前端 `src/api/*` 与后端 Controller 对齐。

### 8.1 网关与鉴权
- `POST /api/auth/login` 登录（放行）
- `POST /api/auth/register` 注册（放行）
- 其余所有 `/api/**` 需 `Authorization: Bearer <jwt>`，由 Gateway 全局过滤器校验。

### 8.2 学习计划
- `GET /api/plan/list?childId=&status=` 计划列表（看板）
- `POST /api/plan` 新建计划
- `POST /api/plan/task` 新建任务
- `PUT /api/plan/task/{id}/progress` 更新任务进度

### 8.3 成绩
- `GET /api/grade/list?childId=` 成绩列表
- `POST /api/grade` 录入成绩
- `GET /api/grade/stat?childId=` 统计/趋势

### 8.4 社区
- `GET /api/community/posts?tab=recommend|latest|hot&page=` 信息流
- `POST /api/community/post` 发帖（发 MQ 事件）
- `POST /api/community/post/{id}/like` 点赞（幂等）
- `GET /api/community/topics/hot` 热议话题榜
- `GET /api/community/active-parents` 活跃家长榜

---

## 9. 技术架构

### 9.1 微服务划分
| 服务 | 职责 | 关键中间件 |
|---|---|---|
| `guanxuege-gateway` | 统一入口、路由、JWT 鉴权、限流 | Spring Cloud Gateway |
| `guanxuege-auth` | 用户/孩子/登录、JWT 签发 | Redis（验证码/ token 黑名单） |
| `guanxuege-learning-plan` | 计划/任务/进度 | MySQL, Redis（打卡统计） |
| `guanxuege-grade` | 成绩/统计 | MySQL |
| `guanxuege-community` | 帖子/评论/话题/热度 | MySQL, Redis（热度/幂等）, RocketMQ |
| `guanxuege-common` | 公共包：R/异常/Redis/RocketMQ/JWT/BaseEntity | — |

### 9.2 关键中间件
- **Nacos**：服务注册与发现 + 配置中心（每个服务 `bootstrap.yml` 指定 `spring.cloud.nacos.discovery.server-addr` 与 `config`）。
- **Gateway**：`RouteLocator` 将 `/api/auth/**`→auth、`/api/plan/**`→learning-plan、`/api/grade/**`→grade、`/api/community/**`→community；全局 `AuthFilter` 校验 JWT（白名单放行登录/注册）。
- **Redis**：验证码缓存、JWT 黑名单、计划打卡统计、社区点赞幂等、话题热度。
- **RocketMQ**：社区发帖事件 `POST_CREATED`（异步刷新热度/通知）；成绩异常事件 `GRADE_ALERT`（可扩展）。
- **OpenFeign**：服务间调用，例如 `community` 通过 `AuthClient` 调 `auth` 校验作者是否存在/获取昵称。
- **MyBatis-Plus + MySQL**：ORM 与持久化，`@Mapper` + `IService` 通用 CRUD；表字符集 `utf8mb4`。

### 9.3 部署拓扑（分布式）
```
Client → Gateway(:8080) → {auth:8801, learning-plan:8802, grade:8803, community:8804}
                               ↑                ↑
                            Nacos(8848)     MySQL(3306) / Redis(6379) / RocketMQ(9876)
```
网关对外暴露，业务服务仅在内网经 Nacos 互发现；配置统一托管 Nacos。

---

## 10. 非功能性需求
- **性能**：列表接口 P95 < 200ms（热数据走 Redis）；信息流分页 20 条/页。
- **安全**：密码 bcrypt 加密；JWT 有效期 2h，.refresh 可选；网关统一鉴权；SQL 参数化（MyBatis-Plus 防注入）。
- **可用性**：核心服务无状态，可水平扩容；MQ 削峰，发帖不阻塞主流程。
- **可观测**：预留 actuator + 日志；建议接入 Prometheus（超出本期）。
- **兼容性**：前端适配 Chrome/Edge/Safari 最新版；移动端响应式降级（本期以桌面端为主）。

---

## 11. 里程碑
| 阶段 | 交付 |
|---|---|
| M1 设计稿 | Ardot 四屏（已完成） |
| M2 PRD + 工程脚手架 | 本文档 + 后端微服务骨架 + Vue 前端骨架（本期） |
| M3 后端业务闭环 | 各服务 CRUD + 鉴权 + MQ 跑通 |
| M4 前端联调 | 四屏对接真实接口、图表、社区互动 |
| M5 上线前置 | Nacos/Redis/RocketMQ/MySQL 部署文档、压测、监控 |

---

## 12. 风险与待确认
- 本期脚手架以「可编译、结构正确、主流程跑通」为目标，部分次要字段为预留。
- 短信验证码、支付、IM 等不在本期范围。
- 是否需要「老师/机构」角色、是否需要小程序端，待产品确认。
