-- 增量迁移：新增帖子点赞关系表（用于点赞幂等 / toggle）
-- 适用版本：2026-10-17 起（P0 修复：点赞无幂等）
-- 日期：2026-10-17
--
-- 说明：
--   - docker-entrypoint-initdb.d 仅在数据卷为空时自动执行，
--     已运行的数据库不会自动应用 schema.sql 的变更，需手动执行本文件
--   - 幂等设计：CREATE TABLE IF NOT EXISTS，可安全重复执行
--   - 不含 DROP 语句，不会影响任何既有数据

CREATE TABLE IF NOT EXISTS `gx_post_like`
(
    `id`          BIGINT   NOT NULL COMMENT '雪花主键',
    `post_id`     BIGINT   NOT NULL COMMENT '帖子 id',
    `user_id`     BIGINT   NOT NULL COMMENT '点赞用户 id',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='帖子点赞关系表';

-- 执行后校验（应返回 gx_post_like 一行）
-- SELECT table_name FROM information_schema.tables
--  WHERE table_schema = 'guanxuege' AND table_name = 'gx_post_like';
