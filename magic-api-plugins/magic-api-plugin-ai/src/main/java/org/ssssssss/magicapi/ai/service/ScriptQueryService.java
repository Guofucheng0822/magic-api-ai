package org.ssssssss.magicapi.ai.service;

import org.ssssssss.magicapi.utils.ScriptManager;
import org.ssssssss.script.MagicScriptContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * 在控制台里直接执行只读脚本，把查询结果返回给 AI 对话。
 */
public class ScriptQueryService {

	private static final int ROW_LIMIT = 100;

	private static final int EXPORT_LIMIT = 20000;

	private static final Pattern WRITE = Pattern.compile(
			"(?i)(\\b(insert|update|delete|drop|alter|truncate|replace|grant)\\b)|(\\bdb\\s*\\.\\s*(update|insert|delete|batchUpdate|cache)\\b)|(\\bredis\\s*\\.\\s*(set|del|delete|flush|hset|hdel|hmset|lpush|rpush|sadd|zadd|expire|persist|rename)\\b)");

	private static final Pattern QUERY = Pattern.compile(
			"(?is)(\\b(select|selectOne|selectValue|selectInt|page|count)\\s*\\()|(\\bredis\\s*\\.\\s*(get|hget|hgetall|hkeys|lrange|llen|smembers|scard|zrange|type|ttl|exists|scan|keys)\\s*\\()");

	private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

	private ScriptQueryService() {
	}

	public static Map<String, Object> run(String script) {
		checkReadOnly(script);
		return shape(execute(script, 30), ROW_LIMIT);
	}

	public static byte[] exportCsv(String script) {
		checkReadOnly(script);
		String csv = toCsv(shape(execute(script, 60), EXPORT_LIMIT));
		byte[] body = csv.getBytes(StandardCharsets.UTF_8);
		byte[] out = new byte[body.length + 3];
		out[0] = (byte) 0xEF;
		out[1] = (byte) 0xBB;
		out[2] = (byte) 0xBF;
		System.arraycopy(body, 0, out, 3, body.length);
		return out;
	}

	private static void checkReadOnly(String script) {
		if (script == null || script.trim().isEmpty()) {
			throw new IllegalArgumentException("脚本不能为空");
		}
		String body = stripStrings(stripComments(script));
		if (!QUERY.matcher(body).find()) {
			throw new IllegalArgumentException("只执行查询脚本");
		}
		if (WRITE.matcher(body).find()) {
			throw new IllegalArgumentException("只允许查询，不能执行写入");
		}
	}

	private static Object execute(String script, int seconds) {
		Future<Object> future = EXECUTOR.submit(() -> {
			MagicScriptContext context = new MagicScriptContext();
			context.setScriptName("ai-query");
			return ScriptManager.executeScript(script, context);
		});
		try {
			return future.get(seconds, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new IllegalArgumentException("查询超过 " + seconds + " 秒");
		} catch (ExecutionException e) {
			throw new IllegalArgumentException(message(e.getCause()));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalArgumentException("查询被中断");
		}
	}

	private static String stripComments(String script) {
		return script.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("(?m)//.*$", " ");
	}

	private static String stripStrings(String script) {
		return script.replaceAll("(?s)\"(\\\\.|[^\"\\\\])*\"", " ").replaceAll("(?s)'(\\\\.|[^'\\\\])*'", " ");
	}

	private static String message(Throwable error) {
		Throwable current = error;
		String text = "查询失败";
		while (current != null) {
			if (current.getMessage() != null && !current.getMessage().trim().isEmpty()) {
				text = current.getMessage().trim();
			}
			current = current.getCause();
		}
		return text.length() > 500 ? text.substring(0, 500) : text;
	}

	private static Map<String, Object> shape(Object value, int rowLimit) {
		List<Map<String, Object>> sections = new ArrayList<>();
		if (value instanceof List) {
			sections.add(tableSection("结果", (List<?>) value, ((List<?>) value).size(), rowLimit));
		} else if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) value;
			Object nested = map.get("list");
			if (nested instanceof List && (map.containsKey("total") || map.containsKey("page"))) {
				Object count = map.get("total");
				long total = count instanceof Number ? ((Number) count).longValue() : ((List<?>) nested).size();
				sections.add(tableSection("结果", (List<?>) nested, total, rowLimit));
			} else if (isRecord(map)) {
				sections.add(tableSection("结果", java.util.Collections.singletonList(map), 1, rowLimit));
			} else {
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					sections.add(section(String.valueOf(entry.getKey()), entry.getValue(), rowLimit));
				}
			}
		} else {
			sections.add(scalarSection("结果", value));
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("sections", sections);
		Map<String, Object> first = sections.isEmpty() ? tableSection("结果", java.util.Collections.emptyList(), 0, rowLimit) : sections.get(0);
		result.put("columns", first.get("columns"));
		result.put("rows", first.get("rows"));
		result.put("total", first.get("total"));
		result.put("truncated", first.get("truncated"));
		return result;
	}

	private static Map<String, Object> section(String name, Object value, int rowLimit) {
		if (value instanceof List) {
			return tableSection(name, (List<?>) value, ((List<?>) value).size(), rowLimit);
		}
		if (value instanceof Map) {
			return tableSection(name, java.util.Collections.singletonList(value), 1, rowLimit);
		}
		return scalarSection(name, value);
	}

	private static boolean isRecord(Map<?, ?> map) {
		for (Object value : map.values()) {
			if (value instanceof Map || value instanceof List) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> tableSection(String name, List<?> list, long total, int rowLimit) {
		boolean truncated = list.size() > rowLimit;
		List<?> shown = truncated ? list.subList(0, rowLimit) : list;
		List<String> columns = new ArrayList<>();
		for (Object item : shown) {
			if (item instanceof Map) {
				for (Object key : ((Map<String, Object>) item).keySet()) {
					String column = String.valueOf(key);
					if (!columns.contains(column)) {
						columns.add(column);
					}
				}
			}
		}
		if (columns.isEmpty()) {
			columns.add("value");
		}
		List<List<String>> rows = new ArrayList<>();
		for (Object item : shown) {
			List<String> row = new ArrayList<>();
			if (item instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) item;
				for (String column : columns) {
					row.add(cell(map.get(column)));
				}
			} else {
				row.add(cell(item));
			}
			rows.add(row);
		}
		Map<String, Object> section = new LinkedHashMap<>();
		section.put("name", name);
		section.put("columns", columns);
		section.put("rows", rows);
		section.put("total", total);
		section.put("truncated", truncated);
		return section;
	}

	private static Map<String, Object> scalarSection(String name, Object value) {
		Map<String, Object> section = new LinkedHashMap<>();
		section.put("name", name);
		section.put("truncated", false);
		if (value == null) {
			section.put("columns", java.util.Collections.emptyList());
			section.put("rows", java.util.Collections.emptyList());
			section.put("total", 0);
			return section;
		}
		section.put("columns", java.util.Collections.singletonList("value"));
		List<List<String>> rows = new ArrayList<>();
		rows.add(java.util.Collections.singletonList(cell(value)));
		section.put("rows", rows);
		section.put("total", 1);
		return section;
	}

	private static String cell(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof Map || value instanceof List) {
			return String.valueOf(value);
		}
		return String.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	private static String toCsv(Map<String, Object> shaped) {
		StringBuilder csv = new StringBuilder();
		List<Map<String, Object>> sections = (List<Map<String, Object>>) shaped.get("sections");
		if (sections == null) {
			return "";
		}
		for (Map<String, Object> section : sections) {
			if (csv.length() > 0) {
				csv.append("\r\n");
			}
			csv.append(csvCell(String.valueOf(section.get("name")))).append("\r\n");
			List<String> columns = (List<String>) section.get("columns");
			List<List<String>> rows = (List<List<String>>) section.get("rows");
			if (rows == null || rows.isEmpty()) {
				csv.append("没有数据\r\n");
				continue;
			}
			if (columns != null) {
				for (int i = 0; i < columns.size(); i++) {
					if (i > 0) {
						csv.append(',');
					}
					csv.append(csvCell(columns.get(i)));
				}
				csv.append("\r\n");
			}
			for (List<String> row : rows) {
				for (int i = 0; i < row.size(); i++) {
					if (i > 0) {
						csv.append(',');
					}
					csv.append(csvCell(row.get(i)));
				}
				csv.append("\r\n");
			}
			if (Boolean.TRUE.equals(section.get("truncated"))) {
				csv.append(csvCell("仅导出前 " + EXPORT_LIMIT + " 条")).append("\r\n");
			}
		}
		return csv.toString();
	}

	private static String csvCell(String value) {
		String text = value == null ? "" : value;
		if (text.contains("\"") || text.contains(",") || text.contains("\n") || text.contains("\r")) {
			return "\"" + text.replace("\"", "\"\"") + "\"";
		}
		return text;
	}
}
