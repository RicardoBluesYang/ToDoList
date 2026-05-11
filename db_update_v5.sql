-- TodoList schema upgrade v5
-- Features: task priority, notes, subtask progress tracking

USE `todolist_db`;

ALTER TABLE `todo_item`
  ADD COLUMN `priority` TINYINT NOT NULL DEFAULT 0 COMMENT '0=low, 1=medium, 2=high',
  ADD COLUMN `notes` TEXT COMMENT 'user notes for the task';
