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
    `community_nickname` VARCHAR(30) DEFAULT NULL COMMENT '社区昵称（有则替代账号昵称在社区展示）',
    `avatar`     VARCHAR(255)    DEFAULT '' COMMENT '头像 URL',
    `wechat`     VARCHAR(64)     DEFAULT NULL COMMENT '微信号（仅本人可见）',
    `email`      VARCHAR(64)     DEFAULT NULL COMMENT '邮箱（仅本人可见）',
    `signature`  VARCHAR(120)    DEFAULT NULL COMMENT '个性签名',
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
    `birthday`   DATE         DEFAULT NULL COMMENT '出生日期',
    `gender`     TINYINT      DEFAULT NULL COMMENT '性别 1 男 / 2 女',
    `personality` VARCHAR(200) DEFAULT '' COMMENT '性格特点（逗号分隔标签）',
    `interests`  VARCHAR(200) DEFAULT '' COMMENT '兴趣特长（逗号分隔标签）',
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
    `images`      VARCHAR(2000) DEFAULT NULL COMMENT '帖子图片（JSON 数组）',
    `tag`         VARCHAR(30)  DEFAULT '' COMMENT '标签',
    `circle_id`   BIGINT       DEFAULT NULL COMMENT '所属圈子 id（分层圈子）',
    `community`   VARCHAR(20)  NOT NULL DEFAULT 'parenting' COMMENT '所属社区：parenting 育儿 / voice 心声',
    `anxiety_flag` TINYINT     NOT NULL DEFAULT 0 COMMENT '命中焦虑词标记 1 是 / 0 否',
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
    KEY `idx_hot` (`hot_score`),
    KEY `idx_community` (`community`)
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
    `images`     VARCHAR(1000) DEFAULT NULL COMMENT '评论图片（JSON 数组）',
    `anxiety_flag` TINYINT     NOT NULL DEFAULT 0 COMMENT '命中焦虑词标记 1 是 / 0 否',
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
-- 帖子点赞关系（保证点赞幂等）
-- ---------------------------
DROP TABLE IF EXISTS `gx_post_like`;
CREATE TABLE `gx_post_like`
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

-- ---------------------------
-- @ 提及关系（帖子 / 评论中 @ 到的用户）
-- ---------------------------
DROP TABLE IF EXISTS `gx_mention`;
CREATE TABLE `gx_mention`
(
    `id`          BIGINT       NOT NULL COMMENT '雪花主键',
    `biz_type`    VARCHAR(16)  NOT NULL COMMENT '业务类型：post 帖子 / comment 评论',
    `biz_id`      BIGINT       NOT NULL COMMENT '业务 id',
    `user_id`     BIGINT       NOT NULL COMMENT '被 @ 的用户 id',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0 正常 / 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_user` (`biz_type`, `biz_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='@提及关系表';

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

-- ---------------------------
-- 家庭档案
-- ---------------------------
DROP TABLE IF EXISTS `gx_family`;
CREATE TABLE `gx_family`
(
    `id`          BIGINT       NOT NULL COMMENT '雪花主键',
    `parent_id`   BIGINT       NOT NULL COMMENT '所属家长 id',
    `family_name` VARCHAR(50)  DEFAULT '' COMMENT '家庭昵称',
    `family_type` VARCHAR(20)  DEFAULT '核心家庭' COMMENT '核心家庭 / 二胎家庭 / 单亲家庭 / 隔代养育 / 其他',
    `motto`       VARCHAR(200) DEFAULT '' COMMENT '家风宣言',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_parent` (`parent_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='家庭档案表';

-- ---------------------------
-- 社区圈子（分层圈子，用户手动加入）
-- ---------------------------
DROP TABLE IF EXISTS `gx_circle`;
CREATE TABLE `gx_circle`
(
    `id`           BIGINT       NOT NULL COMMENT '雪花主键',
    `name`         VARCHAR(50)  NOT NULL COMMENT '圈子名称',
    `community`    VARCHAR(20)  NOT NULL DEFAULT 'parenting' COMMENT '所属社区：parenting 育儿 / voice 心声',
    `scope`        VARCHAR(50)  DEFAULT '' COMMENT '适用阶段，如「一至三年级」',
    `description`  VARCHAR(255) DEFAULT '' COMMENT '圈子简介',
    `color`        VARCHAR(20)  DEFAULT '#81B29A' COMMENT '标识色',
    `member_count` INT          NOT NULL DEFAULT 0 COMMENT '成员数',
    `sort`         INT          NOT NULL DEFAULT 0 COMMENT '排序权重',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='圈子表';

DROP TABLE IF EXISTS `gx_user_circle`;
CREATE TABLE `gx_user_circle`
(
    `id`          BIGINT   NOT NULL COMMENT '雪花主键',
    `user_id`     BIGINT   NOT NULL COMMENT '用户 id',
    `circle_id`   BIGINT   NOT NULL COMMENT '圈子 id',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_circle` (`user_id`, `circle_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户圈子关系表';

INSERT INTO `gx_circle` (`id`, `name`, `community`, `scope`, `description`, `color`, `sort`)
VALUES (1, '幼小衔接', 'parenting', '幼儿园-一年级', '拼音识字、专注力养成', '#E07A5F', 1),
       (2, '小学低年级', 'parenting', '一至三年级', '习惯养成、基础学科', '#81B29A', 2),
       (3, '小学高年级', 'parenting', '四至六年级', '小升初、自律与自驱', '#3D405B', 3),
       (4, '初中生', 'parenting', '七年级及以上', '青春期、学业压力', '#8A6440', 4),
       (5, '二胎家庭', 'parenting', '全阶段', '多孩平衡与陪伴', '#B5392E', 5),
       (101, '工作', 'voice', '职场与打工', '加班、汇报、同事关系，说点工位上不便说的', '#3D405B', 1),
       (102, '副业', 'voice', '第二曲线', '副业起步、时间分配、踩过的坑', '#205C49', 2),
       (103, '创业', 'voice', '从 0 到 1', '找方向、搭团队、活下来的经验', '#B5392E', 3),
       (104, '生活', 'voice', '工作之外', '租房、带娃、健康和那些细碎念头', '#8A6440', 4);

-- ---------------------------
-- 任务打卡
-- ---------------------------
DROP TABLE IF EXISTS `gx_checkin`;
CREATE TABLE `gx_checkin`
(
    `id`           BIGINT       NOT NULL COMMENT '雪花主键',
    `task_id`      BIGINT       NOT NULL COMMENT '计划任务 id',
    `child_id`     BIGINT       NOT NULL COMMENT '孩子 id',
    `checkin_date` DATE         NOT NULL COMMENT '打卡日期',
    `note`         VARCHAR(255) DEFAULT '' COMMENT '打卡记录',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_date` (`task_id`, `checkin_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='任务打卡表';

-- ---------------------------
-- 家庭契约
-- ---------------------------
DROP TABLE IF EXISTS `gx_contract`;
CREATE TABLE `gx_contract`
(
    `id`             BIGINT       NOT NULL COMMENT '雪花主键',
    `parent_id`      BIGINT       NOT NULL COMMENT '家长 id',
    `child_id`       BIGINT       NOT NULL COMMENT '孩子 id',
    `title`          VARCHAR(100) NOT NULL COMMENT '契约标题',
    `child_promise`  VARCHAR(500) DEFAULT '' COMMENT '孩子的承诺',
    `parent_promise` VARCHAR(500) DEFAULT '' COMMENT '家长的承诺',
    `reward`         VARCHAR(255) DEFAULT '' COMMENT '达成奖励',
    `penalty`        VARCHAR(255) DEFAULT '' COMMENT '违约约定',
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '0 进行中 / 1 已完成 / 2 已违约',
    `child_confirm`  TINYINT      NOT NULL DEFAULT 0 COMMENT '孩子已确认 1 是 / 0 否',
    `start_date`     DATE         DEFAULT NULL COMMENT '开始日期',
    `end_date`       DATE         DEFAULT NULL COMMENT '结束日期',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='家庭契约表';

-- ---------------------------
-- 用户同意记录（含儿童个人信息专项同意）
-- ---------------------------
DROP TABLE IF EXISTS `gx_consent_record`;
CREATE TABLE `gx_consent_record`
(
    `id`               BIGINT       NOT NULL COMMENT '雪花主键',
    `parent_id`        BIGINT       NOT NULL COMMENT '家长 id',
    `child_id`         BIGINT       DEFAULT NULL COMMENT '关联孩子 id（注册类同意为 null，儿童信息类同意关联具体孩子）',
    `consent_type`     VARCHAR(32)  NOT NULL COMMENT 'USER_REGISTER 注册同意 / CHILD_INFO 儿童信息专项同意',
    `version`          VARCHAR(16)  NOT NULL COMMENT '同意的文档版本，如 1.0',
    `guardian_relation` VARCHAR(32) DEFAULT NULL COMMENT '监护人关系声明：父母 / 祖父母 / 其他监护人',
    `agreed`           TINYINT      NOT NULL DEFAULT 1 COMMENT '1 已同意 / 0 已撤回',
    `agreed_at`        DATETIME     NOT NULL COMMENT '同意时间',
    `withdrawn_at`     DATETIME     DEFAULT NULL COMMENT '撤回时间',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`          TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_parent` (`parent_id`),
    KEY `idx_child` (`child_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户同意记录表（含儿童个人信息专项同意，保留审计轨迹）';
