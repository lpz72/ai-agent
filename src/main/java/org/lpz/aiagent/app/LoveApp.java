package org.lpz.aiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lpz.aiagent.advisor.MyLoggerAdvisor;
import org.lpz.aiagent.advisor.ReReadingAdvisor;
import org.lpz.aiagent.chatmemory.FileBasedChatMemory;
import org.lpz.aiagent.rag.LoveAppRagCustomAdvisorFactory;
import org.lpz.aiagent.rag.QueryRewriter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.FunctionPromptTemplate;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱\n" +
            "难题。围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追\n" +
            "求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询\n" +
            "问家庭责任与亲属关系处理的问题。引导用户详述事情经过、对方反应及自\n" +
            "身想法，以便给出专属解决方案。";

    /**
     * 初始化 ChatClient（AI客户端）
     * @param dashscopeChatModel
     */
    //这里是直接根据名称dashscopeChatModel查找
    public LoveApp(ChatModel dashscopeChatModel) {

        //初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

//       //初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志Advisor，可按需开启
                        new MyLoggerAdvisor()
//                        //自定义推理增强Advisor，可按需开启
//                        ,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮记忆对话）
     * @param message
     * @param chatId 对话id
     * @return
     */
    public String doChat(String message,String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1)) //支持记忆的对话条数为10
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

    /**
     * AI 基础对话（支持多轮记忆对话,sse流式传输）
     * @param message
     * @param chatId 对话id
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1)) //支持记忆的对话条数为10
                .stream()
                .content();
    }


    record LoveReport(String title,List<String> suggestions){

    }

    /**
     * AI 恋爱报告功能（实战结构化输出）
     * @param message
     * @param chatId 对话id
     * @return
     */
    public LoveReport doChatWithReport(String message,String chatId) {
        LoveReport loveReport = chatClient.prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)) //支持记忆的对话条数为10
                .call()
                .entity(LoveReport.class);

        log.info("lovereport: {}",loveReport);
        return loveReport;
    }

    // AI 恋爱知识库问答功能
    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;


    /**
     * 和 RAG 知识库进行对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message,String chatId){

        String rewriterMessage = queryRewriter.doQueryRewriter(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用查询重写器
                .user(rewriterMessage)
//                .user(message)
                // 支持多轮对话
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用RAG知识库问答
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                //应用 RAG 检索增强服务（基于云知识库）
//                .advisors(loveAppRagCloudAdvisor)
                // 应用 RAG 检索增强服务 （基于pgVector）
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的RAG检索增强服务（文档查询器 + 上下文文本增强）
//                .advisors(
//                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
//                                loveAppVectorStore,"单身")
//                )
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }


    @Resource
    private ToolCallback[] allTools;

    /** AI 工具调用
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message,String chatId){

        ChatResponse chatResponse = chatClient
                .prompt()
                // 支持多轮对话
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /** AI 工具调用 (MCP服务)
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithMcp(String message,String chatId){

        ChatResponse chatResponse = chatClient
                .prompt()
                // 支持多轮对话
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

}
