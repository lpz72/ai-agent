package org.lpz.aiagent.controller;

import io.reactivex.Emitter;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.lpz.aiagent.agent.YuManus;
import org.lpz.aiagent.app.LoveApp;
import org.lpz.aiagent.common.BaseResponse;
import org.lpz.aiagent.common.ErrorCode;
import org.lpz.aiagent.common.ResultUtils;
import org.lpz.aiagent.exception.BusinessException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.awt.*;
import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步调用AI恋爱大师应用
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message,String chatId) {
        return loveApp.doChat(message,chatId);
    }


    /**
     * SSE 流式调用AI恋爱大师应用
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    @GetMapping(value = "/love_app/chat/sse",produces = MediaType.APPLICATION_JSON_VALUE)
//    public BaseResponse<Flux<String>> doChatWithLoveAppSse(String message, String chatId) {
//
//        if (StringUtils.isAnyBlank(message,chatId)) {
//            throw new BusinessException(ErrorCode.NULL_ERROR);
//        }
//
//        return ResultUtils.success(loveApp.doChatByStream(message,chatId));
//    }

    public Flux<String> doChatWithLoveAppSse(String message, String chatId) {

        if (StringUtils.isAnyBlank(message,chatId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        return loveApp.doChatByStream(message,chatId);
    }

    /**
     * SSE 流式调用AI恋爱大师应用
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        if (StringUtils.isAnyBlank(message,chatId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        return loveApp.doChatByStream(message, chatId)
                // 转换每个片段的类型
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE 流式调用AI减肥大师应用
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse_emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {

        if (StringUtils.isAnyBlank(message,chatId)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //创建一个超时时间较长的SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); //3分钟超时

        // 获取Flux 数据流并直接订阅
        loveApp.doChatByStream(message,chatId)
                .subscribe(
                    // 处理每条信息
                    chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                },
                // 处理错误
                sseEmitter::completeWithError,
                // 处理完成
                sseEmitter::complete);


        // 返回sseEmitter
        return sseEmitter;
    }

    /**
     * 流式调用 manus 超级智能体
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        YuManus manus = new YuManus(allTools, dashscopeChatModel);
        if (StringUtils.isBlank(message)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return manus.runStream(message);

    }
//
//    public BaseResponse<SseEmitter> doChatWithManus(String message) {
//        YuManus manus = new YuManus(allTools, dashscopeChatModel);
//        if (StringUtils.isBlank(message)) {
//            throw new BusinessException(ErrorCode.NULL_ERROR);
//        }
//        return ResultUtils.success(manus.runStream(message));
//
//    }

    
}
