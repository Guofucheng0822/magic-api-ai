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

    /**
     * 指定使用的提供商（由前端传入）
     */
    private String provider;

	/**
	 * 指定使用的模型（由前端传入）
	 */
	private String model;

	/**
	 * 用户在数据源树点中的表名
	 */
	private String focusTable;

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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

	public void setModel(String model) {
		this.model = model;
	}

	public String getFocusTable() {
		return focusTable;
	}

	public void setFocusTable(String focusTable) {
		this.focusTable = focusTable;
	}
}
