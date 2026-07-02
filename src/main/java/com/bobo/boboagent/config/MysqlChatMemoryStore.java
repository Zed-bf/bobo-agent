package com.bobo.boboagent.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class MysqlChatMemoryStore implements ChatMemoryStore {


    private final String url;

    private final String username;

    private final String password;

    public MysqlChatMemoryStore(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }


    @Override
    public List<ChatMessage> getMessages(Object memoryId) {

        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT role, content FROM chat_messages WHERE session_id = ? ORDER BY created_at ASC";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, memoryId.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String role = rs.getString("role");
                String content = rs.getString("content");

                // 将数据库记录转换回 LangChain4j 的 ChatMessage 对象
                switch (role.toLowerCase()) {
                    case "user":
                        messages.add(new UserMessage(content));
                        break;
                    case "ai":
                        messages.add(new AiMessage(content));
                        break;
                    case "system":
                        messages.add(new SystemMessage(content));
                        break;
                    // 处理 Tool message 等...
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch messages from MySQL", e);
        }
        return messages;

    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // 这是一个简化的策略：先删后插。
        // 生产环境为了性能，应该只追加新消息，或者做 Diff。

        String deleteSql = "DELETE FROM chat_messages WHERE session_id = ?";
        String insertSql = "INSERT INTO chat_messages (session_id, role, content, created_at) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            conn.setAutoCommit(false); // 开启事务

            // 1. 清除旧数据 (注意：如果有其他依赖此数据的逻辑，慎用全量覆盖)
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setString(1, memoryId.toString());
                stmt.executeUpdate();
            }

            // 2. 插入当前全量消息列表
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                for (ChatMessage msg : messages) {
                    stmt.setString(1, memoryId.toString());

                    if (msg instanceof UserMessage) {
                        stmt.setString(2, "user");
                        stmt.setString(3, ((UserMessage) msg).singleText());
                    } else if (msg instanceof AiMessage) {
                        stmt.setString(2, "ai");
                        stmt.setString(3, ((AiMessage) msg).text());
                    } else if (msg instanceof SystemMessage) {
                        stmt.setString(2, "system");
                        stmt.setString(3, ((SystemMessage) msg).text());
                    } else {
                        continue; // 忽略不支持的类型或做特定处理
                    }
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit(); // 提交事务
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update messages in MySQL", e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String sql = "DELETE FROM chat_messages WHERE session_id = ?";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memoryId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
