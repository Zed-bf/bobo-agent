package com.bobo.boboagent.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
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
//        String sql = "SELECT role, content FROM chat_messages WHERE session_id = ? ORDER BY created_at DESC Limit 10";

        String sql = "SELECT role, content FROM chat_messages WHERE session_id = ? ORDER BY created_at ASC ";

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
//        Collections.reverse(messages);
        return messages;

    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // 这是一个简化的策略：先删后插。
        // 生产环境为了性能，应该只追加新消息，或者做 Diff。

        // 只追加最后一条新消息，避免全量覆盖带来的性能问题和数据丢失风险
        if (messages == null || messages.isEmpty()) {
            return;
        }

        ChatMessage lastMessage = messages.get(messages.size() - 1);
        String role;
        String content;

        if (lastMessage instanceof UserMessage) {
            role = "user";
            content = ((UserMessage) lastMessage).singleText();
        } else if (lastMessage instanceof AiMessage) {
            role = "ai";
            content = ((AiMessage) lastMessage).text();
        } else if (lastMessage instanceof SystemMessage) {
            role = "system";
            content = ((SystemMessage) lastMessage).text();
        } else {
            // 忽略不支持的消息类型
            return;
        }

        String insertSql = "INSERT INTO chat_messages (session_id, role, content, created_at) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setString(1, memoryId.toString());
            stmt.setString(2, role);
            stmt.setString(3, content);
            stmt.executeUpdate();

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
