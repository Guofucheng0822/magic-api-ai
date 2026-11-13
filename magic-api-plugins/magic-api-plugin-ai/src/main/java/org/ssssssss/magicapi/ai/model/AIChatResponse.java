package org.ssssssss.magicapi.ai.model;

import org.ssssssss.magicapi.core.model.ApiInfo;

import java.util.List;

/**
 * AI 对话响应
 */
public class AIChatResponse {

	/**
	 * 助手自然语言回复
	 */
	private String reply;

	/**
	 * 是否成功解析出接口定义
	 */
	private boolean generated;

	/**
	 * 解析出的接口定义（保存前）
	 */
	private AIGeneratedApi definition;

	/**
	 * 保存后的接口信息（autoSave=true 且保存成功时）
	 */
	private ApiInfo api;

	/**
	 * 原始 LLM 文本（便于排查）
	 */
	private String raw;

	/**
	 * 目标接口已锁定，本次未写入
	 */
	private boolean locked;

	/**
	 * 待用户确认的控制台操作，例如创建分组
	 */
	private List<AIConsoleAction> actions;

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public boolean isGenerated() {
		return generated;
	}

	public void setGenerated(boolean generated) {
		this.generated = generated;
	}

	public AIGeneratedApi getDefinition() {
		return definition;
	}

	public void setDefinition(AIGeneratedApi definition) {
		this.definition = definition;
	}

	public ApiInfo getApi() {
		return api;
	}

	public void setApi(ApiInfo api) {
		this.api = api;
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public List<AIConsoleAction> getActions() {
		return actions;
	}

	public void setActions(List<AIConsoleAction> actions) {
		this.actions = actions;
	}
}
