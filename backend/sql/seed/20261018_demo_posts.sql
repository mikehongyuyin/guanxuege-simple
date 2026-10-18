-- ------------------------------------------------------------
-- 演示数据：社区帖子 / 评论 / 点赞
--
-- 用途：让仓库截图与在线演示呈现真实感的社区内容，
--       替换开发过程中产生的测试数据（title = 'tests' / 'aaaaaa'）。
-- 执行：docker exec -i gx-mysql mysql -uroot -p123456 --default-character-set=utf8mb4 guanxuege < 本文件
--
-- 回滚：
--   UPDATE gx_post SET deleted = 0 WHERE id IN (2085711872285941761, 2085738984479973378);
--   DELETE FROM gx_post    WHERE id BETWEEN 2086000000000000000 AND 2086000000000000099;
--   DELETE FROM gx_comment WHERE id BETWEEN 2086000000000000100 AND 2086000000000000199;
--   DELETE FROM gx_post_like WHERE id BETWEEN 2086000000000000200 AND 2086000000000000299;
-- ------------------------------------------------------------

-- 下架开发期遗留的测试帖（逻辑删除，可回滚）
UPDATE gx_post SET deleted = 1
WHERE id IN (2085711872285941761, 2085738984479973378);

-- 帖子：作者 2084967497249697793 云朵爸爸 / 2084978582530420737 小云朵妈妈
INSERT INTO gx_post
    (id, author_id, title, content, images, tag, circle_id,
     anxiety_flag, view_count, like_count, comment_count, hot_score,
     status, create_time, update_time, deleted)
VALUES
    (2086000000000000001, 2084967497249697793,
     '一年级的识字任务，拆成 15 分钟后终于能坚持了',
     '之前每天安排半小时识字，孩子坐不住，我也忍不住开始催。后来改成早晚各 15 分钟，配一张打卡表，他自己贴星星。\n\n坚持四周的变化：\n1. 不用提醒，吃完早饭自己去拿字卡；\n2. 认错的字单独抄在本子上，第二天先复习；\n3. 周末他会主动挑几个字考我。\n\n我的体会是：时长不是关键，可预期的节奏才是。定一个他确定能完成的量，比定一个看起来够努力的量有用得多。',
     NULL, '习惯养成', 2,
     0, 268, 2, 2, 5,
     1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 0),

    (2086000000000000002, 2084978582530420737,
     '用一张作业时段表，把晚上的催促换成提醒',
     '以前每晚都要反复催，后来和孩子一起画了一张时段表贴在书桌前：\n\n19:00-19:30 数学\n19:40-20:10 语文\n20:20-20:35 整理书包\n20:35 以后自由安排\n\n关键不是把时间填满，而是让他知道接下来是什么。两周下来，我提醒的次数从每晚七八次降到一两次，他也更愿意自己安排顺序。',
     NULL, '时间管理', 3,
     0, 154, 1, 1, 3,
     1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),

    (2086000000000000003, 2084967497249697793,
     '幼小衔接这一年，我们只做了三件事',
     '没有提前学完一年级课本，只守着三条：\n\n1. 每天固定 20 分钟亲子共读，书由他挑；\n2. 自己整理书包和文具，忘带了就自己承担；\n3. 早睡，保证十小时。\n\n入学两个月，适应得比预期顺利。回头看，衔接的重点不是抢跑多少知识，而是作息、自理和对上学的期待。',
     NULL, '亲子共读', 1,
     0, 96, 0, 0, 1,
     1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0);

-- 评论（comment_count 与上方保持一致：帖 1 两条、帖 2 一条）
INSERT INTO gx_comment
    (id, post_id, user_id, parent_id, content, images, anxiety_flag, like_count,
     create_time, update_time, deleted)
VALUES
    (2086000000000000101, 2086000000000000001, 2084978582530420737, NULL,
     '贴星星这招我们也用，但总忘记贴。后来把表挂到书包旁边，出门就能看见，好多了。',
     NULL, 0, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),

    (2086000000000000102, 2086000000000000001, 2084967497249697793, NULL,
     '对，位置比奖励更重要。放在必经之路上，比反复提醒省事得多。',
     NULL, 0, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),

    (2086000000000000103, 2086000000000000002, 2084967497249697793, NULL,
     '时段表我试过，关键是别把周末也排满，留白很重要。',
     NULL, 0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0);

-- 点赞关系（like_count 与上方保持一致：帖 1 两个、帖 2 一个）
INSERT INTO gx_post_like (id, post_id, user_id, create_time)
VALUES
    (2086000000000000201, 2086000000000000001, 2084967497249697793, DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (2086000000000000202, 2086000000000000001, 2084978582530420737, DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (2086000000000000203, 2086000000000000002, 2084967497249697793, DATE_SUB(NOW(), INTERVAL 1 DAY));
