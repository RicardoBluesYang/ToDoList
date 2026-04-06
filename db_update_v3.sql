-- TodoList schema upgrade v3
-- Features: due date, subtasks

USE `todolist_db`;

SET @due_col_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'todo_item'
    AND column_name = 'due_date'
);
SET @add_due_col_sql := IF(
  @due_col_exists = 0,
  'ALTER TABLE todo_item ADD COLUMN due_date DATETIME NULL COMMENT ''Task due date''',
  'SELECT 1'
);
PREPARE due_col_stmt FROM @add_due_col_sql;
EXECUTE due_col_stmt;
DEALLOCATE PREPARE due_col_stmt;

CREATE TABLE IF NOT EXISTS `todo_subtask` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `todo_id` bigint(20) NOT NULL,
  `title` varchar(255) NOT NULL,
  `is_completed` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Todo subtasks';

SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'todo_subtask'
    AND index_name = 'idx_subtask_todo_id'
);
SET @create_idx_sql := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_subtask_todo_id ON todo_subtask(todo_id)',
  'SELECT 1'
);
PREPARE idx_stmt FROM @create_idx_sql;
EXECUTE idx_stmt;
DEALLOCATE PREPARE idx_stmt;

SET @fk_exists := (
  SELECT COUNT(1)
  FROM information_schema.table_constraints
  WHERE table_schema = DATABASE()
    AND table_name = 'todo_subtask'
    AND constraint_name = 'fk_subtask_todo'
    AND constraint_type = 'FOREIGN KEY'
);
SET @create_fk_sql := IF(
  @fk_exists = 0,
  'ALTER TABLE todo_subtask ADD CONSTRAINT fk_subtask_todo FOREIGN KEY (todo_id) REFERENCES todo_item(id) ON DELETE CASCADE',
  'SELECT 1'
);
PREPARE fk_stmt FROM @create_fk_sql;
EXECUTE fk_stmt;
DEALLOCATE PREPARE fk_stmt;
