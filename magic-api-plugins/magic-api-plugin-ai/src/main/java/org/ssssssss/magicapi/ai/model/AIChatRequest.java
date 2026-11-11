package org.ssssssss.magicapi.ai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 对话请求
 */
public class AIChatRequest {

	/**
	 * 用户本轮输入
	 */
	private String message;

	/**
	 * 历史消息（不含 system）
	 */
	private List<AIChatMessage> history = new ArrayList<>();

	/**
	 * 若更新已有接口，传入接口 id
	 */
	private String apiId;

	/**
	 * 目标分组 id；为空时使用/创建默认 AI 分组
	 */
	private String groupId;

	/**
	 * 是否在解析到接口定义后自动保存（默认 true）
	 */
	private boolean autoSave = true;

	/**
	 * 可选：附加上下文（当前打开的接口摘要等）
	 */
	private Map<String, Object> context;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<AIChatMessage> getHistory() {
		return history;
	}

	public void setHistory(List<AIChatMessage> history) {
		this.history = history;
	}

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public boolean isAutoSave() {
		return autoSave;
	}

	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public void setContext(Map<String, Object> context) {
		this.context = context;
	}
}
