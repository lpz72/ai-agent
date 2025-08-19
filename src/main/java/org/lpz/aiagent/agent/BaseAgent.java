package org.lpz.aiagent.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lpz.aiagent.agent.model.AgentState;
import org.lpz.aiagent.tools.WebSearchTool;
import org.lpz.aiagent.utils.ResponseCleanupUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    //核心属性
    private String name;

    //提示
    private String systemPrompt;
    private String nextStepPrompt;

    //状态
    private AgentState state = AgentState.IDLE;

    //执行控制
    private int maxSteps = 10;
    private int currentStep = 0;


    //LLM 大模型
    private ChatClient chatClient;

    //Memory (需要自出维护会话上下文)
    private List<Message> messageList = new ArrayList<>();

    public String run(String userPrompt) {

        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }

        if (StringUtils.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        //更改状态
        state = AgentState.RUNNING;

        //记录消息上下文
        messageList.add(new UserMessage(userPrompt));

        //保存结果列表
        List<String> results = new ArrayList<>();

        try {
            for (int i = 0;i < maxSteps && state != AgentState.FINISHED;i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);

                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);

            }

            // 检查是否超过步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            // 将所有结果拼接，回车分割
            return String.join("\n",results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();

        }

    }

    /**
     * 运行代理 （流式输出）
     * @param userPrompt
     * @return
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建SseEmitter，设置较长的超时时间
        SseEmitter sseEmitter = new SseEmitter(300000L);// 5分钟超时时间

        // 使用线程异步处理，防止阻塞主线程
        CompletableFuture.runAsync(() -> {

            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("错误：无法从" + this.state + "状态运行到代理");
                    sseEmitter.complete();
                   return;
                }

                if (StringUtils.isBlank(userPrompt)) {
                    sseEmitter.send("错误：不能使用空提示词运行代理");
                    sseEmitter.complete();
                    return;
                }

                //更改状态
                state = AgentState.RUNNING;

                //记录消息上下文
                messageList.add(new UserMessage(userPrompt));

                //保存结果列表
                List<String> results = new ArrayList<>();

                try {
                    for (int i = 0;i < maxSteps && state != AgentState.FINISHED;i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("Executing step " + stepNumber + "/" + maxSteps);

                        // 单步执行
                        String stepResult = step();
                        String result = "Step " + stepNumber + ": " + stepResult;
                        results.add(result);
                        //发送每一步的结果
                        sseEmitter.send(result);

                    }

                    // 检查是否超过步骤限制
                    if (currentStep >= maxSteps) {
                        state = AgentState.FINISHED;
                        results.add("Terminated: Reached max steps (" + maxSteps + ")");
                        sseEmitter.send("执行结束：达到最大步骤(" + maxSteps + ")");
                    }

                    // 正常完成
                    sseEmitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("Error executing agent", e);
                    try {
                        sseEmitter.send("执行错误" + e.getMessage());
                    } catch (IOException ex) {
                        sseEmitter.completeWithError(ex);
                    }
                    sseEmitter.complete();
                } finally {
                    // 清理资源
                    this.cleanup();

                }
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }

        });

        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return sseEmitter;
    }

    /**
     * 运行代理 （流式输出）
     * @param userPrompt
     * @return
     */
//    public SseEmitter runStream(String userPrompt) {
//        // 创建SseEmitter，设置较长的超时时间
//        SseEmitter sseEmitter = new SseEmitter(300000L);// 5分钟超时时间
//
//        // 使用线程异步处理，防止阻塞主线程
//        CompletableFuture.runAsync(() -> {
//
//            try {
//                if (this.state != AgentState.IDLE) {
//                    sseEmitter.send("错误：无法从" + this.state + "状态运行到代理");
//                    sseEmitter.complete();
//                    return;
//                }
//
//                if (StringUtils.isBlank(userPrompt)) {
//                    sseEmitter.send("错误：不能使用空提示词运行代理");
//                    sseEmitter.complete();
//                    return;
//                }
//
//                //更改状态
//                state = AgentState.RUNNING;
//
//                //记录消息上下文
//                messageList.add(new UserMessage(userPrompt));
//
//                //保存结果列表
//                List<String> results = new ArrayList<>();
//
//                try {
//                    for (int i = 0;i < maxSteps && state != AgentState.FINISHED;i++) {
//                        int stepNumber = i + 1;
//                        currentStep = stepNumber;
//                        log.info("Executing step " + stepNumber + "/" + maxSteps);
//
//                        // 单步执行
//                        String stepResult = step();
//
//                        // ===== 新增：清理步骤结果 =====
//                        String cleanedStepResult = cleanStepResult(stepResult);
//
//                        String result = "Step " + stepNumber + "\n\n" + cleanedStepResult;
//                        results.add(result);
//
//                        //发送每一步的结果
//                        sseEmitter.send(result);
//                    }
//
//                    // 检查是否超过步骤限制
//                    if (currentStep >= maxSteps) {
//                        state = AgentState.FINISHED;
//                        String terminationMsg = "执行结束：达到最大步骤(" + maxSteps + ")";
//                        results.add(terminationMsg);
//                        sseEmitter.send(terminationMsg);
//                    }
//
//                    // 正常完成
//                    sseEmitter.complete();
//                } catch (Exception e) {
//                    state = AgentState.ERROR;
//                    log.error("Error executing agent", e);
//                    try {
//                        sseEmitter.send("执行错误：" + e.getMessage());
//                    } catch (IOException ex) {
//                        sseEmitter.completeWithError(ex);
//                    }
//                    sseEmitter.complete();
//                } finally {
//                    // 清理资源
//                    this.cleanup();
//                }
//            } catch (IOException e) {
//                sseEmitter.completeWithError(e);
//            }
//
//        });
//
//        // 设置超时回调
//        sseEmitter.onTimeout(() -> {
//            this.state = AgentState.ERROR;
//            this.cleanup();
//            log.warn("SSE connection timed out");
//        });
//
//        // 设置完成回调
//        sseEmitter.onCompletion(() -> {
//            if (this.state == AgentState.RUNNING) {
//                this.state = AgentState.FINISHED;
//            }
//            this.cleanup();
//            log.info("SSE connection completed");
//        });
//
//        return sseEmitter;
//    }

    /**
     * 清理步骤执行结果
     * @param stepResult 原始步骤结果
     * @return 清理后的结果
     */
    private String cleanStepResult(String stepResult) {
        if (StringUtils.isBlank(stepResult)) {
            return stepResult;
        }

        try {
            // 首先进行基本的文本清理
            String cleaned = ResponseCleanupUtil.cleanResponseText(stepResult);

            // 检查是否是工具执行结果
            if (cleaned.contains("工具") && cleaned.contains("完成了它的任务")) {
                return ResponseCleanupUtil.cleanToolResult(cleaned);
            }

            // 检查是否包含搜索结果
            if (cleaned.contains("searchWeb") || cleaned.contains("scrapeWeb")) {
                // 尝试提取并格式化搜索结果
                Pattern jsonPattern = Pattern.compile("结果:\\s*\"(.+)\"");
                Matcher matcher = jsonPattern.matcher(cleaned);

                if (matcher.find()) {
                    String jsonStr = matcher.group(1)
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\");

                    try {
                        // 尝试格式化为易读的搜索结果
                        String formattedResults = ResponseCleanupUtil.formatSearchResults(jsonStr);
                        return cleaned.replace(matcher.group(0), "搜索结果:\n\n" + formattedResults);
                    } catch (Exception e) {
                        // 格式化失败，使用清理后的JSON
                        String cleanedJson = ResponseCleanupUtil.cleanJsonData(jsonStr);
                        return cleaned.replace(matcher.group(0), "结果: " + cleanedJson);
                    }
                }
            }

            // 检查是否包含JSON数据
            if (cleaned.contains("{") || cleaned.contains("[")) {
                return ResponseCleanupUtil.cleanToolResult(cleaned);
            }

            return cleaned;

        } catch (Exception e) {
            log.warn("Failed to clean step result: " + e.getMessage());
            // 清理失败时，至少进行基本的文本清理
            return ResponseCleanupUtil.cleanResponseText(stepResult);
        }
    }

    /**
     * 执行单个步骤
     * @return 执行的结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 重置WebSearchTool的搜索次数计数器
        WebSearchTool.resetSearchCallCount();
        log.debug("清理资源：已重置WebSearchTool的搜索次数计数器");
        // 子类可以重写该方法来清理资源
    }
}
