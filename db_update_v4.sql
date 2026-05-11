-- TodoList schema upgrade v4
-- Features: lightweight AI call statistics for admin dashboard

USE `todolist_db`;

CREATE TABLE IF NOT EXISTS `ai_call_stat` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `status` varchar(20) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_call_stat_created_at` (`created_at`),
  KEY `idx_ai_call_stat_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI call statistics';

SET @ai_stat_created_idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'ai_call_stat'
    AND index_name = 'idx_ai_call_stat_created_at'
);
SET @ai_stat_created_idx_sql := IF(
  @ai_stat_created_idx_exists = 0,
  'CREATE INDEX idx_ai_call_stat_created_at ON ai_call_stat(created_at)',
  'SELECT 1'
);
PREPARE ai_stat_created_idx_stmt FROM @ai_stat_created_idx_sql;
EXECUTE ai_stat_created_idx_stmt;
DEALLOCATE PREPARE ai_stat_created_idx_stmt;

SET @ai_stat_status_idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'ai_call_stat'
    AND index_name = 'idx_ai_call_stat_status'
);
SET @ai_stat_status_idx_sql := IF(
  @ai_stat_status_idx_exists = 0,
  'CREATE INDEX idx_ai_call_stat_status ON ai_call_stat(status)',
  'SELECT 1'
);
PREPARE ai_stat_status_idx_stmt FROM @ai_stat_status_idx_sql;
EXECUTE ai_stat_status_idx_stmt;
DEALLOCATE PREPARE ai_stat_status_idx_stmt;
