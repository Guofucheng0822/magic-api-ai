package org.ssssssss.magicapi.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取已注册数据源的表和字段，供数据源面板和 AI 提示词使用。
 */
@Component
public class DatasourceSchemaService implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger logger = LoggerFactory.getLogger(DatasourceSchemaService.class);

	private static final int TABLE_LIMIT = 80;

	private static final int COLUMN_LIMIT = 80;

	private static final int PROMPT_LIMIT = 48000;

	private static final long CACHE_MILLIS = 180_000L;

	private static DatasourceSchemaService instance;

	private final MagicDynamicDataSource dataSource;

	private final ApplicationContext applicationContext;

	private volatile List<Map<String, Object>> cached;

	private volatile long cachedAt;

	public DatasourceSchemaService(MagicDynamicDataSource dataSource, ApplicationContext applicationContext) {
		this.dataSource = dataSource;
		this.applicationContext = applicationContext;
		instance = this;
	}

	public static String currentPrompt() {
		return promptFor("");
	}

	/**
	 * 把已加载的库、表和字段交给模型，由模型自己对应业务说法。
	 */
	public static String promptFor(String userText) {
		return instance == null ? "" : instance.buildPrompt(userText);
	}

	public static String optionalSourcesPrompt() {
		if (instance == null) {
			return "";
		}
		StringBuilder prompt = new StringBuilder();
		if (instance.dataSource.contains("doris")) {
			prompt.append("- Doris 用 db.doris.select、db.doris.selectOne，走 MySQL 协议。\n");
		}
		if (instance.dataSource.contains("selectdb")) {
			prompt.append("- SelectDB 用 db.selectdb.select、db.selectdb.selectOne，走 MySQL 协议。\n");
		}
		return prompt.toString();
	}

	public List<Map<String, String>> optionalSources() {
		List<Map<String, String>> sources = new ArrayList<>();
		addOptional(sources, "doris", "Doris");
		addOptional(sources, "selectdb", "SelectDB");
		return sources;
	}

	private void addOptional(List<Map<String, String>> sources, String key, String name) {
		if (!dataSource.contains(key)) {
			return;
		}
		Map<String, String> source = new LinkedHashMap<>();
		source.put("key", key);
		source.put("name", name);
		sources.add(source);
	}

	public static DatasourceSchemaService get() {
		return instance;
	}

	public String promptText() {
		return buildPrompt("");
	}

	public String buildPrompt(String userText) {
		String catalog = catalog(describe(), true);
		if (catalog.length() > PROMPT_LIMIT) {
			catalog = catalog(describe(), false);
		}
		return catalog.length() > PROMPT_LIMIT ? catalog.substring(0, PROMPT_LIMIT) : catalog;
	}

	@SuppressWarnings("unchecked")
	private static String catalog(List<Map<String, Object>> sources, boolean withComment) {
		StringBuilder prompt = new StringBuilder();
		prompt.append("下面是当前已加载的库、表和字段，直接来自数据库。用户用业务说法提问时，你自己根据表名、字段名和注释判断该查哪张表、哪个库。不要问用户表在哪个库，不要让用户补充表名。SQL 只能使用该表列出的字段。同名表在不同库的字段不一样，不能把另一个库的字段抄过来。varchar、char 用字符串比较。datetime、timestamp、date 才能和日期比较。注释写明 yyyy-MM 的字段是字符串月份。\n");
		boolean anyTable = false;
		for (Map<String, Object> source : sources) {
			String key = String.valueOf(source.get("key"));
			if ("redis".equals(source.get("kind"))) {
				prompt.append("[redis] 没有表。\n");
				continue;
			}
			prompt.append("[").append(key).append("] 查询写法 ").append(source.get("usage")).append("\n");
			List<Map<String, Object>> tables = (List<Map<String, Object>>) source.get("tables");
			if (tables == null || tables.isEmpty()) {
				prompt.append("（没有读到表）\n");
				continue;
			}
			for (Map<String, Object> table : tables) {
				anyTable = true;
				prompt.append(table.get("name")).append(": ");
				List<Map<String, String>> columns = (List<Map<String, String>>) table.get("columns");
				if (columns == null || columns.isEmpty()) {
					prompt.append("（没有读到字段）\n");
					continue;
				}
				for (int i = 0; i < columns.size(); i++) {
					if (i > 0) {
						prompt.append(", ");
					}
					Map<String, String> column = columns.get(i);
					prompt.append(column.get("name")).append(" ").append(column.get("type"));
					String comment = column.get("comment");
					if (withComment && comment != null && !comment.isEmpty() && !"null".equals(comment)) {
						String text = comment.length() > 40 ? comment.substring(0, 40) : comment;
						prompt.append(" ").append(text.replace('\n', ' '));
					}
				}
				prompt.append("\n");
			}
		}
		if (!anyTable) {
			prompt.append("还没有读到表。不要编造字段名。\n");
		}
		return prompt.toString();
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		Thread thread = new Thread(this::warmQuietly, "magic-schema-warm");
		thread.setDaemon(true);
		thread.start();
	}

	private void warmQuietly() {
		for (int i = 0; i < 6; i++) {
			try {
				Thread.sleep(i == 0 ? 8000L : 10000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			try {
				if (warmIfReady()) {
					logger.info("数据源表结构已缓存");
					return;
				}
			} catch (Exception e) {
				logger.warn("预热表结构失败: {}", e.getMessage());
			}
		}
	}

	public synchronized boolean warmIfReady() {
		List<Map<String, Object>> sources = readAll();
		if (!hasTables(sources)) {
			return false;
		}
		this.cached = sources;
		this.cachedAt = System.currentTimeMillis();
		return true;
	}

	public synchronized List<Map<String, Object>> describe() {
		if (cached != null && System.currentTimeMillis() - cachedAt < CACHE_MILLIS) {
			return cached;
		}
		List<Map<String, Object>> sources = readAll();
		this.cached = sources;
		this.cachedAt = System.currentTimeMillis();
		return sources;
	}

	private List<Map<String, Object>> readAll() {
		List<Map<String, Object>> sources = new ArrayList<>();
		for (MagicDynamicDataSource.DataSourceNode node : dataSource.getDataSources()) {
			sources.add(readJdbc(node));
		}
		Map<String, Object> redis = readRedis();
		if (redis != null) {
			sources.add(redis);
		}
		return sources;
	}

	private static boolean hasTables(List<Map<String, Object>> sources) {
		for (Map<String, Object> source : sources) {
			Object tables = source.get("tables");
			if (tables instanceof List && !((List<?>) tables).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private Map<String, Object> readJdbc(MagicDynamicDataSource.DataSourceNode node) {
		String key = node.getKey() == null ? "" : node.getKey();
		Map<String, Object> source = new LinkedHashMap<>();
		source.put("key", key.isEmpty() ? "default" : key);
		source.put("name", node.getName() == null || node.getName().isEmpty() ? (key.isEmpty() ? "默认数据源" : key) : node.getName());
		source.put("kind", "jdbc");
		source.put("usage", usage(key));
		source.put("tables", new ArrayList<Map<String, Object>>());
		try (Connection connection = node.getDataSource().getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			String url = meta.getURL() == null ? "" : meta.getURL();
			String product = meta.getDatabaseProductName() == null ? "" : meta.getDatabaseProductName();
			source.put("product", product);
			source.put("url", hideUser(url));
			boolean clickHouse = product.toLowerCase().contains("clickhouse") || url.toLowerCase().contains("clickhouse");
			List<Map<String, Object>> tables = clickHouse ? clickHouseTables(node) : mysqlTables(node);
			if (tables.isEmpty() && !clickHouse) {
				tables = metaTables(meta);
			}
			source.put("tables", tables);
			source.put("label", source.get("name") + "（" + usage(key) + "）");
		} catch (Exception e) {
			logger.warn("读取数据源 {} 表结构失败: {}", key, e.getMessage());
			source.put("error", e.getMessage());
			source.put("label", source.get("name") + "（" + usage(key) + "）");
		}
		return source;
	}

	private List<Map<String, Object>> mysqlTables(MagicDynamicDataSource.DataSourceNode node) {
		List<Map<String, Object>> rows = node.getJdbcTemplate().queryForList(
				"select table_name, column_name, data_type, column_comment from information_schema.columns where table_schema = database() order by table_name, ordinal_position");
		return groupColumns(rows, "table_name", "column_name", "data_type", "column_comment");
	}

	private List<Map<String, Object>> clickHouseTables(MagicDynamicDataSource.DataSourceNode node) {
		List<Map<String, Object>> rows;
		try {
			rows = node.getJdbcTemplate().queryForList(
					"select table, name, type, comment from system.columns where database = currentDatabase() order by table, position");
		} catch (Exception e) {
			rows = node.getJdbcTemplate().queryForList(
					"select table, name, type from system.columns where database = currentDatabase() order by table, position");
		}
		return groupColumns(rows, "table", "name", "type", "comment");
	}

	private List<Map<String, Object>> groupColumns(List<Map<String, Object>> rows, String tableKey, String columnKey, String typeKey, String commentKey) {
		Map<String, Map<String, Object>> tables = new LinkedHashMap<>();
		for (Map<String, Object> row : rows) {
			String tableName = String.valueOf(value(row, tableKey));
			if (tables.size() >= TABLE_LIMIT && !tables.containsKey(tableName)) {
				continue;
			}
			Map<String, Object> table = tables.computeIfAbsent(tableName, name -> {
				Map<String, Object> item = new LinkedHashMap<>();
				item.put("name", name);
				item.put("columns", new ArrayList<Map<String, String>>());
				return item;
			});
			@SuppressWarnings("unchecked")
			List<Map<String, String>> columns = (List<Map<String, String>>) table.get("columns");
			if (columns.size() >= COLUMN_LIMIT) {
				continue;
			}
			Map<String, String> column = new LinkedHashMap<>();
			column.put("name", String.valueOf(value(row, columnKey)));
			column.put("type", String.valueOf(value(row, typeKey)));
			Object comment = value(row, commentKey);
			if (comment != null) {
				column.put("comment", String.valueOf(comment));
			}
			columns.add(column);
		}
		return new ArrayList<>(tables.values());
	}

	private static Object value(Map<String, Object> row, String key) {
		if (row.containsKey(key)) {
			return row.get(key);
		}
		for (Map.Entry<String, Object> entry : row.entrySet()) {
			if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private List<Map<String, Object>> metaTables(DatabaseMetaData meta) throws Exception {
		List<Map<String, Object>> tables = new ArrayList<>();
		try (java.sql.ResultSet resultSet = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
			while (resultSet.next() && tables.size() < TABLE_LIMIT) {
				Map<String, Object> table = new LinkedHashMap<>();
				table.put("name", resultSet.getString("TABLE_NAME"));
				table.put("columns", new ArrayList<Map<String, String>>());
				tables.add(table);
			}
		}
		return tables;
	}

	private Map<String, Object> readRedis() {
		RedisTemplate<?, ?> template = findRedisTemplate();
		if (template == null) {
			return null;
		}
		Map<String, Object> source = new LinkedHashMap<>();
		source.put("key", "redis");
		source.put("name", "Redis");
		source.put("kind", "redis");
		source.put("usage", "redis.get / redis.scan");
		source.put("label", "Redis（redis.get / redis.scan，只读）");
		source.put("tables", new ArrayList<>());
		List<String> keys = new ArrayList<>();
		Object cursor = null;
		try {
			ScanOptions options = ScanOptions.scanOptions().match("*").count(20).build();
			cursor = template.getClass().getMethod("scan", ScanOptions.class).invoke(template, options);
			java.lang.reflect.Method hasNext = cursor.getClass().getMethod("hasNext");
			java.lang.reflect.Method next = cursor.getClass().getMethod("next");
			while ((Boolean) hasNext.invoke(cursor) && keys.size() < 20) {
				keys.add(String.valueOf(next.invoke(cursor)));
			}
		} catch (Exception e) {
			source.put("error", e.getMessage());
		} finally {
			if (cursor != null) {
				try {
					cursor.getClass().getMethod("close").invoke(cursor);
				} catch (Exception ignored) {
					// 旧版本游标没有 close
				}
			}
		}
		source.put("keys", keys);
		return source;
	}

	@SuppressWarnings("rawtypes")
	private RedisTemplate<?, ?> findRedisTemplate() {
		Map<String, RedisTemplate> beans = applicationContext.getBeansOfType(RedisTemplate.class);
		for (RedisTemplate candidate : beans.values()) {
			if (!candidate.getClass().getName().contains("StringRedisTemplate")) {
				return candidate;
			}
		}
		return null;
	}

	private static String usage(String key) {
		if (key == null || key.isEmpty()) {
			return "db.select";
		}
		return "db." + key + ".select";
	}

	private static String hideUser(String url) {
		return url.replaceAll("(?i)(user|password)=[^&]*", "$1=***");
	}
}
