package com.bobo.boboagent.aiservice;

import com.alibaba.dashscope.assistants.Assistant;
import com.bobo.boboagent.config.MysqlChatMemoryStore;
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
public class LuckyApp {


    interface Assistant {

        String chat(@MemoryId String memoryId, @UserMessage String userMessage);

        Flux<String> chatByStream(@MemoryId String memoryId, @UserMessage String userMessage);
    }

    private final OpenAiChatModel openAiChatModel;

    private final StreamingChatLanguageModel streamingChatLanguageModel;

    private final String PROMPT = "你是一位精通测字术、阅历深厚的传统命理先生，擅长一字断运势。\\n\" +\n" +
            "            \"请依据用户提供的单个汉字，完整解析近期整体运势，分三块清晰作答：\\n\" +\n" +
            "            \"1.当下最兴旺的运势领域（财运、事业、感情、健康、人际等），附带详细解读；\\n\" +\n" +
            "            \"2.运势低迷、易出波折的方面，说明潜在隐患；\\n\" +\n" +
            "            \"3。针对性避坑建议、化解注意事项，给出实用可行的提点。\\n\" +\n" +
            "            \"行文文风沉稳老道，通俗易懂，不使用晦涩难懂的玄学术语，客观理性，不制造焦虑。\\n\" +\n" +
            "            \"如果用户输入不是一个字；请提示用户数输入单个汉字";



    private Assistant assistant;

    private final  MysqlChatMemoryStore mysqlChatMemoryStore =
            new MysqlChatMemoryStore("jdbc:mysql://127.0.0.1:3306/bobo_agent" +
                    "?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true",
                    "root",
                    "991204");


    public LuckyApp(OpenAiChatModel openAiChatModel, StreamingChatLanguageModel streamingChatLanguageModel) {
        this.openAiChatModel = openAiChatModel;
        this.streamingChatLanguageModel = streamingChatLanguageModel;


        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(mysqlChatMemoryStore)
                .build();

        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(openAiChatModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .systemMessageProvider(memoryId -> PROMPT)
                .build();
    }

    public String lucky(String memoryId, String question) {

        return assistant.chat(memoryId, question);
    }

    public Flux<String> luckyByStream(String memoryId, String question) {

        return assistant.chatByStream(memoryId, question);
    }
}
