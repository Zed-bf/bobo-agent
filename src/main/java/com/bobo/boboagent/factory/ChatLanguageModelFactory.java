package com.bobo.boboagent.factory;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ChatLanguageModelFactory {

    private final HashMap<String, ChatLanguageModel> modelMap = new HashMap<>();


    public ChatLanguageModelFactory(OpenAiChatModel deepseekChatModel, QwenChatModel qwenChatModel) {
        modelMap.put("deepseek", deepseekChatModel);
        modelMap.put("qwen", qwenChatModel);
    }

    public ChatLanguageModel getModel(String modelName) {
        if (!modelMap.containsKey(modelName)) {
            throw new IllegalArgumentException("Invalid model name: " + modelName);
        }
        return modelMap.get(modelName);
    }


}
