-- ============================================================
-- 观学阁：雪花 ID 精度修复脚本
-- ============================================================
-- 背景：
--   后端使用 19 位雪花 ID，超出 JS Number 安全整数范围（2^53）。
--   前端 JS 解析 JSON 数字时精度被截断（如 2083706548530819073 -> 2083706548530819000），
--   导致创建计划/成绩/评论时把"截断的" child_id / plan_id / post_id 写入数据库。
--   本脚本把这些截断的关联字段修正为数据库中的真实 ID（按数值距离就近匹配，容差 2000）。
--
-- 执行方式（任选其一）：
--   1. Navicat / DBeaver / IDEA Database 中，选中 guanxuege 库执行
--   2. 命令行：mysql -h127.0.0.1 -uroot -p123456 guanxuege < fix_snowflake_id.sql
-- 建议先备份数据。
-- ============================================================

-- 1) 修复 gx_plan.child_id -> gx_child.id
UPDATE gx_plan p
JOIN gx_child c ON ABS(c.id - p.child_id) < 2000
SET p.child_id = c.id
WHERE p.child_id IS NOT NULL
  AND p.child_id NOT IN (SELECT id FROM gx_child);

-- 2) 修复 gx_grade.child_id -> gx_child.id
UPDATE gx_grade g
JOIN gx_child c ON ABS(c.id - g.child_id) < 2000
SET g.child_id = c.id
WHERE g.child_id IS NOT NULL
  AND g.child_id NOT IN (SELECT id FROM gx_child);

-- 3) 修复 gx_comment.post_id -> gx_post.id
UPDATE gx_comment cm
JOIN gx_post pt ON ABS(pt.id - cm.post_id) < 2000
SET cm.post_id = pt.id
WHERE cm.post_id IS NOT NULL
  AND cm.post_id NOT IN (SELECT id FROM gx_post);

-- 4) 修复 gx_plan_task.plan_id -> gx_plan.id
UPDATE gx_plan_task t
JOIN gx_plan p ON ABS(p.id - t.plan_id) < 2000
SET t.plan_id = p.id
WHERE t.plan_id IS NOT NULL
  AND t.plan_id NOT IN (SELECT id FROM gx_plan);

-- 验证（执行后应返回 0 行）：
-- SELECT * FROM gx_plan    WHERE child_id IS NOT NULL AND child_id NOT IN (SELECT id FROM gx_child);
-- SELECT * FROM gx_grade   WHERE child_id IS NOT NULL AND child_id NOT IN (SELECT id FROM gx_child);
-- SELECT * FROM gx_comment WHERE post_id  IS NOT NULL AND post_id  NOT IN (SELECT id FROM gx_post);
-- SELECT * FROM gx_plan_task WHERE plan_id IS NOT NULL AND plan_id NOT IN (SELECT id FROM gx_plan);
