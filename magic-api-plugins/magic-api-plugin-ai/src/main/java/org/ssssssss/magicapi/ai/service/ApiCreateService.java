package org.ssssssss.magicapi.ai.service;

import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.exception.MagicAPIException;
import org.ssssssss.magicapi.core.model.ApiInfo;
import org.ssssssss.magicapi.core.model.DataType;
import org.ssssssss.magicapi.core.model.Group;
import org.ssssssss.magicapi.core.model.Parameter;
import org.ssssssss.magicapi.core.model.TreeNode;
import org.ssssssss.magicapi.core.service.MagicResourceService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 把 AI 生成的脚本保存成一个新接口，不覆盖当前打开的文件。
 */
@Component
public class ApiCreateService {

	private static final String GROUP_NAME = "AI生成";

	private static final String GROUP_PATH = "/ai";

	private static ApiCreateService instance;

	public ApiCreateService() {
		instance = this;
	}

	public static ApiCreateService get() {
		if (instance == null) {
			throw new IllegalStateException("新建接口服务未就绪");
		}
		return instance;
	}

	public Map<String, String> create(Map<String, Object> body) {
		String script = text(body == null ? null : body.get("script"));
		if (script == null || script.trim().isEmpty()) {
			throw new IllegalArgumentException("脚本不能为空");
		}
		String name = cleanName(text(body.get("name")));
		String path = cleanPath(text(body.get("path")), name);
		String method = cleanMethod(text(body.get("method")));
		MagicResourceService resourceService = MagicConfiguration.getMagicResourceService();
		String groupId = ensureGroup(resourceService);
		ApiInfo api = saveNew(resourceService, groupId, name, path, method, script, text(body.get("description")), parameters(body.get("parameters")));
		Map<String, String> result = new LinkedHashMap<>();
		result.put("id", api.getId());
		result.put("name", api.getName());
		result.put("path", api.getPath());
		result.put("method", api.getMethod());
		result.put("groupId", groupId);
		result.put("groupName", GROUP_NAME);
		return result;
	}

	private ApiInfo saveNew(MagicResourceService resourceService, String groupId, String name, String path, String method, String script, String description, List<Parameter> parameters) {
		RuntimeException last = null;
		for (int i = 0; i < 20; i++) {
			ApiInfo api = new ApiInfo();
			api.setName(i == 0 ? name : name + i);
			api.setPath(i == 0 ? path : path + i);
			api.setMethod(method);
			api.setScript(script);
			api.setGroupId(groupId);
			api.setDescription(description);
			api.setParameters(parameters);
			try {
				if (resourceService.saveFile(api)) {
					return api;
				}
				last = new IllegalStateException("保存失败");
			} catch (RuntimeException e) {
				if (!isConflict(e)) {
					throw e;
				}
				last = e;
			}
		}
		throw last == null ? new MagicAPIException("保存失败") : last;
	}

	private static boolean isConflict(RuntimeException e) {
		String message = e.getMessage() == null ? "" : e.getMessage();
		return message.contains("路径") || message.contains("冲突") || message.contains("保存失败");
	}

	private String ensureGroup(MagicResourceService resourceService) {
		TreeNode<Group> tree = resourceService.tree("api");
		if (tree != null) {
			TreeNode<Group> found = tree.findTreeNode(group -> GROUP_NAME.equals(group.getName()));
			if (found != null && found.getNode() != null) {
				return found.getNode().getId();
			}
		}
		Group group = new Group();
		group.setName(GROUP_NAME);
		group.setType("api");
		group.setPath(GROUP_PATH);
		group.setParentId(Constants.ROOT_ID);
		if (!resourceService.saveGroup(group)) {
			throw new IllegalStateException("创建分组失败");
		}
		return group.getId();
	}

	private static String text(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private static List<Parameter> parameters(Object raw) {
		List<String> names = new ArrayList<>();
		if (raw instanceof List) {
			for (Object item : (List<?>) raw) {
				addName(names, text(item));
			}
		} else {
			String value = text(raw);
			if (value != null) {
				for (String part : value.split("[,，\\s]+")) {
					addName(names, part);
				}
			}
		}
		List<Parameter> parameters = new ArrayList<>();
		for (String name : names) {
			Parameter parameter = new Parameter();
			parameter.setName(name);
			parameter.setDataType(DataType.String);
			parameter.setRequired(true);
			parameters.add(parameter);
		}
		return parameters;
	}

	private static void addName(List<String> names, String name) {
		if (name == null) {
			return;
		}
		String value = name.trim();
		if (value.isEmpty() || names.contains(value) || !value.matches("[A-Za-z_][\\w]*")) {
			return;
		}
		names.add(value);
	}

	private static String cleanName(String name) {
		String value = name == null ? "" : name.trim();
		if (value.isEmpty()) {
			value = "aiApi";
		}
		value = value.replaceAll("[\\\\/:*?\"<>|\\s]+", "");
		if (value.isEmpty()) {
			value = "aiApi";
		}
		return value.length() > 64 ? value.substring(0, 64) : value;
	}

	private static String cleanPath(String path, String name) {
		String value = path == null ? "" : path.trim();
		if (value.isEmpty()) {
			value = "/" + name;
		}
		if (!value.startsWith("/")) {
			value = "/" + value;
		}
		return value.replaceAll("\\s+", "");
	}

	private static String cleanMethod(String method) {
		String value = method == null ? "GET" : method.trim().toUpperCase(Locale.ROOT);
		if (!value.matches("GET|POST|PUT|DELETE|HEAD|PATCH")) {
			return "GET";
		}
		return value;
	}
}
