package org.lpz.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class LangChainAiInvoke {

    public static void main(String[] args) {
        ChatLanguageModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-plus")
                .build();

        String chat = qwenChatModel.chat("你好，我是李鹏振，请介绍下你自己");
        System.out.println(chat);
    }
}
