package org.ssssssss.magicapi.ai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LLM 生成的接口定义（中间结构）
 */
public class AIGeneratedApi {

	private String name;

	private String path;

	private String method = "GET";

	private String description;

	private String script;

	private String requestBody;

	private String responseBody;

	private List<Map<String, Object>> parameters = new ArrayList<>();

	private List<Map<String, Object>> headers = new ArrayList<>();

	private List<Map<String, Object>> paths = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public List<Map<String, Object>> getParameters() {
		return parameters;
	}

	public void setParameters(List<Map<String, Object>> parameters) {
		this.parameters = parameters;
	}

	public List<Map<String, Object>> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Map<String, Object>> headers) {
		this.headers = headers;
	}

	public List<Map<String, Object>> getPaths() {
		return paths;
	}

	public void setPaths(List<Map<String, Object>> paths) {
		this.paths = paths;
	}
}
