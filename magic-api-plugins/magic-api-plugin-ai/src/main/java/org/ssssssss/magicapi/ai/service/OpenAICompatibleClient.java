package org.ssssssss.magicapi.ai.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.ssssssss.magicapi.ai.model.AIChatMessage;
import org.ssssssss.magicapi.ai.starter.MagicAIConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI 兼容 Chat Completions 客户端（DashScope / 通义千问及其他兼容服务）
 */
public class OpenAICompatibleClient {

	private final MagicAIConfig config;
	private final RestTemplate restTemplate;

	public OpenAICompatibleClient(MagicAIConfig config) {
		this.config = config;
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(config.getTimeout());
		factory.setReadTimeout(config.getTimeout());
		this.restTemplate = new RestTemplate(factory);
	}

	public String chat(String systemPrompt, List<AIChatMessage> messages) {
		if (!StringUtils.hasText(config.getApiKey())) {
			throw new IllegalStateException("magic-api.ai.api-key 未配置。请设置环境变量 MAGIC_API_AI_API_KEY 或在 application.yml 中配置 magic-api.ai.api-key（DashScope API Key）");
		}
		if (!StringUtils.hasText(config.getBaseUrl())) {
			throw new IllegalStateException("magic-api.ai.base-url 未配置");
		}

		String url = trimTrailingSlash(config.getBaseUrl()) + "/chat/completions";

		JSONObject body = new JSONObject();
		body.put("model", config.getModel());
		body.put("temperature", config.getTemperature());

		JSONArray msgs = new JSONArray();
		if (StringUtils.hasText(systemPrompt)) {
			JSONObject system = new JSONObject();
			system.put("role", "system");
			system.put("content", systemPrompt);
			msgs.add(system);
		}
		if (messages != null) {
			for (AIChatMessage message : messages) {
				if (message == null || !StringUtils.hasText(message.getContent())) {
					continue;
				}
				JSONObject item = new JSONObject();
				item.put("role", StringUtils.hasText(message.getRole()) ? message.getRole() : "user");
				item.put("content", message.getContent());
				msgs.add(item);
			}
		}
		body.put("messages", msgs);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(config.getApiKey().trim());

		try {
			ResponseEntity<String> response = restTemplate.exchange(
					url,
					HttpMethod.POST,
					new HttpEntity<>(body.toJSONString(), headers),
					String.class
			);
			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
				throw new IllegalStateException("LLM 调用失败: HTTP " + response.getStatusCodeValue());
			}
			return extractContent(response.getBody());
		} catch (RestClientException e) {
			throw new IllegalStateException("LLM 调用异常: " + e.getMessage(), e);
		}
	}

	private String extractContent(String responseBody) {
		JSONObject json = JSON.parseObject(responseBody);
		if (json == null) {
			throw new IllegalStateException("LLM 返回空响应");
		}
		if (json.containsKey("error")) {
			Object error = json.get("error");
			throw new IllegalStateException("LLM 返回错误: " + String.valueOf(error));
		}
		JSONArray choices = json.getJSONArray("choices");
		if (choices == null || choices.isEmpty()) {
			throw new IllegalStateException("LLM 未返回 choices: " + responseBody);
		}
		JSONObject message = choices.getJSONObject(0).getJSONObject("message");
		if (message == null) {
			throw new IllegalStateException("LLM 未返回 message: " + responseBody);
		}
		String content = message.getString("content");
		if (!StringUtils.hasText(content)) {
			throw new IllegalStateException("LLM 返回空 content");
		}
		return content;
	}

	private static String trimTrailingSlash(String url) {
		if (url == null) {
			return "";
		}
		String value = url.trim();
		while (value.endsWith("/")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}

	public List<AIChatMessage> copyMessages(List<AIChatMessage> source) {
		List<AIChatMessage> list = new ArrayList<>();
		if (source != null) {
			for (AIChatMessage item : source) {
				if (item != null) {
					list.add(new AIChatMessage(item.getRole(), item.getContent()));
				}
			}
		}
		return list;
	}
}
