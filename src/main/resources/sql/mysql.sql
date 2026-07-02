CREATE TABLE chat_messages (
                               id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               session_id VARCHAR(64) NOT NULL COMMENT '会话ID，对应LangChain4j中的memoryId',
                               user_id VARCHAR(64) DEFAULT NULL COMMENT '用户ID（可选，用于跨设备同步）',
                               role ENUM('system', 'user', 'ai', 'tool') NOT NULL COMMENT '消息角色',
                               content TEXT NOT NULL COMMENT '消息内容',
                               token_count INT DEFAULT 0 COMMENT 'Token消耗（可选，用于统计）',
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 索引优化：这是查询的核心，必须按会话ID和时间排序
                               INDEX idx_session_time (session_id, created_at),
                               INDEX idx_user_session (user_id, session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent对话全量日志';