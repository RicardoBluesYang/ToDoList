CREATE DATABASE IF NOT EXISTS `todolist_db`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `todolist_db`;

DROP TABLE IF EXISTS `todo_subtask`;
DROP TABLE IF EXISTS `todo_item`;
DROP TABLE IF EXISTS `ai_call_stat`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `todo_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `is_completed` tinyint(1) NOT NULL DEFAULT '0',
  `due_date` datetime NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_todo_user_id` (`user_id`),
  CONSTRAINT `fk_todo_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `todo_subtask` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `todo_id` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `is_completed` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_subtask_todo_id` (`todo_id`),
  CONSTRAINT `fk_subtask_todo` FOREIGN KEY (`todo_id`) REFERENCES `todo_item` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `ai_call_stat` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `status` varchar(20) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_call_stat_created_at` (`created_at`),
  KEY `idx_ai_call_stat_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (`username`, `password`, `created_at`)
VALUES ('admin', '123456', NOW());

INSERT INTO `todo_item` (`user_id`, `title`, `is_completed`, `due_date`, `created_at`, `updated_at`)
VALUES
  ((SELECT id FROM `user` WHERE username = 'admin'), 'Finish database design', 1, NOW(), NOW(), NOW()),
  ((SELECT id FROM `user` WHERE username = 'admin'), 'Connect frontend to API', 0, DATE_ADD(NOW(), INTERVAL 1 DAY), NOW(), NOW());
