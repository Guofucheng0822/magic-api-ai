package org.ssssssss.magicapi.ai.model;

/**
 * AI对话响应
 */
public class AiChatResponse {

    /**
     * AI回复的消息
     */
    private String message;

    /**
     * 是否包含代码
     */
    private boolean hasCode;

    public AiChatResponse() {
    }

    public AiChatResponse(String message) {
        this.message = message;
        this.hasCode = message != null && message.contains("```");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isHasCode() {
        return hasCode;
    }

    public void setHasCode(boolean hasCode) {
        this.hasCode = hasCode;
    }
}
