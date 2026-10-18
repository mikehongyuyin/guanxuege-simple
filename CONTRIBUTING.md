# 贡献指南

感谢你对观学阁感兴趣。本文档说明如何参与开发。

## 开发约定

### 分支

- `main` —— 稳定分支，随时可部署
- 功能开发请从 `main` 切出 `feat/xxx` 分支，完成后提 PR

### 提交信息

采用 Conventional Commits 格式：

```
feat: 新增成绩趋势图
fix: 修复打卡连续天数在当日未打卡时误判为断签
docs: 补充架构决策记录
refactor: 抽取归属校验为横切服务
test: 补充点赞幂等测试
```

### 代码规范

**后端**

- 包结构按业务边界划分，新模块请放在对应业务包下
- Controller 不写业务逻辑，只做参数接收与归属校验
- 所有涉及孩子 / 计划 / 任务 / 成绩的接口，**必须先调用 `OwnershipService` 校验归属**
- 写操作加 `@Transactional(rollbackFor = Exception.class)`
- 计数类更新用数据库原子操作，避免「读-改-写」

**前端**

- 组件使用组合式 API（`<script setup>`）
- 接口请求统一走 `src/api/` 下的封装，不在组件里直接写 axios

### 数据库变更

`backend/sql/schema.sql` 仅在数据卷为空时自动执行，**已运行的库不会自动应用变更**。

修改表结构时：

1. 同步更新 `schema.sql`（保证新环境正确）
2. 在 `backend/sql/migration/` 新增增量脚本，命名 `YYYYMMDD_说明.sql`
3. 增量脚本必须幂等（用 `IF NOT EXISTS`）

## 提交 PR 前

```bash
# 后端：确保测试通过
cd backend && mvn test

# 前端：确保构建通过
cd frontend && npm run build
```

CI 会自动执行以上检查。

## 安全

请勿提交任何凭据：私钥（`*.pem` / `*.key`）、环境变量文件（`.env`）、服务器地址、真实用户数据。
`.gitignore` 已覆盖上述类型，提交前请确认。
