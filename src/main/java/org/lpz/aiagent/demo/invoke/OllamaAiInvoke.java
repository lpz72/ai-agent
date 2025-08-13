//package org.lpz.aiagent.demo.invoke;
//
//import jakarta.annotation.Resource;
//import org.springframework.ai.chat.messages.AssistantMessage;
//import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
///**
// * Spring AI 框架调用 AI 大模型（Ollama）
// */
////可单次运行，启动项目使时行
//@Component
//public class OllamaAiInvoke implements CommandLineRunner {
//
//    @Resource
//    private ChatModel ollamaChatModel;
//
//    @Override
//    public void run(String... args) throws Exception {
//        AssistantMessage assistantMessage = ollamaChatModel.call(new Prompt("你好，我是李鹏振"))
//                .getResult() // 得到一个对象
//                .getOutput(); // 取出对象中的结果
//
//        System.out.println(assistantMessage.getText());
//    }
//}
