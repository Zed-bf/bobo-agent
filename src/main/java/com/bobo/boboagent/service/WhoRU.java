package com.bobo.boboagent.service;


import com.bobo.boboagent.factory.ChatLanguageModelFactory;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WhoRU {


   @Autowired
   private ChatLanguageModelFactory chatLanguageModelFactory;

    private static final String PROMPT = "你叫什么名字";


    public String whoRU(String model){

        ChatLanguageModel chatLanguageModel = chatLanguageModelFactory.getModel(model);
        return chatLanguageModel.chat(PROMPT);

    }
}
