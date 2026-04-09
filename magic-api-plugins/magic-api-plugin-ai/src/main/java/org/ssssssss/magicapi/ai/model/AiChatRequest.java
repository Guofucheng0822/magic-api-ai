package org.ssssssss.magicapi.ai.model;

import java.util.List;

/**
 * AI对话请求
 */
public class AiChatRequest {

    /**
     * 用户输入的消息
     */
    private String message;

    /**
     * 历史对话记录
     */
    private List<AiChatMessage> history;

    /**
     * 当前编辑器中的代码（作为上下文）
     */
    private String currentCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AiChatMessage> getHistory() {
        return history;
    }

    public void setHistory(List<AiChatMessage> history) {
        this.history = history;
    }

    public String getCurrentCode() {
        return currentCode;
    }

    public void setCurrentCode(String currentCode) {
        this.currentCode = currentCode;
    }
}
