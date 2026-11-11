package org.ssssssss.magicapi.ai.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 在线测试请求
 */
public class AITestRequest {

	private String apiId;

	/**
	 * 测试上下文变量（query/body 参数等），会注入脚本执行上下文
	 */
	private Map<String, Object> context = new HashMap<>();

	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public void setContext(Map<String, Object> context) {
		this.context = context;
	}
}
