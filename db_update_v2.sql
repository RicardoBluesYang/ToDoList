-- 数据库更新脚本 (支持多用户系统)

USE `todolist_db`;

-- 1. 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 修改待办事项表，添加用户关联
-- 先添加列
ALTER TABLE `todo_item` ADD COLUMN `user_id` bigint(20) COMMENT '所属用户ID' AFTER `id`;

-- 3. 插入默认用户 (admin/123456)
INSERT INTO `user` (`username`, `password`, `created_at`) VALUES ('admin', '123456', NOW());

-- 4. 将现有的待办事项归属给默认用户 (防止旧数据悬空)
UPDATE `todo_item` SET `user_id` = (SELECT `id` FROM `user` WHERE `username` = 'admin');

-- 5. 添加外键约束 (可选，严格模式推荐)
ALTER TABLE `todo_item` ADD CONSTRAINT `fk_todo_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
