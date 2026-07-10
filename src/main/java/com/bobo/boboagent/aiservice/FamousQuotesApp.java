package com.bobo.boboagent.aiservice;

import com.bobo.boboagent.config.MysqlChatMemoryStore;
import com.bobo.boboagent.tool.FamousQuotesTool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;



@Component
public class FamousQuotesApp {


    interface Assistant {

        String chat(@UserMessage String userMessage);

        Flux<String> chatByStream( @UserMessage String userMessage);
    }

    private final OpenAiChatModel openAiChatModel;

    private final StreamingChatLanguageModel streamingChatLanguageModel;

    private final String PROMPT = "调用工具回答指定作者的名人名言";


    private FamousQuotesApp.Assistant assistant;

    private final MysqlChatMemoryStore mysqlChatMemoryStore =
            new MysqlChatMemoryStore("jdbc:mysql://127.0.0.1:3306/bobo_agent" +
                    "?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
                    "root",
                    "991204");


    public FamousQuotesApp(OpenAiChatModel openAiChatModel, StreamingChatLanguageModel streamingChatLanguageModel) {
        this.openAiChatModel = openAiChatModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;


//        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
//                .id(memoryId)
//                .maxMessages(10)
//                .chatMemoryStore(mysqlChatMemoryStore)
//                .build();

        assistant = AiServices.builder(FamousQuotesApp.Assistant.class)
                .chatLanguageModel(openAiChatModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
//                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> PROMPT)
                .tools(new FamousQuotesTool())
                .build();
    }

    public String chat( String question) {
        return assistant.chat(question);
    }


}