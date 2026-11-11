package org.ssssssss.magicapi.ai.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 对话插件配置。默认对接阿里云百炼（DashScope）千问的 OpenAI 兼容接口，
 * 也可通过 base-url / model / api-key 切换到任意 OpenAI 兼容服务。
 */
@ConfigurationProperties("magic-api.ai")
public class MagicAIConfig {

	/**
	 * 是否启用 AI 插件
	 */
	private boolean enable = true;

	/**
	 * OpenAI 兼容接口 Base URL（不含 /chat/completions）
	 * 默认：DashScope 兼容模式
	 */
	private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

	/**
	 * API Key，建议通过环境变量 MAGIC_API_AI_API_KEY 或配置中心注入，勿硬编码
	 */
	private String apiKey = "";

	/**
	 * 模型名称，默认 qwen-plus；也可用 qwen-turbo / qwen-max 等
	 */
	private String model = "qwen-plus";

	/**
	 * 采样温度
	 */
	private double temperature = 0.2;

	/**
	 * HTTP 超时（毫秒）
	 */
	private int timeout = 60000;

	/**
	 * 新建接口时默认分组名称（若不存在则自动创建）
	 */
	private String defaultGroupName = "AI生成";

	/**
	 * 可选：覆盖内置系统提示词
	 */
	private String systemPrompt;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getDefaultGroupName() {
		return defaultGroupName;
	}

	public void setDefaultGroupName(String defaultGroupName) {
		this.defaultGroupName = defaultGroupName;
	}

	public String getSystemPrompt() {
		return systemPrompt;
	}

	public void setSystemPrompt(String systemPrompt) {
		this.systemPrompt = systemPrompt;
	}
}
