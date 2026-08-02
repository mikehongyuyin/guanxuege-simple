-- 观学阁 数据库初始化脚本
-- 字符集：utf8mb4 / utf8mb4_general_ci
-- 引擎：InnoDB
-- 逻辑删除统一字段：deleted（1 已删 / 0 未删），由 MyBatis-Plus 自动维护

CREATE DATABASE IF NOT EXISTS `guanxuege`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `guanxuege`;

-- ---------------------------
-- 用户（家长 / 管理员）
-- ---------------------------
DROP TABLE IF EXISTS `gx_user`;
CREATE TABLE `gx_user`
(
    `id`         BIGINT          NOT NULL COMMENT '雪花主键',
    `phone`      VARCHAR(20)     NOT NULL COMMENT '手机号（登录名）',
    `password`   VARCHAR(100)    NOT NULL COMMENT 'BCrypt 加密密码',
    `nickname`   VARCHAR(50)     DEFAULT '' COMMENT '昵称',
    `avatar`     VARCHAR(255)    DEFAULT '' COMMENT '头像 URL',
    `role`       TINYINT         NOT NULL DEFAULT 1 COMMENT '1 家长 / 2 管理员',
    `status`     TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 / 0 禁用',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户表';

-- ---------------------------
-- 孩子档案
-- ---------------------------
DROP TABLE IF EXISTS `gx_child`;
CREATE TABLE `gx_child`
(
    `id`         BIGINT       NOT NULL COMMENT '雪花主键',
    `parent_id`  BIGINT       NOT NULL COMMENT '所属家长 id',
    `name`       VARCHAR(50)  NOT NULL COMMENT '姓名',
    `grade`      VARCHAR(20)  DEFAULT '' COMMENT '年级，如「二年级」',
    `school`     VARCHAR(100) DEFAULT '' COMMENT '学校',
    `avatar`     VARCHAR(255) DEFAULT '' COMMENT '头像',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='孩子档案表';

-- ---------------------------
-- 学习计划
-- ---------------------------
DROP TABLE IF EXISTS `gx_plan`;
CREATE TABLE `gx_plan`
(
    `id`         BIGINT       NOT NULL COMMENT '雪花主键',
    `child_id`   BIGINT       NOT NULL COMMENT '关联孩子 id',
    `title`      VARCHAR(100) NOT NULL COMMENT '计划标题',
    `description` VARCHAR(500) DEFAULT '' COMMENT '计划说明',
    `plan_type`  VARCHAR(20)  DEFAULT 'WEEK' COMMENT 'DAY 日 / WEEK 周 / MONTH 月',
    `status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0 待开始 / 1 进行中 / 2 已完成',
    `start_date` DATE         DEFAULT NULL COMMENT '开始日期',
    `end_date`   DATE         DEFAULT NULL COMMENT '结束日期',
    `progress`   INT          NOT NULL DEFAULT 0 COMMENT '整体进度 0-100',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_child_status` (`child_id`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='学习计划表';

-- ---------------------------
-- 计划任务
-- ---------------------------
DROP TABLE IF EXISTS `gx_plan_task`;
CREATE TABLE `gx_plan_task`
(
    `id`         BIGINT       NOT NULL COMMENT '雪花主键',
    `plan_id`    BIGINT       NOT NULL COMMENT '所属计划 id',
    `title`      VARCHAR(100) NOT NULL COMMENT '任务标题',
    `subject`    VARCHAR(30)  DEFAULT '' COMMENT '科目',
    `status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0 待开始 / 1 进行中 / 2 已完成',
    `due_date`   DATE         DEFAULT NULL COMMENT '截止日期',
    `progress`   INT          NOT NULL DEFAULT 0 COMMENT '进度 0-100',
    `remark`     VARCHAR(255) DEFAULT '' COMMENT '备注',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_plan` (`plan_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='计划任务表';

-- ---------------------------
-- 成绩
-- ---------------------------
DROP TABLE IF EXISTS `gx_grade`;
CREATE TABLE `gx_grade`
(
    `id`          BIGINT        NOT NULL COMMENT '雪花主键',
    `child_id`    BIGINT        NOT NULL COMMENT '关联孩子 id',
    `subject`     VARCHAR(30)   NOT NULL COMMENT '科目',
    `exam_name`   VARCHAR(100)  DEFAULT '' COMMENT '考试名称',
    `score`       DECIMAL(6, 2) NOT NULL COMMENT '得分',
    `full_score`  DECIMAL(6, 2) NOT NULL DEFAULT 100.00 COMMENT '满分',
    `exam_date`   DATE          DEFAULT NULL COMMENT '考试日期',
    `rank_in_class` INT         DEFAULT NULL COMMENT '班级排名',
    `comment`     VARCHAR(255)  DEFAULT '' COMMENT '评语',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_child_subject` (`child_id`, `subject`),
    KEY `idx_exam_date` (`exam_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='成绩表';

-- ---------------------------
-- 社区帖子
-- ---------------------------
DROP TABLE IF EXISTS `gx_post`;
CREATE TABLE `gx_post`
(
    `id`          BIGINT       NOT NULL COMMENT '雪花主键',
    `author_id`   BIGINT       NOT NULL COMMENT '作者用户 id',
    `title`       VARCHAR(150) NOT NULL COMMENT '标题',
    `content`     TEXT         COMMENT '正文',
    `tag`         VARCHAR(30)  DEFAULT '' COMMENT '标签',
    `view_count`  INT          NOT NULL DEFAULT 0 COMMENT '浏览数',
    `like_count`  INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT        NOT NULL DEFAULT 0 COMMENT '评论数',
    `hot_score`   INT          NOT NULL DEFAULT 0 COMMENT '热度分',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 / 0 下架',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_author` (`author_id`),
    KEY `idx_hot` (`hot_score`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='帖子表';

-- ---------------------------
-- 评论
-- ---------------------------
DROP TABLE IF EXISTS `gx_comment`;
CREATE TABLE `gx_comment`
(
    `id`         BIGINT       NOT NULL COMMENT '雪花主键',
    `post_id`    BIGINT       NOT NULL COMMENT '帖子 id',
    `user_id`    BIGINT       NOT NULL COMMENT '评论用户 id',
    `parent_id`  BIGINT       DEFAULT NULL COMMENT '父评论 id（回复）',
    `content`    VARCHAR(500) NOT NULL COMMENT '评论内容',
    `like_count` INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_post` (`post_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='评论表';

-- ---------------------------
-- 热议话题
-- ---------------------------
DROP TABLE IF EXISTS `gx_topic`;
CREATE TABLE `gx_topic`
(
    `id`            BIGINT       NOT NULL COMMENT '雪花主键',
    `name`          VARCHAR(50)  NOT NULL COMMENT '话题名称',
    `discuss_count` INT          NOT NULL DEFAULT 0 COMMENT '讨论数',
    `rank`          INT          NOT NULL DEFAULT 0 COMMENT '排序权重',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='热议话题表';

-- ---------------------------
-- 初始化若干热议话题（与社区页示例一致）
-- ---------------------------
INSERT INTO `gx_topic` (`id`, `name`, `discuss_count`, `rank`)
VALUES (1, '习惯养成', 512, 5),
       (2, '成绩交流', 430, 4),
       (3, '双减政策', 387, 3),
       (4, '亲子阅读', 295, 2),
       (5, '二胎家庭', 241, 1);
