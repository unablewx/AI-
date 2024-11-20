package com.manager;

import com.common.ErrorCode;
import com.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class AiManager {
    @Resource
    private ClientV4 clientV4;
    //稳定的随机数
    private static final float STABLE_TEMPERATURE = 0.05f;
    //不稳定的随机数
    private static final float UNSTABLE_TEMPERATURE = 0.99f;

    /**
     * 封装稳定同步通用请求
     *
     * @param systemMessage 系统携带消息
     * @param userMessage   用户携带消息
     * @return AI生成内容
     */
    public String doSyncStableRequest(String systemMessage, String userMessage) {
        return doRequest(systemMessage, userMessage, STABLE_TEMPERATURE, Constants.invokeMethod);
    }

    /**
     * 封装不稳定同步通用请求
     *
     * @param systemMessage 系统携带消息
     * @param userMessage   用户携带消息
     * @return AI生成内容
     */
    public String doSyncUnstableRequest(String systemMessage, String userMessage) {
        return doRequest(systemMessage, userMessage, UNSTABLE_TEMPERATURE, Constants.invokeMethod);
    }


    /**
     * 封装通用请求
     *
     * @param systemMessage 系统携带消息
     * @param userMessage   用户携带消息
     * @param temperature   偏差值
     * @param invokeMethod  访问方法(async-invoke,sse-invoke,invoke)
     * @return AI生成内容
     */
    public String doRequest(String systemMessage, String userMessage, Float temperature
            , String invokeMethod) {
        List<ChatMessage> messageList = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        messageList.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messageList.add(userChatMessage);
        return doRequest(messageList, temperature, invokeMethod);
    }


    /**
     * 通用请求
     *
     * @param messageList  消息列表
     * @param temperature  偏差值
     * @param invokeMethod 访问方法(async-invoke,sse-invoke,invoke)
     * @return AI生成内容
     */
    public String doRequest(List<ChatMessage> messageList, Float temperature
            , String invokeMethod) {
        ChatCompletionRequest build = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .temperature(temperature)
                .invokeMethod(invokeMethod)
                .messages(messageList)
                .build();
        ModelApiResponse modelApiResponse = null;
        try {
            modelApiResponse = clientV4.invokeModelApi(build);
            return modelApiResponse.getData().getChoices().get(0).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 封装稳定流式同步通用请求
     *
     * @param systemMessage 系统携带消息
     * @param userMessage   用户携带消息
     * @return AI生成内容
     */
    public Flowable<ModelData> doStreamSyncStableRequest(String systemMessage, String userMessage) {
        return doStreamRequest(systemMessage, userMessage, STABLE_TEMPERATURE, Constants.invokeMethod);
    }

    /**
     * 封装不稳定流式同步通用请求
     *
     * @param systemMessage 系统携带消息
     * @param userMessage   用户携带消息
     * @return AI生成内容
     */
    public Flowable<ModelData> doStreamSyncUnstableRequest(String systemMessage, String userMessage) {
        return doStreamRequest(systemMessage, userMessage, UNSTABLE_TEMPERATURE, Constants.invokeMethod);
    }


    /**
     * 封装通用请求
     *
     * @param systemMessage 系统携带消息
     * @param userMessage   用户携带消息
     * @param temperature   偏差值
     * @param invokeMethod  访问方法(async-invoke,sse-invoke,invoke)
     * @return AI生成内容
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage, String userMessage, Float temperature
            , String invokeMethod) {
        List<ChatMessage> messageList = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        messageList.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messageList.add(userChatMessage);
        return doStreamRequest(messageList, temperature, invokeMethod);
    }

    /**
     * 通用流式请求
     *
     * @param messageList  消息列表
     * @param temperature  偏差值
     * @param invokeMethod 访问方法(async-invoke,sse-invoke,invoke)
     * @return AI生成内容
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messageList, Float temperature
            , String invokeMethod) {
        ChatCompletionRequest build = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .temperature(temperature)
                .invokeMethod(invokeMethod)
                .messages(messageList)
                .build();
        ModelApiResponse modelApiResponse = null;
        try {
            modelApiResponse = clientV4.invokeModelApi(build);
            return modelApiResponse.getFlowable();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
