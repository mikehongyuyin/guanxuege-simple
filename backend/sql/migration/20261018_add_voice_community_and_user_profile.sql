-- ------------------------------------------------------------
-- 2026-10-18 心声社区 + 用户资料扩展
--
-- 变更内容：
--   1) gx_user    扩展社区昵称 / 微信号 / 邮箱 / 签名
--   2) gx_circle  增加 community 维度（parenting 育儿 / voice 心声），并初始化心声四圈子
--   3) gx_post    增加 community 维度，历史数据统一归为 parenting
--   4) gx_mention 新增：记录帖子 / 评论中 @ 到的用户
--
-- 执行：
--   docker exec -i gx-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 guanxuege < 本文件
-- 幂等：可重复执行（建列前先判断，圈子按 community 先清后插）
-- ------------------------------------------------------------

-- ---------- 1. 用户资料扩展 ----------
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE table_schema = DATABASE() AND table_name = 'gx_user' AND column_name = 'community_nickname');
SET @sql := IF(@exist = 0,
    'ALTER TABLE gx_user ADD COLUMN community_nickname VARCHAR(30) DEFAULT NULL COMMENT ''社区昵称（有则替代账号昵称在社区展示）'' AFTER nickname',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE table_schema = DATABASE() AND table_name = 'gx_user' AND column_name = 'wechat');
SET @sql := IF(@exist = 0,
    'ALTER TABLE gx_user ADD COLUMN wechat VARCHAR(64) DEFAULT NULL COMMENT ''微信号（仅本人可见）'' AFTER avatar',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE table_schema = DATABASE() AND table_name = 'gx_user' AND column_name = 'email');
SET @sql := IF(@exist = 0,
    'ALTER TABLE gx_user ADD COLUMN email VARCHAR(64) DEFAULT NULL COMMENT ''邮箱（仅本人可见）'' AFTER wechat',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE table_schema = DATABASE() AND table_name = 'gx_user' AND column_name = 'signature');
SET @sql := IF(@exist = 0,
    'ALTER TABLE gx_user ADD COLUMN signature VARCHAR(120) DEFAULT NULL COMMENT ''个性签名'' AFTER email',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 2. 圈子归属社区 ----------
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE table_schema = DATABASE() AND table_name = 'gx_circle' AND column_name = 'community');
SET @sql := IF(@exist = 0,
    'ALTER TABLE gx_circle ADD COLUMN community VARCHAR(20) NOT NULL DEFAULT ''parenting'' COMMENT ''所属社区：parenting 育儿 / voice 心声'' AFTER name',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE gx_circle SET community = 'parenting' WHERE community IS NULL OR community = '';

-- 心声社区默认四圈子（先清后插，保证可重复执行）
DELETE FROM gx_circle WHERE community = 'voice';
INSERT INTO gx_circle (`id`, `name`, `community`, `scope`, `description`, `color`, `sort`)
VALUES (101, '工作', 'voice', '职场与打工', '加班、汇报、同事关系，说点工位上不便说的', '#3D405B', 1),
       (102, '副业', 'voice', '第二曲线', '副业起步、时间分配、踩过的坑', '#205C49', 2),
       (103, '创业', 'voice', '从 0 到 1', '找方向、搭团队、活下来的经验', '#B5392E', 3),
       (104, '生活', 'voice', '工作之外', '租房、带娃、健康和那些细碎念头', '#8A6440', 4);

-- ---------- 3. 帖子归属社区 ----------
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE table_schema = DATABASE() AND table_name = 'gx_post' AND column_name = 'community');
SET @sql := IF(@exist = 0,
    'ALTER TABLE gx_post ADD COLUMN community VARCHAR(20) NOT NULL DEFAULT ''parenting'' COMMENT ''所属社区：parenting 育儿 / voice 心声'' AFTER circle_id',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE gx_post SET community = 'parenting' WHERE community IS NULL OR community = '';

SET @idxExist := (SELECT COUNT(*) FROM information_schema.STATISTICS
                  WHERE table_schema = DATABASE() AND table_name = 'gx_post' AND index_name = 'idx_community');
SET @sql := IF(@idxExist = 0, 'ALTER TABLE gx_post ADD INDEX idx_community (community)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 4. @ 提及关系 ----------
CREATE TABLE IF NOT EXISTS `gx_mention`
(
    `id`          BIGINT   NOT NULL COMMENT '雪花主键',
    `biz_type`    VARCHAR(16) NOT NULL COMMENT '业务类型：post 帖子 / comment 评论',
    `biz_id`      BIGINT   NOT NULL COMMENT '业务 id',
    `user_id`     BIGINT   NOT NULL COMMENT '被 @ 的用户 id',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0 正常 / 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_user` (`biz_type`, `biz_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='@提及关系表';

-- 幂等补齐：Mention 继承 BaseEntity（带 @TableLogic），缺少 update_time / deleted 时
-- MyBatis-Plus 会自动拼 `WHERE deleted = 0`，缺列即报 Unknown column 'deleted'。
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE table_schema = DATABASE() AND table_name = 'gx_mention' AND column_name = 'update_time');
SET @sql := IF(@exist = 0,
    'ALTER TABLE gx_mention ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'' AFTER create_time',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE table_schema = DATABASE() AND table_name = 'gx_mention' AND column_name = 'deleted');
SET @sql := IF(@exist = 0,
    'ALTER TABLE gx_mention ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT ''逻辑删除 0 正常 / 1 已删'' AFTER update_time',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
