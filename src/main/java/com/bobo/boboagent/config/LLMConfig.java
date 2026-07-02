package com.bobo.boboagent.config;


import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private  String deepseekApiKey ;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private  String deepseekBaseUrl ;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private  String deepseekModelName;


//    @Bean
//    public OpenAiChatModel deepseekChatModel() {
//        return OpenAiChatModel.builder()
//                .apiKey("sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
//                .baseUrl("https://api.deepseek.ai/v1")
//                .modelName("gpt-3.5-turbo")
//                .build();
//    }

    @Bean
    public StreamingChatLanguageModel streamingDeepseekChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(deepseekApiKey)
                .baseUrl(deepseekBaseUrl)
                .modelName(deepseekModelName)
                .build();
    }
}
