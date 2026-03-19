-- 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS `todolist_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `todolist_db`;

-- 待办事项表
DROP TABLE IF EXISTS `todo_item`;
CREATE TABLE `todo_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '待办事项内容',
  `is_completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否完成 0:未完成 1:已完成',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='待办事项表';

-- 插入测试数据
INSERT INTO `todo_item` (`title`, `is_completed`, `created_at`, `updated_at`) VALUES
('完成数据库设计', 1, NOW(), NOW()),
('搭建SpringBoot后端', 0, NOW(), NOW()),
('Vue前端对接API', 0, NOW(), NOW());
