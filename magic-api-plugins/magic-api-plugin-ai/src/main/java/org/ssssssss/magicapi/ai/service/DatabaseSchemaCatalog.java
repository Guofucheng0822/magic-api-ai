package org.ssssssss.magicapi.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.ssssssss.magicapi.ai.starter.MagicAIConfig;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource.DataSourceNode;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 读取当前进程能连上的 MySQL、ClickHouse、Doris、Redis，供 AI 提示词和左侧数据源树使用。
 */
public class DatabaseSchemaCatalog {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaCatalog.class);

	private static final String MYSQL_SQL =
			"select t.table_name, ifnull(t.table_comment, ''), c.column_name, c.column_type, ifnull(c.column_comment, ''), ifnull(c.column_key, '') "
					+ "from information_schema.tables t "
					+ "join information_schema.columns c on c.table_schema = t.table_schema and c.table_name = t.table_name "
					+ "where t.table_schema = database() and t.table_type in ('BASE TABLE', 'VIEW') "
					+ "order by t.table_name, c.ordinal_position";

	private static final String CLICKHOUSE_SQL =
			"select t.name, ifNull(t.comment, ''), c.name, c.type, ifNull(c.comment, ''), '' "
					+ "from system.tables t "
					+ "left join system.columns c on c.database = t.database and c.table = t.name "
					+ "where t.database = currentDatabase() "
					+ "order by t.name, c.position";

	private final MagicAIConfig config;
	private final MagicDynamicDataSource dynamicDataSource;
	private final Environment environment;
	private final ApplicationContext applicationContext;

	private volatile List<Map<String, Object>> cached;
	private volatile long cachedAt;

	public DatabaseSchemaCatalog(MagicAIConfig config, MagicDynamicDataSource dynamicDataSource, Environment environment, ApplicationContext applicationContext) {
		this.config = config;
		this.dynamicDataSource = dynamicDataSource;
		this.environment = environment;
		this.applicationContext = applicationContext;
	}

	public List<Map<String, Object>> snapshot() {
		if (config != null && !config.isSchemaEnable()) {
			return new ArrayList<>();
		}
		int ttlMillis = Math.max(30, config == null ? 600 : config.getSchemaCacheSeconds()) * 1000;
		long now = System.currentTimeMillis();
		List<Map<String, Object>> snapshot = cached;
		if (snapshot != null && now - cachedAt < ttlMillis) {
			return snapshot;
		}
		synchronized (this) {
			now = System.currentTimeMillis();
			if (cached != null && now - cachedAt < ttlMillis) {
				return cached;
			}
			List<Map<String, Object>> loaded = load();
			cached = loaded;
			cachedAt = System.currentTimeMillis();
			logger.info("数据源目录已刷新，{} 个数据源", loaded.size());
			return loaded;
		}
	}

	public String describe() {
		List<Map<String, Object>> sources = snapshot();
		if (sources.isEmpty()) {
			return "";
		}
		int maxChars = config == null ? 24000 : Math.max(2000, config.getSchemaMaxChars());
		StringBuilder body = new StringBuilder();
		body.append("[当前可查询的数据源与表]\n");
		body.append("本节是本进程刚刚读到的真实结构，MySQL、ClickHouse、Redis 都在里面。回答有哪些表时按数据源分开列，不要只说主库。\n");
		body.append("db.select 只连接主库 MySQL。ClickHouse 必须写 db.clickhouse.select，其他数据源必须用该行给出的脚本。把 ClickHouse 的表写进 db.select 会查错库。\n");
		body.append("ClickHouse 表名区分大小写。条件参数写 #{name}。只问表清单时不要输出接口 JSON。\n");
		for (Map<String, Object> source : sources) {
			body.append(renderIndex(source));
		}
		body.append("\n");
		List<Map<String, Object>> ordered = new ArrayList<>(sources);
		ordered.sort((left, right) -> Integer.compare(columnOrder(left), columnOrder(right)));
		int remaining = maxChars - body.length();
		for (Map<String, Object> source : ordered) {
			if (remaining < 400) {
				break;
			}
			String block = renderColumns(source, remaining);
			if (!StringUtils.hasText(block)) {
				continue;
			}
			body.append(block);
			remaining -= block.length();
		}
		logger.info("数据源提示词 {} 字符，ClickHouse={}", body.length(), body.indexOf("db.clickhouse.select") >= 0);
		return body.toString();
	}

	private String renderIndex(Map<String, Object> source) {
		StringBuilder text = new StringBuilder();
		text.append("- ").append(source.get("name"));
		text.append(" | 脚本 ").append(source.get("script"));
		if (StringUtils.hasText(string(source.get("database")))) {
			text.append(" | 库 ").append(source.get("database"));
		}
		if (StringUtils.hasText(string(source.get("host")))) {
			text.append(" | ").append(source.get("host"));
		}
		String kind = string(source.get("kind"));
		if ("redis".equals(kind)) {
			text.append(" | 没有表，不能写 SQL。命令：redis.scan(pattern)、redis.get(key)、redis.hget(key, field)、redis.hgetall(key)、redis.lrange(key, 0, 20)、redis.smembers(key)、redis.zrange(key, 0, 20)、redis.type(key)、redis.ttl(key)、redis.exists(key)");
			if (source.get("keyCount") != null) {
				text.append("，键数量 ").append(source.get("keyCount"));
			}
			text.append("\n");
			@SuppressWarnings("unchecked")
			List<String> sampleKeys = (List<String>) source.get("sampleKeys");
			if (sampleKeys != null && !sampleKeys.isEmpty()) {
				text.append("  键样例：").append(String.join(", ", sampleKeys)).append("\n");
			}
			return text.toString();
		}
		if (StringUtils.hasText(string(source.get("error")))) {
			text.append(" | 读取表结构失败：").append(source.get("error")).append("\n");
			return text.toString();
		}
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> tables = (List<Map<String, Object>>) source.get("tables");
		if (tables == null || tables.isEmpty()) {
			text.append(" | 没有读到业务表\n");
			return text.toString();
		}
		text.append(" | ").append(tables.size()).append("张表：");
		for (int i = 0; i < tables.size(); i++) {
			if (i > 0) {
				text.append(", ");
			}
			text.append(tables.get(i).get("name"));
		}
		text.append("\n");
		return text.toString();
	}

	private String renderColumns(Map<String, Object> source, int budget) {
		String kind = string(source.get("kind"));
		if ("redis".equals(kind) || StringUtils.hasText(string(source.get("error")))) {
			return "";
		}
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> tables = (List<Map<String, Object>>) source.get("tables");
		if (tables == null || tables.isEmpty()) {
			return "";
		}
		StringBuilder text = new StringBuilder();
		text.append(source.get("name")).append(" 字段，脚本 ").append(source.get("script")).append("：\n");
		for (Map<String, Object> table : tables) {
			String line = columnLine(table);
			if (text.length() + line.length() > budget) {
				text.append("其余表的字段未展开，表名见上方清单，不要编造字段。\n");
				break;
			}
			text.append(line);
		}
		text.append("\n");
		return text.toString();
	}

	private String columnLine(Map<String, Object> table) {
		StringBuilder text = new StringBuilder();
		text.append("- ").append(table.get("name"));
		if (StringUtils.hasText(string(table.get("comment")))) {
			text.append("（").append(table.get("comment")).append("）");
		}
		text.append("：");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> columns = (List<Map<String, Object>>) table.get("columns");
		int shown = columns == null ? 0 : Math.min(columns.size(), 24);
		for (int i = 0; i < shown; i++) {
			if (i > 0) {
				text.append(", ");
			}
			Map<String, Object> column = columns.get(i);
			text.append(column.get("name"));
			if (StringUtils.hasText(string(column.get("type")))) {
				text.append(" ").append(column.get("type"));
			}
			if (Boolean.TRUE.equals(column.get("primary"))) {
				text.append(" PK");
			}
		}
		if (columns != null && columns.size() > shown) {
			text.append(" …等").append(columns.size()).append("列");
		}
		text.append("\n");
		return text.toString();
	}

	private static int columnOrder(Map<String, Object> source) {
		String kind = string(source.get("kind"));
		if ("clickhouse".equals(kind) || "doris".equals(kind)) {
			return 0;
		}
		if ("redis".equals(kind)) {
			return 9;
		}
		if (StringUtils.hasText(string(source.get("key")))) {
			return 1;
		}
		return 8;
	}

	private List<Map<String, Object>> load() {
		List<Map<String, Object>> sources = new ArrayList<>();
		if (dynamicDataSource != null && !dynamicDataSource.isEmpty()) {
			ensureClickHouse();
			for (DataSourceNode node : dynamicDataSource.datasourceNodes()) {
				sources.add(readJdbc(node));
			}
		}
		Map<String, Object> redis = readRedis();
		if (redis != null) {
			sources.add(redis);
		}
		return sources;
	}

	private void ensureClickHouse() {
		if (hasKey("clickhouse") || environment == null) {
			return;
		}
		String url = environment.getProperty("clickhouse.address");
		if (!StringUtils.hasText(url)) {
			return;
		}
		DataSource dataSource = openClickHouse(url.trim());
		if (dataSource == null) {
			return;
		}
		dynamicDataSource.add("clickhouse", dataSource);
		logger.info("已把 ClickHouse 注册为 Magic 数据源 clickhouse，脚本使用 db.clickhouse.select");
	}

	private boolean hasKey(String key) {
		for (DataSourceNode node : dynamicDataSource.datasourceNodes()) {
			if (key.equals(node.getKey())) {
				return true;
			}
		}
		return false;
	}

	private DataSource openClickHouse(String url) {
		ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "magic-ai-clickhouse");
			thread.setDaemon(true);
			return thread;
		});
		try {
			Future<DataSource> future = executor.submit(() -> createClickHouse(url));
			return future.get(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			logger.warn("ClickHouse 数据源暂不可用: {}", e.getMessage());
			return null;
		} finally {
			executor.shutdownNow();
		}
	}

	private DataSource createClickHouse(String url) throws Exception {
		Class<?> clazz = Class.forName("com.clickhouse.jdbc.ClickHouseDataSource");
		Properties properties = new Properties();
		properties.setProperty("user", environment.getProperty("clickhouse.username", "default"));
		String password = environment.getProperty("clickhouse.password");
		if (password != null) {
			properties.setProperty("password", password);
		}
		String database = environment.getProperty("clickhouse.db");
		if (StringUtils.hasText(database)) {
			properties.setProperty("database", database);
		}
		properties.setProperty("connection_timeout", "5000");
		properties.setProperty("socket_timeout", "8000");
		DataSource dataSource = (DataSource) clazz.getConstructor(String.class, Properties.class).newInstance(url, properties);
		try (Connection connection = dataSource.getConnection()) {
			if (!connection.isValid(3)) {
				throw new IllegalStateException("连接无效");
			}
		}
		return dataSource;
	}

	private Map<String, Object> readJdbc(DataSourceNode node) {
		String key = node.getKey();
		boolean primary = !StringUtils.hasText(key);
		Map<String, Object> source = new LinkedHashMap<>();
		source.put("id", primary ? "jdbc:default" : "jdbc:" + key);
		source.put("key", primary ? "" : key);
		source.put("name", primary ? "默认数据源" : key);
		if (StringUtils.hasText(node.getName()) && !node.getName().equals(key)) {
			source.put("title", node.getName());
		}
		String script = primary ? "db.select" : "db." + key + ".select";
		source.put("script", script);
		try (Connection connection = node.getDataSource().getConnection()) {
			String product = connection.getMetaData().getDatabaseProductName();
			String url = connection.getMetaData().getURL();
			String catalog = connection.getCatalog();
			String kind = kindOf(product, url);
			if (!StringUtils.hasText(catalog) && "clickhouse".equals(kind) && environment != null) {
				catalog = environment.getProperty("clickhouse.db");
			}
			source.put("kind", kind);
			source.put("product", product == null ? "" : product);
			source.put("database", catalog == null ? "" : catalog);
			source.put("host", hostOf(url));
			source.put("url", maskUrl(url));
			if (primary && "mysql".equals(kind)) {
				source.put("name", "MySQL" + (StringUtils.hasText(catalog) ? " / " + catalog : ""));
			} else if ("clickhouse".equals(kind)) {
				source.put("name", "ClickHouse" + (StringUtils.hasText(catalog) ? " / " + catalog : ""));
			} else if ("doris".equals(kind)) {
				source.put("name", "Doris" + (StringUtils.hasText(catalog) ? " / " + catalog : ""));
			}
			String sql = "clickhouse".equals(kind) ? CLICKHOUSE_SQL : MYSQL_SQL;
			List<Map<String, Object>> tables = readTables(connection, sql);
			if (tables.isEmpty() && "clickhouse".equals(kind)) {
				tables = readTables(connection, MYSQL_SQL);
			}
			source.put("tables", tables);
			source.put("example", "return " + script + "('select * from " + sampleTable(tables) + " limit 20');");
		} catch (Exception e) {
			source.put("kind", "jdbc");
			source.put("error", e.getMessage());
			source.put("tables", new ArrayList<>());
		}
		return source;
	}

	private Map<String, Object> readRedis() {
		if (environment == null) {
			return null;
		}
		String host = firstProperty("spring.data.redis.host", "spring.redis.host");
		if (!StringUtils.hasText(host)) {
			return null;
		}
		String port = firstProperty("spring.data.redis.port", "spring.redis.port");
		String database = firstProperty("spring.data.redis.database", "spring.redis.database");
		Map<String, Object> source = new LinkedHashMap<>();
		source.put("id", "redis");
		source.put("key", "redis");
		source.put("kind", "redis");
		source.put("name", "Redis / " + host);
		source.put("product", "Redis");
		source.put("host", host + (StringUtils.hasText(port) ? ":" + port : ""));
		source.put("database", StringUtils.hasText(database) ? database : "0");
		source.put("script", "redis");
		source.put("note", "Redis 没有表，不能写 SQL。只读命令：redis.scan(pattern)、redis.get(key)、redis.hget(key, field)、redis.hgetall(key)、redis.lrange(key, 0, 20)、redis.smembers(key)、redis.zrange(key, 0, 20)、redis.type(key)、redis.ttl(key)、redis.exists(key)。不要用 set、del。");
		source.put("example", "return redis.scan('*');");
		source.put("passwordSet", StringUtils.hasText(firstProperty("spring.data.redis.password", "spring.redis.password")));
		source.put("tables", new ArrayList<>());
		List<String> sampleKeys = sampleRedisKeys();
		if (!sampleKeys.isEmpty()) {
			source.put("sampleKeys", sampleKeys);
		}
		return source;
	}

	private List<String> sampleRedisKeys() {
		if (applicationContext == null) {
			return new ArrayList<>();
		}
		try {
			Class<?> templateType = Class.forName("org.springframework.data.redis.core.RedisTemplate");
			java.util.Map<String, ?> beans = applicationContext.getBeansOfType(templateType);
			if (beans.isEmpty()) {
				return new ArrayList<>();
			}
			Object template = beans.values().iterator().next();
			Class<?> scanOptionsType = Class.forName("org.springframework.data.redis.core.ScanOptions");
			Object builder = scanOptionsType.getMethod("scanOptions").invoke(null);
			builder = builder.getClass().getMethod("count", long.class).invoke(builder, 40L);
			Object options = builder.getClass().getMethod("build").invoke(builder);
			Object cursor = template.getClass().getMethod("scan", scanOptionsType).invoke(template, options);
			java.lang.reflect.Method hasNext = cursor.getClass().getMethod("hasNext");
			java.lang.reflect.Method next = cursor.getClass().getMethod("next");
			java.lang.reflect.Method type = template.getClass().getMethod("type", Object.class);
			List<String> keys = new ArrayList<>();
			try {
				while (keys.size() < 20 && Boolean.TRUE.equals(hasNext.invoke(cursor))) {
					String key = String.valueOf(next.invoke(cursor));
					String kind = "unknown";
					try {
						Object dataType = type.invoke(template, key);
						if (dataType != null) {
							kind = String.valueOf(dataType.getClass().getMethod("code").invoke(dataType));
						}
					} catch (ReflectiveOperationException ignored) {
						// 类型读不到时仍保留键名
					}
					keys.add(key + "(" + kind + ")");
				}
			} finally {
				try {
					cursor.getClass().getMethod("close").invoke(cursor);
				} catch (ReflectiveOperationException ignored) {
					// 游标没有 close 时由连接自己回收
				}
			}
			return keys;
		} catch (Throwable e) {
			logger.info("Redis 键样例暂不可读: {}", e.getMessage());
			return new ArrayList<>();
		}
	}

	private String firstProperty(String primary, String fallback) {
		String value = environment.getProperty(primary);
		return StringUtils.hasText(value) ? value : environment.getProperty(fallback);
	}

	private List<Map<String, Object>> readTables(Connection connection, String sql) throws Exception {
		Map<String, Map<String, Object>> tables = new LinkedHashMap<>();
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setQueryTimeout(8);
			try (ResultSet rows = statement.executeQuery()) {
				while (rows.next()) {
					String tableName = rows.getString(1);
					if (!StringUtils.hasText(tableName)) {
						continue;
					}
					Map<String, Object> table = tables.get(tableName);
					if (table == null) {
						table = new LinkedHashMap<>();
						table.put("name", tableName);
						table.put("comment", clean(rows.getString(2), 80));
						table.put("columns", new ArrayList<Map<String, Object>>());
						tables.put(tableName, table);
					}
					String columnName = rows.getString(3);
					if (!StringUtils.hasText(columnName)) {
						continue;
					}
					Map<String, Object> column = new LinkedHashMap<>();
					column.put("name", columnName);
					column.put("type", rows.getString(4) == null ? "" : rows.getString(4));
					column.put("comment", clean(rows.getString(5), 80));
					column.put("primary", "PRI".equalsIgnoreCase(rows.getString(6)));
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> columns = (List<Map<String, Object>>) table.get("columns");
					columns.add(column);
				}
			}
		}
		return new ArrayList<>(tables.values());
	}

	private static String sampleTable(List<Map<String, Object>> tables) {
		if (tables == null || tables.isEmpty()) {
			return "your_table";
		}
		return String.valueOf(tables.get(0).get("name"));
	}

	private static String kindOf(String product, String url) {
		String value = ((product == null ? "" : product) + " " + (url == null ? "" : url)).toLowerCase();
		if (value.contains("clickhouse")) {
			return "clickhouse";
		}
		if (value.contains("doris") || value.contains("selectdb")) {
			return "doris";
		}
		if (value.contains("mysql") || value.contains("mariadb")) {
			return "mysql";
		}
		if (value.contains("postgres")) {
			return "postgresql";
		}
		return "jdbc";
	}

	private static String hostOf(String url) {
		if (!StringUtils.hasText(url)) {
			return "";
		}
		int scheme = url.indexOf("://");
		if (scheme < 0) {
			return "";
		}
		String rest = url.substring(scheme + 3);
		int end = rest.length();
		int slash = rest.indexOf('/');
		int query = rest.indexOf('?');
		if (slash >= 0) {
			end = Math.min(end, slash);
		}
		if (query >= 0) {
			end = Math.min(end, query);
		}
		return rest.substring(0, end);
	}

	private static String maskUrl(String url) {
		if (!StringUtils.hasText(url)) {
			return "";
		}
		return url.replaceAll("(?i)(password=)[^&]*", "$1***");
	}

	private static String clean(String value, int max) {
		if (value == null) {
			return "";
		}
		String text = value.replace('\n', ' ').replace('\r', ' ').trim();
		return text.length() > max ? text.substring(0, max) : text;
	}

	private static String string(Object value) {
		return value == null ? "" : String.valueOf(value);
	}
}
