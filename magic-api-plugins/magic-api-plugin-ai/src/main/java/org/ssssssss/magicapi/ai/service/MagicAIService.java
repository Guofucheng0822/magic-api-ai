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
					+ "系统提示词末尾如果有「当前可查询的数据源与表」，那就是本进程实时读到的库表。\n"
					+ "用户询问有哪些表、能查什么数据时，只根据该清单用中文列出表名和注释，不要说无法访问数据库，不要让用户自己执行 SHOW TABLES。\n"
					+ "生成 SQL 时只能使用清单里的表和字段。db.select 只连接主库 MySQL。ClickHouse 必须写 db.clickhouse.select，factory 写 db.factory.select，其他数据源用清单里的脚本。不要把 ClickHouse 的表写进 db.select。ClickHouse 表名区分大小写。条件参数用 #{name}。\n"
					+ "Redis 没有表，不能写 SQL。查询写 redis.scan(pattern)、redis.get(key)、redis.hget(key, field)、redis.hgetall(key)、redis.lrange(key, 0, 20)、redis.smembers(key)、redis.zrange(key, 0, 20)、redis.type(key)、redis.ttl(key)、redis.exists(key)。只根据清单里的键样例，不要编造键名，不要写 set、del。\n"
					+ "Magic-Script 不是 JavaScript。var 是声明关键字，不能当变量名，禁止 for (var i = 0; i < n; i++)。\n"
					+ "循环只能写 for (item in list) 或 for (index, item in list)。不要用 let、const、===、toFixed、.length，集合用 size()。\n"
					+ "同一段脚本不要重复声明同名变量。小数格式用 new java.text.DecimalFormat('0.00').format(数值)。\n"
					+ "SQL 条件用 #{name}，多值用 <foreach>，不要把参数拼进 SQL 字符串。\n"
					+ "修改或生成接口时，必须输出一个 json 代码块，script 是改完后的完整脚本，不能只写改动说明。用户说确认、保存、是的、同意时，立刻输出这个 JSON，不要再问要不要保存。\n"
					+ "新建接口必须使用新的 name 和 path，不要复用当前接口。只有用户明确说修改当前接口、这个接口时，才覆盖当前接口。\n"
					+ "如果用户贴了在线测试报错，根据报错修改当前接口并输出完整 JSON。\n"
					+ "用户明确要求创建分组、重命名、移动、复制或删除分组/接口/函数时，输出操作 JSON，不要改成操作步骤。\n"
					+ "操作 JSON 格式：{\"actions\":[{\"op\":\"createGroup\",\"type\":\"api\",\"name\":\"报表\",\"path\":\"/report\",\"parentId\":\"0\"}]}\n"
					+ "op 只能是 createGroup、updateGroup、move、copyGroup、delete。type 只能是 api、function、datasource、task。\n"
					+ "parentId 为 0 表示根节点，也可以写 parentName。updateGroup 用 id 或 name 定位，新名字写 newName，新路径写 path。\n"
					+ "move、copyGroup 用 id 或 name 定位，目标用 targetId 或 targetName，目标为根节点时 targetId 写 0。delete 会真的删除，对象必须和用户说的一致。\n"
					+ "如果同时还要在新建分组里生成接口，把接口定义放在 api 字段，不要和 actions 平铺。api.groupName 写分组名称。\n"
					+ "只是询问怎么点菜单、没有明确要求执行时，用中文说明，不要输出 actions。\n"
					+ "分组和文件名称、id 只能使用提示词里的「当前分组与资源」。操作还没执行，不要说已经创建成功。\n"
					+ "若用户只是闲聊、提问或查看表清单，且没有要求生成接口或操作资源，不要输出 JSON 代码块。";

	private final MagicAIConfig config;
	private final OpenAICompatibleClient client;
	private final DatabaseSchemaCatalog schemaCatalog;

	public MagicAIService(MagicAIConfig config, OpenAICompatibleClient client, DatabaseSchemaCatalog schemaCatalog) {
		this.config = config;
		this.client = client;
		this.schemaCatalog = schemaCatalog;
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
		appendDatasourceHint(userContent, request.getMessage());
		messages.add(new AIChatMessage("user", userContent.toString()));

		String systemPrompt = StringUtils.hasText(config.getSystemPrompt()) ? config.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT;
		if (schemaCatalog != null) {
			String schema = schemaCatalog.describe();
			if (StringUtils.hasText(schema)) {
				systemPrompt = systemPrompt + "\n\n" + schema;
			}
		}
		String resources = describeResources();
		if (StringUtils.hasText(resources)) {
			systemPrompt = systemPrompt + "\n\n" + resources;
		}
		String raw = client.chat(systemPrompt, messages);
		JSONObject payload = firstPayload(raw);
		List<AIConsoleAction> actions = parseActions(payload);
		AIGeneratedApi definition = definitionFrom(payload, raw, actions);
		if (expectsScript(request) && !scriptReady(definition, request)) {
			messages.add(new AIChatMessage("assistant", raw));
			messages.add(new AIChatMessage("user", retryScriptPrompt(request)));
			raw = client.chat(systemPrompt, messages);
			payload = firstPayload(raw);
			actions = parseActions(payload);
			definition = definitionFrom(payload, raw, actions);
		}
		completeDefinition(definition, saveApiId(request, definition));

		AIChatResponse response = new AIChatResponse();
		response.setRaw(raw);
		if (!actions.isEmpty()) {
			response.setActions(actions);
			String reply = stripJsonBlock(raw);
			String summary = summarizeActions(actions);
			if (!StringUtils.hasText(reply) || "已生成接口定义，可在编辑器中查看并测试。".equals(reply)) {
				reply = summary;
			} else {
				reply = reply + "\n\n" + summary;
			}
			response.setReply(reply);
			if (definition != null && StringUtils.hasText(definition.getScript()) && StringUtils.hasText(definition.getPath())) {
				response.setGenerated(true);
				response.setDefinition(definition);
			}
			return response;
		}
		if (definition != null && StringUtils.hasText(definition.getScript()) && StringUtils.hasText(definition.getPath())) {
			if (expectsScript(request) && !scriptReady(definition, request)) {
				response.setGenerated(false);
				String reply = stripJsonBlock(raw);
				response.setReply((StringUtils.hasText(reply) ? reply + "\n\n" : "") + "脚本还不是目标数据源，这次没有保存。");
				return response;
			}
			response.setGenerated(true);
			response.setDefinition(definition);
			response.setReply(stripJsonBlock(raw));
			if (request.isAutoSave()) {
				String targetId = saveApiId(request, definition);
				if (isLocked(targetId)) {
					response.setLocked(true);
					String reply = response.getReply() == null ? "" : response.getReply();
					response.setReply(reply + (reply.isEmpty() ? "" : "\n\n")
							+ "当前接口已锁定，这次没有写入。点「同意修改」会先解锁再保存。");
				} else {
					ApiInfo saved = saveGenerated(definition, targetId, request.getGroupId(), false);
					response.setApi(saved);
				}
			}
		} else {
			response.setGenerated(false);
			response.setReply(raw);
		}
		return response;
	}

	private boolean expectsScript(AIChatRequest request) {
		String message = request.getMessage();
		if (!StringUtils.hasText(message)) {
			return false;
		}
		String text = message.toLowerCase(Locale.ROOT);
		if (text.contains("有哪些表") || text.contains("有什么表") || text.contains("能查什么") || text.contains("哪些数据源")) {
			return false;
		}
		return text.contains("保存") || text.contains("确认") || text.contains("是的") || text.contains("同意")
				|| text.contains("改") || text.contains("生成") || text.contains("查询") || text.contains("接口")
				|| text.contains("clickhouse") || text.contains("ck") || text.contains("redis");
	}

	private boolean scriptReady(AIGeneratedApi definition, AIChatRequest request) {
		if (definition == null || !StringUtils.hasText(definition.getScript())) {
			return false;
		}
		String script = definition.getScript().toLowerCase(Locale.ROOT);
		String text = conversationText(request);
		if (mentionsClickHouse(text)) {
			return script.contains("db.clickhouse.");
		}
		if (text.contains("redis")) {
			return script.contains("redis.");
		}
		return true;
	}

	private String retryScriptPrompt(AIChatRequest request) {
		StringBuilder prompt = new StringBuilder("上一轮只有说明，没有可保存的脚本。现在只输出一个 json 代码块，必须包含 name、path、method、script。script 写完整 Magic-Script，禁止只写改动点，禁止再问要不要保存。");
		String text = conversationText(request);
		if (mentionsClickHouse(text)) {
			prompt.append("这次查 ClickHouse：script 里必须写 db.clickhouse.select，不要写 db.select。");
		}
		if (text.contains("operate")) {
			prompt.append("表名用 operate，条件用 dnum = #{dnum}，按 time 倒序。");
		}
		return prompt.toString();
	}

	private String conversationText(AIChatRequest request) {
		StringBuilder text = new StringBuilder();
		if (request.getHistory() != null) {
			for (AIChatMessage message : request.getHistory()) {
				if (message != null && message.getContent() != null) {
					text.append(message.getContent()).append('\n');
				}
			}
		}
		if (request.getMessage() != null) {
			text.append(request.getMessage());
		}
		return text.toString().toLowerCase(Locale.ROOT);
	}

	private boolean mentionsClickHouse(String text) {
		return text.contains("clickhouse") || text.contains("click house")
				|| text.contains("ck的") || text.contains("查ck") || text.contains("ck查询")
				|| text.contains("ck库") || text.contains("ck表") || text.contains("ck数据");
	}

	private String saveApiId(AIChatRequest request, AIGeneratedApi definition) {
		if (request == null || !StringUtils.hasText(request.getApiId()) || definition == null) {
			return null;
		}
		MagicEntity entity = MagicConfiguration.getMagicResourceService().file(request.getApiId());
		if (!(entity instanceof ApiInfo)) {
			return null;
		}
		ApiInfo current = (ApiInfo) entity;
		String message = request.getMessage() == null ? "" : request.getMessage().trim();
		if (explicitCurrentEdit(message)) {
			return current.getId();
		}
		boolean samePath = StringUtils.hasText(definition.getPath())
				&& normalizePath(definition.getPath()).equals(normalizePath(current.getPath()));
		boolean sameName = StringUtils.hasText(definition.getName())
				&& sanitizeName(definition.getName()).equals(current.getName());
		if (shortConfirm(message) && (samePath || sameName || !StringUtils.hasText(definition.getPath()))) {
			return current.getId();
		}
		if (samePath && sameName) {
			return current.getId();
		}
		return null;
	}

	private boolean explicitCurrentEdit(String message) {
		return message.contains("当前接口") || message.contains("这个接口") || message.contains("改这个")
				|| message.contains("修改当前") || message.contains("把当前") || message.contains("沿用当前");
	}

	private boolean shortConfirm(String message) {
		return message.matches("^(保存|确认|是的|同意|好的|可以)[。.!！\\s]*$");
	}

	private void completeDefinition(AIGeneratedApi definition, String apiId) {
		if (definition == null || !StringUtils.hasText(definition.getScript()) || !StringUtils.hasText(apiId)) {
			return;
		}
		MagicEntity entity = MagicConfiguration.getMagicResourceService().file(apiId);
		if (!(entity instanceof ApiInfo)) {
			return;
		}
		ApiInfo api = (ApiInfo) entity;
		if (!StringUtils.hasText(definition.getName())) {
			definition.setName(api.getName());
		}
		if (!StringUtils.hasText(definition.getPath())) {
			definition.setPath(api.getPath());
		}
		if (!StringUtils.hasText(definition.getMethod())) {
			definition.setMethod(api.getMethod());
		}
	}

	private void appendDatasourceHint(StringBuilder userContent, String message) {
		if (!StringUtils.hasText(message)) {
			return;
		}
		String text = message.toLowerCase(Locale.ROOT);
		boolean clickHouse = text.contains("clickhouse") || text.contains("click house")
				|| text.contains("ck的") || text.contains("查ck") || text.contains("ck查询")
				|| text.contains("ck库") || text.contains("ck表") || text.contains("ck数据");
		if (clickHouse) {
			userContent.append("\n\n[数据源要求]\n这次查询 ClickHouse。脚本必须是 db.clickhouse.select 或 db.clickhouse.selectOne，禁止使用 db.select。")
					.append("只使用提示词里 ClickHouse 那一节的表和字段，表名保持原样。");
		}
		if (text.contains("factory") || text.contains("idmanager")) {
			userContent.append("\n\n[数据源要求]\n这次查询 factory 库。脚本必须是 db.factory.select，禁止使用 db.select。");
		}
		if (text.contains("redis")) {
			userContent.append("\n\n[数据源要求]\n这次查询 Redis，不是 SQL。使用 redis.scan、redis.get、redis.hget、redis.hgetall、redis.lrange、redis.smembers、redis.zrange、redis.type、redis.ttl、redis.exists。禁止 db.select，禁止 set 和 del。键名只用提示词里的键样例。");
		}
	}

	public ApiInfo saveGenerated(AIGeneratedApi definition, String apiId, String groupId) {
		return saveGenerated(definition, apiId, groupId, false);
	}

	public ApiInfo saveGenerated(AIGeneratedApi definition, String apiId, String groupId, boolean unlockIfLocked) {
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
			if (entity instanceof ApiInfo) {
				apiInfo = (ApiInfo) entity;
				if (Constants.LOCK.equals(apiInfo.getLock())) {
					if (!unlockIfLocked) {
						throw new IllegalStateException("当前资源已被锁定，请解锁后再操作。");
					}
					resourceService.unlock(apiInfo.getId());
					apiInfo.setLock(Constants.UNLOCK);
				}
			} else if (entity == null) {
				// 编辑器里还留着 id，但文件已被删掉。按原 id 重建，避免同意修改时报「不是接口」。
				apiInfo = new ApiInfo();
				apiInfo.setId(apiId);
				apiInfo.setGroupId(resolveGroupId(groupId));
			} else {
				throw new IllegalArgumentException("apiId 对应资源不是接口: " + apiId);
			}
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
		if (definition.getParameters() != null && !definition.getParameters().isEmpty()) {
			apiInfo.setParameters(toParameters(definition.getParameters()));
		}
		if (definition.getHeaders() != null && !definition.getHeaders().isEmpty()) {
			apiInfo.setHeaders(toHeaders(definition.getHeaders()));
		}
		if (definition.getPaths() != null && !definition.getPaths().isEmpty()) {
			apiInfo.setPaths(toPaths(definition.getPaths()));
		}

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
		Map<String, Object> ctx = new HashMap<>();
		if (request.getContext() != null) {
			ctx.putAll(request.getContext());
		}
		if (apiInfo.getParameters() != null) {
			for (Parameter parameter : apiInfo.getParameters()) {
				if (parameter == null || !StringUtils.hasText(parameter.getName()) || ctx.containsKey(parameter.getName())) {
					continue;
				}
				if (parameter.getValue() != null && StringUtils.hasText(String.valueOf(parameter.getValue()))) {
					ctx.put(parameter.getName(), parameter.getValue());
				}
			}
		}
		if (!ctx.containsKey("body") && StringUtils.hasText(apiInfo.getRequestBody())) {
			String rawBody = apiInfo.getRequestBody().trim();
			if (rawBody.startsWith("{") || rawBody.startsWith("[")) {
				try {
					ctx.put("body", JSON.parse(rawBody));
				} catch (RuntimeException ignored) {
					// 示例请求体不是 JSON 时，测试仍按调用方传入的上下文执行
				}
			}
		}

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

	private boolean isLocked(String apiId) {
		if (!StringUtils.hasText(apiId)) {
			return false;
		}
		MagicEntity entity = MagicConfiguration.getMagicResourceService().file(apiId);
		return entity != null && Constants.LOCK.equals(entity.getLock());
	}

	public List<Map<String, Object>> schema() {
		return schemaCatalog == null ? new ArrayList<>() : schemaCatalog.snapshot();
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

	public Map<String, Object> executeActions(List<AIConsoleAction> actions, AIGeneratedApi definition) {
		if ((actions == null || actions.isEmpty()) && definition == null) {
			throw new IllegalArgumentException("没有可执行的操作");
		}
		List<Map<String, Object>> results = new ArrayList<>();
		String createdGroupId = null;
		if (actions != null) {
			for (AIConsoleAction action : actions) {
				Map<String, Object> one = executeOne(action);
				results.add(one);
				if ("createGroup".equals(one.get("op")) && one.get("id") != null) {
					createdGroupId = String.valueOf(one.get("id"));
				}
			}
		}
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("results", results);
		if (definition != null && StringUtils.hasText(definition.getScript()) && StringUtils.hasText(definition.getPath())) {
			String groupId = createdGroupId;
			if (!StringUtils.hasText(groupId) && StringUtils.hasText(definition.getGroupName())) {
				Group group = findGroup(null, definition.getGroupName(), "api");
				if (group != null) {
					groupId = group.getId();
				}
			}
			ApiInfo saved = saveGenerated(definition, null, groupId, false);
			data.put("api", saved);
		}
		return data;
	}

	private Map<String, Object> executeOne(AIConsoleAction action) {
		if (action == null || !StringUtils.hasText(action.getOp())) {
			throw new IllegalArgumentException("操作类型不能为空");
		}
		String op = canonicalOp(action.getOp());
		MagicResourceService resourceService = MagicConfiguration.getMagicResourceService();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("op", op);
		switch (op) {
			case "createGroup": {
				String type = normalizeType(action.getType());
				if (!StringUtils.hasText(type)) {
					type = "api";
				}
				notBlank(action.getName(), "分组名称");
				Group group = new Group();
				group.setType(type);
				group.setName(sanitizeName(action.getName()));
				String parentId = resolveParentId(action, type);
				group.setParentId(parentId);
				if (StringUtils.hasText(action.getPath())) {
					group.setPath(normalizePath(action.getPath()));
				} else if (!"datasource".equals(type)) {
					group.setPath("/g" + Long.toHexString(System.currentTimeMillis()));
				}
				if (!resourceService.saveGroup(group)) {
					throw new IllegalStateException("创建分组失败");
				}
				result.put("id", group.getId());
				result.put("name", group.getName());
				result.put("message", "已创建" + typeLabel(type) + "分组「" + group.getName() + "」");
				return result;
			}
			case "updateGroup": {
				Group current = requireGroup(action.getId(), action.getName(), normalizeType(action.getType()));
				Group updating = current.copy();
				String nextName = StringUtils.hasText(action.getNewName()) ? action.getNewName()
						: (StringUtils.hasText(action.getId()) ? action.getName() : null);
				if (StringUtils.hasText(nextName)) {
					updating.setName(sanitizeName(nextName));
				}
				if (StringUtils.hasText(action.getPath())) {
					updating.setPath(normalizePath(action.getPath()));
				}
				if (!resourceService.saveGroup(updating)) {
					throw new IllegalStateException("更新分组失败");
				}
				result.put("id", updating.getId());
				result.put("name", updating.getName());
				result.put("message", "已更新分组「" + updating.getName() + "」");
				return result;
			}
			case "move": {
				String srcId = resolveResourceId(action);
				String targetId = resolveTargetId(action, normalizeType(action.getType()));
				if (!resourceService.move(srcId, targetId)) {
					throw new IllegalStateException("移动失败");
				}
				result.put("id", srcId);
				result.put("message", "已移动「" + displayName(action) + "」");
				return result;
			}
			case "copyGroup": {
				Group source = requireGroup(action.getId(), action.getName(), normalizeType(action.getType()));
				String targetId = resolveTargetId(action, source.getType());
				String newId = resourceService.copyGroup(source.getId(), targetId);
				result.put("id", newId);
				result.put("name", source.getName());
				result.put("message", "已复制分组「" + source.getName() + "」");
				return result;
			}
			case "delete": {
				String id = resolveResourceId(action);
				String label = displayName(action);
				if (!resourceService.delete(id)) {
					throw new IllegalStateException("删除失败");
				}
				result.put("id", id);
				result.put("message", "已删除「" + label + "」");
				return result;
			}
			default:
				throw new IllegalArgumentException("不支持的操作: " + action.getOp());
		}
	}

	private String describeResources() {
		try {
			MagicResourceService resourceService = MagicConfiguration.getMagicResourceService();
			Map<String, TreeNode<Group>> trees = resourceService.tree();
			StringBuilder builder = new StringBuilder("当前分组与资源：\n");
			if (trees == null || trees.isEmpty()) {
				builder.append("（还没有分组。根节点 parentId=0）\n");
				return builder.toString();
			}
			int files = 0;
			for (TreeNode<Group> root : trees.values()) {
				List<Group> groups = new ArrayList<>();
				collectGroups(root, groups);
				for (Group group : groups) {
					if (group == null || !StringUtils.hasText(group.getId()) || group.getId().contains(":")) {
						continue;
					}
					builder.append("- 分组 type=").append(group.getType())
							.append(" name=").append(group.getName())
							.append(" id=").append(group.getId())
							.append(" parentId=").append(group.getParentId())
							.append(" path=").append(Objects.toString(group.getPath(), ""))
							.append('\n');
					if (files >= 40) {
						continue;
					}
					List<MagicEntity> list = resourceService.listFiles(group.getId());
					if (list == null) {
						continue;
					}
					for (MagicEntity entity : list) {
						if (files >= 40) {
							break;
						}
						builder.append("  - 文件 type=").append(group.getType())
								.append(" name=").append(entity.getName())
								.append(" id=").append(entity.getId())
								.append(" group=").append(group.getName())
								.append('\n');
						files++;
					}
				}
			}
			builder.append("根节点 parentId=0。\n");
			return builder.toString();
		} catch (RuntimeException ex) {
			return "";
		}
	}

	private void collectGroups(TreeNode<Group> node, List<Group> groups) {
		if (node == null) {
			return;
		}
		if (node.getNode() != null) {
			groups.add(node.getNode());
		}
		if (node.getChildren() == null) {
			return;
		}
		for (TreeNode<Group> child : node.getChildren()) {
			collectGroups(child, groups);
		}
	}

	private JSONObject firstPayload(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		List<String> candidates = new ArrayList<>();
		Matcher matcher = JSON_BLOCK.matcher(raw);
		while (matcher.find()) {
			candidates.add(matcher.group(1).trim());
		}
		String trimmed = raw.trim();
		if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
			candidates.add(trimmed);
		}
		for (String candidate : candidates) {
			try {
				if (candidate.startsWith("[")) {
					JSONObject wrapped = new JSONObject();
					wrapped.put("actions", JSON.parseArray(candidate));
					return wrapped;
				}
				JSONObject obj = JSON.parseObject(candidate);
				if (obj != null && (obj.containsKey("actions") || obj.containsKey("api")
						|| obj.containsKey("script") || obj.containsKey("path"))) {
					return obj;
				}
			} catch (RuntimeException ignore) {
				// try next
			}
		}
		return null;
	}

	private List<AIConsoleAction> parseActions(JSONObject payload) {
		if (payload == null || !(payload.get("actions") instanceof com.alibaba.fastjson.JSONArray)) {
			return Collections.emptyList();
		}
		List<AIConsoleAction> actions = payload.getJSONArray("actions").toJavaList(AIConsoleAction.class);
		if (actions == null || actions.isEmpty()) {
			return Collections.emptyList();
		}
		List<AIConsoleAction> usable = new ArrayList<>();
		for (AIConsoleAction action : actions) {
			if (action != null && StringUtils.hasText(action.getOp())) {
				usable.add(action);
			}
		}
		return usable;
	}

	private AIGeneratedApi definitionFrom(JSONObject payload, String raw, List<AIConsoleAction> actions) {
		if (payload != null && payload.get("api") instanceof JSONObject) {
			return ((JSONObject) payload.get("api")).toJavaObject(AIGeneratedApi.class);
		}
		if (actions != null && !actions.isEmpty()) {
			return null;
		}
		return parseDefinition(raw);
	}

	private String summarizeActions(List<AIConsoleAction> actions) {
		StringBuilder builder = new StringBuilder();
		for (AIConsoleAction action : actions) {
			String op = canonicalOp(action.getOp());
			String name = displayName(action);
			switch (op) {
				case "createGroup":
					builder.append("准备创建").append(typeLabel(normalizeType(action.getType())))
							.append("分组「").append(name).append("」");
					if (StringUtils.hasText(action.getPath())) {
						builder.append("，路径 ").append(action.getPath());
					}
					builder.append('\n');
					break;
				case "updateGroup":
					builder.append("准备更新分组「").append(name).append("」\n");
					break;
				case "move":
					builder.append("准备移动「").append(name).append("」\n");
					break;
				case "copyGroup":
					builder.append("准备复制分组「").append(name).append("」\n");
					break;
				case "delete":
					builder.append("准备删除「").append(name).append("」\n");
					break;
				default:
					builder.append("准备执行 ").append(action.getOp()).append('\n');
					break;
			}
		}
		builder.append("点「同意执行」后才会改左侧资源。");
		return builder.toString().trim();
	}

	private String resolveParentId(AIConsoleAction action, String type) {
		if (isRoot(action.getParentId()) || isRoot(action.getParentName())) {
			return Constants.ROOT_ID;
		}
		if (StringUtils.hasText(action.getParentId())) {
			Group parent = MagicConfiguration.getMagicResourceService().getGroup(action.getParentId());
			if (parent == null) {
				throw new IllegalArgumentException("上级分组不存在: " + action.getParentId());
			}
			if (StringUtils.hasText(type) && !type.equals(parent.getType())) {
				throw new IllegalArgumentException("上级分组类型不一致");
			}
			return parent.getId();
		}
		if (StringUtils.hasText(action.getParentName())) {
			Group parent = requireGroup(null, action.getParentName(), type);
			return parent.getId();
		}
		return Constants.ROOT_ID;
	}

	private String resolveTargetId(AIConsoleAction action, String type) {
		if (isRoot(action.getTargetId()) || isRoot(action.getTargetName())) {
			return Constants.ROOT_ID;
		}
		if (StringUtils.hasText(action.getTargetId())) {
			if (MagicConfiguration.getMagicResourceService().getGroup(action.getTargetId()) == null) {
				throw new IllegalArgumentException("目标分组不存在: " + action.getTargetId());
			}
			return action.getTargetId();
		}
		if (StringUtils.hasText(action.getTargetName())) {
			return requireGroup(null, action.getTargetName(), type).getId();
		}
		throw new IllegalArgumentException("需要目标分组");
	}

	private String resolveResourceId(AIConsoleAction action) {
		MagicResourceService resourceService = MagicConfiguration.getMagicResourceService();
		if (StringUtils.hasText(action.getId())) {
			if (resourceService.getGroup(action.getId()) != null || resourceService.file(action.getId()) != null) {
				return action.getId();
			}
			throw new IllegalArgumentException("找不到资源: " + action.getId());
		}
		notBlank(action.getName(), "名称");
		String type = normalizeType(action.getType());
		List<Group> groups = groupsByName(action.getName(), type);
		List<MagicEntity> files = filesByName(action.getName(), type);
		if (groups.size() + files.size() == 0) {
			throw new IllegalArgumentException("找不到「" + action.getName() + "」");
		}
		if (groups.size() + files.size() > 1) {
			throw new IllegalArgumentException("「" + action.getName() + "」不唯一，请换 id");
		}
		return groups.isEmpty() ? files.get(0).getId() : groups.get(0).getId();
	}

	private Group requireGroup(String id, String name, String type) {
		Group group = findGroup(id, name, type);
		if (group == null) {
			throw new IllegalArgumentException("找不到分组: " + (StringUtils.hasText(name) ? name : id));
		}
		return group;
	}

	private Group findGroup(String id, String name, String type) {
		MagicResourceService resourceService = MagicConfiguration.getMagicResourceService();
		if (StringUtils.hasText(id)) {
			if (isRoot(id)) {
				return null;
			}
			Group group = resourceService.getGroup(id);
			if (group == null || group.getId().contains(":")) {
				throw new IllegalArgumentException("找不到分组: " + id);
			}
			return group;
		}
		if (!StringUtils.hasText(name) || isRoot(name)) {
			return null;
		}
		List<Group> matches = groupsByName(name, type);
		if (matches.isEmpty()) {
			return null;
		}
		if (matches.size() > 1) {
			throw new IllegalArgumentException("分组「" + name + "」不唯一，请换 id");
		}
		return matches.get(0);
	}

	private List<Group> groupsByName(String name, String type) {
		List<Group> matches = new ArrayList<>();
		Map<String, TreeNode<Group>> trees = MagicConfiguration.getMagicResourceService().tree();
		if (trees == null) {
			return matches;
		}
		List<Group> groups = new ArrayList<>();
		for (TreeNode<Group> root : trees.values()) {
			collectGroups(root, groups);
		}
		for (Group group : groups) {
			if (group == null || !StringUtils.hasText(group.getId()) || group.getId().contains(":")) {
				continue;
			}
			if (!name.equals(group.getName())) {
				continue;
			}
			if (StringUtils.hasText(type) && !type.equals(group.getType())) {
				continue;
			}
			matches.add(group);
		}
		return matches;
	}

	private List<MagicEntity> filesByName(String name, String type) {
		List<MagicEntity> matches = new ArrayList<>();
		MagicResourceService resourceService = MagicConfiguration.getMagicResourceService();
		Map<String, TreeNode<Group>> trees = resourceService.tree();
		if (trees == null) {
			return matches;
		}
		for (Map.Entry<String, TreeNode<Group>> entry : trees.entrySet()) {
			if (StringUtils.hasText(type) && !type.equals(entry.getKey())) {
				continue;
			}
			List<Group> groups = new ArrayList<>();
			collectGroups(entry.getValue(), groups);
			for (Group group : groups) {
				if (group == null || !StringUtils.hasText(group.getId()) || group.getId().contains(":")) {
					continue;
				}
				List<MagicEntity> files = resourceService.listFiles(group.getId());
				if (files == null) {
					continue;
				}
				for (MagicEntity entity : files) {
					if (name.equals(entity.getName())) {
						matches.add(entity);
					}
				}
			}
		}
		return matches;
	}

	private static String canonicalOp(String op) {
		String value = op == null ? "" : op.trim().toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
		switch (value) {
			case "creategroup":
			case "addgroup":
				return "createGroup";
			case "updategroup":
			case "renamegroup":
			case "rename":
				return "updateGroup";
			case "move":
			case "movegroup":
			case "movefile":
				return "move";
			case "copygroup":
			case "copy":
				return "copyGroup";
			case "delete":
			case "remove":
			case "deletegroup":
				return "delete";
			default:
				return op == null ? "" : op.trim();
		}
	}

	private static String normalizeType(String type) {
		if (!StringUtils.hasText(type)) {
			return "";
		}
		switch (type.trim().toLowerCase(Locale.ROOT)) {
			case "api":
			case "接口":
				return "api";
			case "function":
			case "函数":
				return "function";
			case "datasource":
			case "数据源":
				return "datasource";
			case "task":
			case "任务":
			case "定时任务":
				return "task";
			default:
				return type.trim();
		}
	}

	private static String typeLabel(String type) {
		switch (type == null ? "" : type) {
			case "function":
				return "函数";
			case "datasource":
				return "数据源";
			case "task":
				return "定时任务";
			default:
				return "接口";
		}
	}

	private static boolean isRoot(String value) {
		if (!StringUtils.hasText(value)) {
			return false;
		}
		String text = value.trim();
		return Constants.ROOT_ID.equals(text) || "根".equals(text) || "根节点".equals(text)
				|| "根目录".equals(text) || "root".equalsIgnoreCase(text);
	}

	private static String displayName(AIConsoleAction action) {
		if (StringUtils.hasText(action.getNewName())) {
			return action.getNewName().trim();
		}
		if (StringUtils.hasText(action.getName())) {
			return action.getName().trim();
		}
		if (StringUtils.hasText(action.getId())) {
			return action.getId();
		}
		return "未命名";
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
