package com.bobo.boboagent.store;


import com.alibaba.dashscope.assistants.Assistant;
import com.bobo.boboagent.config.MysqlChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class mysqlChatMemoryStoreTest {


    @Autowired
    private OpenAiChatModel openAiChatModel;

    interface Assistant {

        String chat(@MemoryId Integer memoryId, @UserMessage String userMessage);
    }


    @Test
    public void test() {

        MysqlChatMemoryStore mysqlChatMemoryStore = new MysqlChatMemoryStore("jdbc:mysql://127.0.0.1:3306/bobo_agent?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true", "root", "991204");

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(mysqlChatMemoryStore)
                .build();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(openAiChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();

        String chat = assistant.chat( 1, "我是波波");
        System.out.println(chat);
        chat = assistant.chat( 1 , "我是谁");
        System.out.println(chat);

    }


}
