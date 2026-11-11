package org.ssssssss.magicapi.ai.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;
import org.ssssssss.magicapi.ai.model.*;
import org.ssssssss.magicapi.ai.starter.MagicAIConfig;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.config.WebSocketSessionManager;
import org.ssssssss.magicapi.core.logging.MagicLoggerContext;
import org.ssssssss.magicapi.core.model.*;
import org.ssssssss.magicapi.core.service.MagicAPIService;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.utils.ScriptManager;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptDebugContext;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 对话 → 解析接口定义 → 保存 / 测试 / 发布
 */
public class MagicAIService {

	private static final Pattern JSON_BLOCK = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

	private static final String DEFAULT_SYSTEM_PROMPT =
			"你是 Magic-API 接口生成助手。用户用自然语言描述 HTTP 接口需求，你需要：\n"
					+ "1) 用简洁中文说明你的设计思路；\n"
					+ "2) 输出一个 JSON 代码块，描述完整接口定义。\n"
					+ "JSON 字段：\n"
					+ "- name: 接口名称（简短中文或英文，可用于文件名，勿含特殊字符）\n"
					+ "- path: 接口路径，如 /hello 或 /user/{id}\n"
					+ "- method: GET/POST/PUT/DELETE\n"
					+ "- description: 接口说明\n"
					+ "- script: Magic-Script 脚本正文（不要包含 ```；可使用 db.select / db.selectOne / return 等）\n"
					+ "- parameters: 查询参数数组，元素含 name,value,description,required,dataType\n"
					+ "- headers: 请求头数组，结构同 parameters\n"
					+ "- paths: 路径变量数组，结构同 parameters\n"
					+ "- requestBody: 示例请求体字符串（可空）\n"
					+ "- responseBody: 示例响应体字符串（可空）\n"
					+ "Magic-Script 示例：\n"
					+ "return {code: 1, message: 'ok', data: 'hello'};\n"
					+ "或：var list = db.select('select id,name from user');\nreturn list;\n"
					+ "若用户只是闲聊或提问且无需生成接口，可不输出 JSON 代码块。";

	private final MagicAIConfig config;
	private final OpenAICompatibleClient client;

	public MagicAIService(MagicAIConfig config, OpenAICompatibleClient client) {
		this.config = config;
		this.client = client;
	}

	public AIChatResponse chat(AIChatRequest request) {
		if (request == null || !StringUtils.hasText(request.getMessage())) {
			throw new IllegalArgumentException("message 不能为空");
		}
		List<AIChatMessage> messages = client.copyMessages(request.getHistory());
		StringBuilder userContent = new StringBuilder(request.getMessage().trim());
		if (StringUtils.hasText(request.getApiId())) {
			MagicEntity existing = MagicConfiguration.getMagicResourceService().file(request.getApiId());
			if (existing instanceof ApiInfo) {
				ApiInfo api = (ApiInfo) existing;
				userContent.append("\n\n[当前接口上下文]\nid=").append(api.getId())
						.append(", name=").append(api.getName())
						.append(", method=").append(api.getMethod())
						.append(", path=").append(api.getPath())
						.append(", script=\n").append(Objects.toString(api.getScript(), ""));
			}
		}
		if (request.getContext() != null && !request.getContext().isEmpty()) {
			userContent.append("\n\n[附加上下文]\n").append(JSON.toJSONString(request.getContext()));
		}
		messages.add(new AIChatMessage("user", userContent.toString()));

		String systemPrompt = StringUtils.hasText(config.getSystemPrompt()) ? config.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT;
		String raw = client.chat(systemPrompt, messages);

		AIChatResponse response = new AIChatResponse();
		response.setRaw(raw);
		AIGeneratedApi definition = parseDefinition(raw);
		if (definition != null && StringUtils.hasText(definition.getScript()) && StringUtils.hasText(definition.getPath())) {
			response.setGenerated(true);
			response.setDefinition(definition);
			response.setReply(stripJsonBlock(raw));
			if (request.isAutoSave()) {
				ApiInfo saved = saveGenerated(definition, request.getApiId(), request.getGroupId());
				response.setApi(saved);
			}
		} else {
			response.setGenerated(false);
			response.setReply(raw);
		}
		return response;
	}

	public ApiInfo saveGenerated(AIGeneratedApi definition, String apiId, String groupId) {
		if (definition == null) {
			throw new IllegalArgumentException("接口定义为空");
		}
		notBlank(definition.getName(), "name");
		notBlank(definition.getPath(), "path");
		notBlank(definition.getScript(), "script");
		if (!StringUtils.hasText(definition.getMethod())) {
			definition.setMethod("GET");
		}

		MagicResourceService resourceService = MagicConfiguration.getMagicResourceService();
		ApiInfo apiInfo;
		if (StringUtils.hasText(apiId)) {
			MagicEntity entity = resourceService.file(apiId);
			if (!(entity instanceof ApiInfo)) {
				throw new IllegalArgumentException("apiId 对应资源不是接口: " + apiId);
			}
			apiInfo = (ApiInfo) entity;
		} else {
			apiInfo = new ApiInfo();
			apiInfo.setGroupId(resolveGroupId(groupId));
		}

		apiInfo.setName(sanitizeName(definition.getName()));
		apiInfo.setPath(normalizePath(definition.getPath()));
		apiInfo.setMethod(definition.getMethod().trim().toUpperCase(Locale.ROOT));
		apiInfo.setDescription(definition.getDescription());
		apiInfo.setScript(definition.getScript());
		apiInfo.setRequestBody(definition.getRequestBody());
		apiInfo.setResponseBody(definition.getResponseBody());
		apiInfo.setParameters(toParameters(definition.getParameters()));
		apiInfo.setHeaders(toHeaders(definition.getHeaders()));
		apiInfo.setPaths(toPaths(definition.getPaths()));

		boolean ok = resourceService.saveFile(apiInfo);
		if (!ok) {
			throw new IllegalStateException("保存接口失败");
		}
		MagicEntity saved = resourceService.file(apiInfo.getId());
		return saved instanceof ApiInfo ? (ApiInfo) saved : apiInfo;
	}

	/**
	 * 在线测试：优先使用编辑器 debug/websocket 机制（与定时任务手动执行一致），
	 * 否则回退到普通脚本执行或 MagicAPIService.call。
	 */
	public Object testApi(AITestRequest request, MagicHttpServletRequest httpRequest,
						  MagicConfiguration configuration, MagicAPIService magicAPIService) {
		if (request == null || !StringUtils.hasText(request.getApiId())) {
			throw new IllegalArgumentException("apiId 不能为空");
		}
		MagicEntity entity = MagicConfiguration.getMagicResourceService().file(request.getApiId());
		if (!(entity instanceof ApiInfo)) {
			throw new IllegalArgumentException("接口不存在: " + request.getApiId());
		}
		ApiInfo apiInfo = (ApiInfo) entity;
		Map<String, Object> ctx = request.getContext() == null
				? Collections.<String, Object>emptyMap()
				: request.getContext();

		DebugRequest debugRequest = DebugRequest.create(httpRequest);
		String clientId = debugRequest.getRequestedClientId();
		String scriptId = debugRequest.getRequestedScriptId();
		if (StringUtils.hasText(clientId) && StringUtils.hasText(scriptId)) {
			MagicLoggerContext.SESSION.set(clientId);
			String sessionAndScriptId = clientId + scriptId;
			try {
				MagicScriptDebugContext debugContext = debugRequest.createMagicScriptContext(configuration.getDebugTimeout());
				WebSocketSessionManager.addMagicScriptContext(sessionAndScriptId, debugContext);
				debugContext.setScriptName(MagicConfiguration.getMagicResourceService().getScriptName(apiInfo));
				for (Map.Entry<String, Object> entry : ctx.entrySet()) {
					debugContext.set(entry.getKey(), entry.getValue());
				}
				return ScriptManager.executeScript(apiInfo.getScript(), debugContext);
			} finally {
				WebSocketSessionManager.removeMagicScriptContext(sessionAndScriptId);
				MagicLoggerContext.SESSION.remove();
			}
		}

		try {
			MagicScriptContext context = new MagicScriptContext();
			for (Map.Entry<String, Object> entry : ctx.entrySet()) {
				context.set(entry.getKey(), entry.getValue());
			}
			return ScriptManager.executeScript(apiInfo.getScript(), context);
		} catch (RuntimeException ex) {
			if (magicAPIService != null) {
				return magicAPIService.call(apiInfo.getMethod(), buildFullPath(apiInfo), ctx);
			}
			throw ex;
		}
	}

	public boolean publish(String apiId) {
		if (!StringUtils.hasText(apiId)) {
			throw new IllegalArgumentException("apiId 不能为空");
		}
		MagicResourceService resourceService = MagicConfiguration.getMagicResourceService();
		MagicEntity entity = resourceService.file(apiId);
		if (entity == null) {
			throw new IllegalArgumentException("接口不存在: " + apiId);
		}
		// 平台「锁定」即发布后不可编辑，作为一键发布机制
		return resourceService.lock(apiId);
	}

	public Map<String, Object> status() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("enable", config.isEnable());
		map.put("baseUrl", config.getBaseUrl());
		map.put("model", config.getModel());
		map.put("apiKeyConfigured", StringUtils.hasText(config.getApiKey()));
		map.put("defaultGroupName", config.getDefaultGroupName());
		map.put("providerHint", "DashScope OpenAI-compatible (Qwen); override base-url/model for other providers");
		return map;
	}

	private String buildFullPath(ApiInfo apiInfo) {
		String groupPath = MagicConfiguration.getMagicResourceService().getGroupPath(apiInfo.getGroupId());
		String path = Objects.toString(groupPath, "") + "/" + Objects.toString(apiInfo.getPath(), "");
		return path.replaceAll("/+", "/");
	}

	private String resolveGroupId(String groupId) {
		MagicResourceService resourceService = MagicConfiguration.getMagicResourceService();
		if (StringUtils.hasText(groupId)) {
			Group group = resourceService.getGroup(groupId);
			if (group == null) {
				throw new IllegalArgumentException("分组不存在: " + groupId);
			}
			return groupId;
		}
		TreeNode<Group> tree = resourceService.tree("api");
		if (tree != null) {
			String found = findGroupByName(tree, config.getDefaultGroupName());
			if (found != null) {
				return found;
			}
		}
		Group group = new Group();
		group.setName(config.getDefaultGroupName());
		group.setType("api");
		group.setParentId(Constants.ROOT_ID);
		group.setPath("/ai");
		boolean saved = resourceService.saveGroup(group);
		if (!saved || !StringUtils.hasText(group.getId())) {
			throw new IllegalStateException("创建默认分组失败: " + config.getDefaultGroupName());
		}
		return group.getId();
	}

	private String findGroupByName(TreeNode<Group> node, String name) {
		if (node == null) {
			return null;
		}
		Group group = node.getNode();
		if (group != null && name.equals(group.getName()) && StringUtils.hasText(group.getId())
				&& !Constants.ROOT_ID.equals(group.getId()) && !group.getId().contains(":")) {
			return group.getId();
		}
		if (node.getChildren() != null) {
			for (TreeNode<Group> child : node.getChildren()) {
				String found = findGroupByName(child, name);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	private AIGeneratedApi parseDefinition(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		List<String> candidates = new ArrayList<>();
		Matcher matcher = JSON_BLOCK.matcher(raw);
		while (matcher.find()) {
			candidates.add(matcher.group(1).trim());
		}
		String trimmed = raw.trim();
		if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
			candidates.add(trimmed);
		}
		for (String candidate : candidates) {
			try {
				JSONObject obj = JSON.parseObject(candidate);
				if (obj == null) {
					continue;
				}
				if (!obj.containsKey("script") && !obj.containsKey("path")) {
					continue;
				}
				return obj.toJavaObject(AIGeneratedApi.class);
			} catch (Exception ignore) {
				// try next
			}
		}
		return null;
	}

	private String stripJsonBlock(String raw) {
		if (!StringUtils.hasText(raw)) {
			return raw;
		}
		String text = JSON_BLOCK.matcher(raw).replaceAll("").trim();
		return StringUtils.hasText(text) ? text : "已生成接口定义，可在编辑器中查看并测试。";
	}

	private List<Parameter> toParameters(List<Map<String, Object>> list) {
		List<Parameter> result = new ArrayList<>();
		if (list == null) {
			return result;
		}
		for (Map<String, Object> item : list) {
			Parameter parameter = new Parameter();
			fillDefinition(parameter, item);
			result.add(parameter);
		}
		return result;
	}

	private List<Header> toHeaders(List<Map<String, Object>> list) {
		List<Header> result = new ArrayList<>();
		if (list == null) {
			return result;
		}
		for (Map<String, Object> item : list) {
			Header header = new Header();
			fillDefinition(header, item);
			result.add(header);
		}
		return result;
	}

	private List<Path> toPaths(List<Map<String, Object>> list) {
		List<Path> result = new ArrayList<>();
		if (list == null) {
			return result;
		}
		for (Map<String, Object> item : list) {
			Path path = new Path();
			fillDefinition(path, item);
			result.add(path);
		}
		return result;
	}

	private void fillDefinition(BaseDefinition definition, Map<String, Object> item) {
		if (item == null) {
			return;
		}
		Object name = item.get("name");
		if (name != null) {
			definition.setName(String.valueOf(name));
		}
		if (item.containsKey("value")) {
			definition.setValue(item.get("value"));
		}
		Object description = item.get("description");
		if (description != null) {
			definition.setDescription(String.valueOf(description));
		}
		Object required = item.get("required");
		if (required instanceof Boolean) {
			definition.setRequired((Boolean) required);
		} else if (required != null) {
			definition.setRequired(Boolean.parseBoolean(String.valueOf(required)));
		}
		Object dataType = item.get("dataType");
		if (dataType != null) {
			try {
				definition.setDataType(DataType.valueOf(String.valueOf(dataType)));
			} catch (Exception ignore) {
				// keep default
			}
		}
	}

	private static String sanitizeName(String name) {
		String value = name.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
		return value.length() > 64 ? value.substring(0, 64) : value;
	}

	private static String normalizePath(String path) {
		String value = path.trim();
		if (!value.startsWith("/")) {
			value = "/" + value;
		}
		return value.replaceAll("/+", "/");
	}

	private static void notBlank(String value, String field) {
		if (!StringUtils.hasText(value)) {
			throw new IllegalArgumentException(field + " 不能为空");
		}
	}
}
