package org.ssssssss.magicapi.selectdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;

import javax.sql.DataSource;

/**
 * 按 selectdb.address 注册名为 selectdb 的数据源。SelectDB 使用 MySQL 协议，脚本里写 db.selectdb.select。
 */
@Configuration
public class SelectDbPluginConfiguration implements MagicPluginConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(SelectDbPluginConfiguration.class);

	public SelectDbPluginConfiguration(Environment environment, MagicDynamicDataSource dataSource) {
		String address = environment.getProperty("selectdb.address");
		if (address == null || address.trim().isEmpty()) {
			logger.info("未配置 selectdb.address，跳过 SelectDB 数据源");
			return;
		}
		if (dataSource.contains("selectdb")) {
			logger.info("SelectDB 数据源已存在，跳过重复注册");
			return;
		}
		try {
			dataSource.add("selectdb", createDataSource(environment, address.trim()));
			logger.info("已注册 SelectDB 数据源: selectdb");
		} catch (Exception e) {
			logger.warn("注册 SelectDB 数据源失败: {}", e.getMessage());
		}
	}

	private DataSource createDataSource(Environment environment, String address) throws Exception {
		String username = environment.getProperty("selectdb.username", "admin");
		String password = environment.getProperty("selectdb.password", "");
		String database = environment.getProperty("selectdb.db", "");
		String url = jdbcUrl(address, database);
		Class<?> type = Class.forName("com.mysql.cj.jdbc.MysqlDataSource");
		Object source = type.getDeclaredConstructor().newInstance();
		type.getMethod("setUrl", String.class).invoke(source, url);
		type.getMethod("setUser", String.class).invoke(source, username == null ? "admin" : username);
		type.getMethod("setPassword", String.class).invoke(source, password == null ? "" : password);
		return (DataSource) source;
	}

	static String jdbcUrl(String address, String database) {
		String url = address.trim();
		if (!url.startsWith("jdbc:")) {
			url = "jdbc:mysql://" + url;
		}
		String db = database == null ? "" : database.trim();
		if (!db.isEmpty() && !hasDatabase(url)) {
			int query = url.indexOf('?');
			if (query < 0) {
				url = url + "/" + db;
			} else {
				url = url.substring(0, query) + "/" + db + url.substring(query);
			}
		}
		if (!url.toLowerCase().contains("usessl=")) {
			url = url + (url.contains("?") ? "&" : "?") + "useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai";
		}
		return url;
	}

	private static boolean hasDatabase(String url) {
		int scheme = url.indexOf("://");
		int start = scheme < 0 ? 0 : scheme + 3;
		int query = url.indexOf('?', start);
		int slash = url.indexOf('/', start);
		if (slash < 0 || (query >= 0 && slash > query)) {
			return false;
		}
		String path = query < 0 ? url.substring(slash + 1) : url.substring(slash + 1, query);
		return !path.isEmpty();
	}

	@Override
	public Plugin plugin() {
		return new Plugin("SelectDB");
	}
}
