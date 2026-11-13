package org.ssssssss.magicapi.ai.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.ssssssss.magicapi.ai.model.AIChatRequest;
import org.ssssssss.magicapi.ai.model.AIChatResponse;
import org.ssssssss.magicapi.ai.model.AIConsoleAction;
import org.ssssssss.magicapi.ai.model.AIGeneratedApi;
import org.ssssssss.magicapi.ai.model.AITestRequest;
import org.ssssssss.magicapi.ai.service.MagicAIService;
import org.ssssssss.magicapi.core.annotation.Valid;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.interceptor.Authorization;
import org.ssssssss.magicapi.core.model.ApiInfo;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 对话、在线测试、一键发布
 */
public class MagicAIController extends MagicController implements MagicExceptionHandler {

	private final MagicAIService magicAIService;

	public MagicAIController(MagicConfiguration configuration, MagicAIService magicAIService) {
		super(configuration);
		this.magicAIService = magicAIService;
	}

	@GetMapping("/ai/status")
	@ResponseBody
	@Valid(requireLogin = false, authorization = Authorization.NONE)
	public JsonBean<Map<String, Object>> status() {
		return new JsonBean<>(magicAIService.status());
	}

	@GetMapping("/ai/schema")
	@ResponseBody
	@Valid(requireLogin = false, authorization = Authorization.NONE)
	public JsonBean<List<Map<String, Object>>> schema() {
		return new JsonBean<>(magicAIService.schema());
	}

	@PostMapping("/ai/chat")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<AIChatResponse> chat(MagicHttpServletRequest request) {
		try {
			AIChatRequest body = readJson(request).toJavaObject(AIChatRequest.class);
			return new JsonBean<>(magicAIService.chat(body));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return new JsonBean<>(0, e.getMessage());
		} catch (Exception e) {
			return new JsonBean<>(0, "AI 对话失败: " + e.getMessage());
		}
	}

	@PostMapping("/ai/apply")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<ApiInfo> apply(MagicHttpServletRequest request) {
		try {
			JSONObject body = readJson(request);
			AIGeneratedApi definition = JSONCompat.toGenerated(body.get("definition"));
			String apiId = body.get("apiId") == null ? null : String.valueOf(body.get("apiId"));
			String groupId = body.get("groupId") == null ? null : String.valueOf(body.get("groupId"));
			return new JsonBean<>(magicAIService.saveGenerated(definition, apiId, groupId, true));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return new JsonBean<>(0, e.getMessage());
		} catch (Exception e) {
			return new JsonBean<>(0, "保存接口失败: " + e.getMessage());
		}
	}

	@PostMapping("/ai/action")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.SAVE)
	public JsonBean<Map<String, Object>> action(MagicHttpServletRequest request) {
		try {
			JSONObject body = readJson(request);
			List<AIConsoleAction> actions = new ArrayList<>();
			Object rawActions = body.get("actions");
			if (rawActions instanceof JSONArray) {
				actions = ((JSONArray) rawActions).toJavaList(AIConsoleAction.class);
			} else if (rawActions instanceof String && ((String) rawActions).trim().startsWith("[")) {
				actions = JSON.parseArray((String) rawActions, AIConsoleAction.class);
			}
			AIGeneratedApi definition = null;
			if (body.get("definition") != null) {
				definition = JSONCompat.toGenerated(body.get("definition"));
			}
			return new JsonBean<>(magicAIService.executeActions(actions, definition));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return new JsonBean<>(0, e.getMessage());
		} catch (Exception e) {
			return new JsonBean<>(0, "执行失败: " + e.getMessage());
		}
	}

	@PostMapping("/ai/test")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<Object> test(MagicHttpServletRequest httpRequest) {
		try {
			AITestRequest request = readJson(httpRequest).toJavaObject(AITestRequest.class);
			Object result = magicAIService.testApi(request, httpRequest, configuration, configuration.getMagicAPIService());
			return new JsonBean<>(result);
		} catch (IllegalArgumentException | IllegalStateException e) {
			return new JsonBean<>(0, e.getMessage());
		} catch (Exception e) {
			return new JsonBean<>(0, "在线测试失败: " + e.getMessage());
		}
	}

	@PostMapping("/ai/publish")
	@ResponseBody
	@Valid(readonly = false, authorization = Authorization.LOCK)
	public JsonBean<Map<String, Object>> publish(MagicHttpServletRequest request) {
		try {
			JSONObject body = readJson(request);
			String apiId = body.get("apiId") == null ? null : String.valueOf(body.get("apiId"));
			boolean ok = magicAIService.publish(apiId);
			Map<String, Object> data = new HashMap<>();
			data.put("success", ok);
			data.put("apiId", apiId);
			data.put("locked", ok);
			data.put("message", ok ? "发布成功（已锁定，不可再编辑，解锁后可继续修改）" : "发布失败");
			return new JsonBean<>(data);
		} catch (IllegalArgumentException | IllegalStateException e) {
			return new JsonBean<>(0, e.getMessage());
		} catch (Exception e) {
			return new JsonBean<>(0, "发布失败: " + e.getMessage());
		}
	}

	/**
	 * 编辑器 request.sendPost 固定使用 application/x-www-form-urlencoded。
	 * 同时保留 application/json，方便直接调 HTTP API。
	 */
	private JSONObject readJson(MagicHttpServletRequest request) throws IOException {
		String contentType = request.getContentType() == null ? "" : request.getContentType().toLowerCase();
		if (contentType.contains("application/json")) {
			return parseObject(readUtf8(request.getInputStream()));
		}
		Map<String, String[]> parameters = parameterMap(request);
		if (parameters.isEmpty()) {
			String raw = readUtf8(request.getInputStream());
			if (raw.trim().startsWith("{")) {
				return parseObject(raw);
			}
			return new JSONObject();
		}
		JSONObject body = new JSONObject();
		for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
			String[] values = entry.getValue();
			if (values == null || values.length == 0) {
				continue;
			}
			if (values.length == 1) {
				putPath(body, entry.getKey(), values[0]);
			} else {
				JSONArray array = new JSONArray();
				for (String value : values) {
					array.add(value);
				}
				putPath(body, entry.getKey(), array);
			}
		}
		expandJsonField(body, "history");
		expandJsonField(body, "context");
		expandJsonField(body, "definition");
		expandJsonField(body, "actions");
		normalizeRepeatedObjectList(body, "history");
		return body;
	}

	/**
	 * qs 在 allowDots + arrayFormat=repeat 时，把对象数组编成重复字段：
	 * history.role=user&amp;history.content=a&amp;history.role=assistant&amp;history.content=b
	 * Servlet 会收成 history.role=["user","assistant"]。这里按相同下标还原成消息列表。
	 */
	private static void normalizeRepeatedObjectList(JSONObject body, String field) {
		Object value = body.get(field);
		if (value instanceof JSONObject) {
			body.put(field, toObjectList((JSONObject) value));
		}
	}

	private static Object toObjectList(JSONObject object) {
		if (object.isEmpty()) {
			return new JSONArray();
		}
		int size = -1;
		boolean parallelArrays = true;
		for (Object child : object.values()) {
			if (!(child instanceof JSONArray)) {
				parallelArrays = false;
				break;
			}
			int length = ((JSONArray) child).size();
			if (size < 0) {
				size = length;
			} else if (size != length) {
				parallelArrays = false;
				break;
			}
		}
		if (parallelArrays) {
			JSONArray list = new JSONArray();
			for (int i = 0; i < size; i++) {
				JSONObject item = new JSONObject(true);
				for (Map.Entry<String, Object> entry : object.entrySet()) {
					item.put(entry.getKey(), ((JSONArray) entry.getValue()).get(i));
				}
				list.add(item);
			}
			return list;
		}
		if (object.containsKey("role") || object.containsKey("content")) {
			JSONArray list = new JSONArray();
			list.add(object);
			return list;
		}
		return object;
	}

	private static JSONObject parseObject(String raw) {
		if (raw == null || raw.trim().isEmpty()) {
			return new JSONObject();
		}
		JSONObject object = JSON.parseObject(raw);
		return object == null ? new JSONObject() : object;
	}

	private static void expandJsonField(JSONObject body, String field) {
		Object value = body.get(field);
		if (!(value instanceof String)) {
			return;
		}
		String text = ((String) value).trim();
		if (text.startsWith("{")) {
			body.put(field, JSON.parseObject(text));
		} else if (text.startsWith("[")) {
			body.put(field, JSON.parseArray(text));
		}
	}

	private static void putPath(JSONObject root, String key, Object value) {
		List<String> parts = tokenize(key);
		if (parts.isEmpty()) {
			return;
		}
		Object current = root;
		for (int i = 0; i < parts.size(); i++) {
			String part = parts.get(i);
			boolean last = i == parts.size() - 1;
			if (last) {
				if (current instanceof JSONObject) {
					((JSONObject) current).put(part, value);
				} else if (current instanceof JSONArray) {
					setIndex((JSONArray) current, Integer.parseInt(part), value);
				}
				return;
			}
			boolean nextIsIndex = parts.get(i + 1).matches("\\d+");
			if (current instanceof JSONObject) {
				JSONObject object = (JSONObject) current;
				Object child = object.get(part);
				if (!(child instanceof JSONObject) && !(child instanceof JSONArray)) {
					child = nextIsIndex ? new JSONArray() : new JSONObject();
					object.put(part, child);
				}
				current = child;
			} else if (current instanceof JSONArray) {
				JSONArray array = (JSONArray) current;
				int index = Integer.parseInt(part);
				Object child = index < array.size() ? array.get(index) : null;
				if (!(child instanceof JSONObject) && !(child instanceof JSONArray)) {
					child = nextIsIndex ? new JSONArray() : new JSONObject();
					setIndex(array, index, child);
				}
				current = child;
			}
		}
	}

	private static final Pattern PATH_TOKEN = Pattern.compile("([^\\[\\].]+)|\\[(\\d+)\\]");

	private static List<String> tokenize(String key) {
		List<String> parts = new ArrayList<>();
		Matcher matcher = PATH_TOKEN.matcher(key);
		while (matcher.find()) {
			parts.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
		}
		return parts;
	}

	private static void setIndex(JSONArray array, int index, Object value) {
		while (array.size() <= index) {
			array.add(null);
		}
		array.set(index, value);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String[]> parameterMap(MagicHttpServletRequest request) {
		Object raw = request.getRequest();
		try {
			Method method = raw.getClass().getMethod("getParameterMap");
			Object value = method.invoke(raw);
			if (value instanceof Map) {
				return (Map<String, String[]>) value;
			}
		} catch (ReflectiveOperationException ignored) {
			// 回退为空，交给 JSON 正文解析
		}
		return new HashMap<>();
	}

	private static String readUtf8(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			return "";
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len;
		while ((len = inputStream.read(buffer)) != -1) {
			output.write(buffer, 0, len);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	/**
	 * 避免控制器直接依赖 fastjson API 细节
	 */
	static final class JSONCompat {
		private JSONCompat() {
		}

		@SuppressWarnings("unchecked")
		static AIGeneratedApi toGenerated(Object definition) {
			if (definition == null) {
				throw new IllegalArgumentException("definition 不能为空");
			}
			if (definition instanceof AIGeneratedApi) {
				return (AIGeneratedApi) definition;
			}
			return com.alibaba.fastjson.JSON.parseObject(
					com.alibaba.fastjson.JSON.toJSONString(definition),
					AIGeneratedApi.class
			);
		}
	}
}
