package org.ssssssss.magicapi.clickhouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 按 clickhouse.address 注册名为 clickhouse 的数据源，脚本里使用 db.clickhouse.select。
 */
@Configuration
public class ClickHousePluginConfiguration implements MagicPluginConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(ClickHousePluginConfiguration.class);

	public ClickHousePluginConfiguration(Environment environment, MagicDynamicDataSource dataSource) {
		String address = environment.getProperty("clickhouse.address");
		if (address == null || address.trim().isEmpty()) {
			logger.info("未配置 clickhouse.address，跳过 ClickHouse 数据源");
			return;
		}
		if (dataSource.contains("clickhouse")) {
			logger.info("ClickHouse 数据源已存在，跳过重复注册");
			return;
		}
		try {
			dataSource.add("clickhouse", createDataSource(environment, address.trim()));
			logger.info("已注册 ClickHouse 数据源: clickhouse");
		} catch (Exception e) {
			logger.warn("注册 ClickHouse 数据源失败: {}", e.getMessage());
		}
	}

	private DataSource createDataSource(Environment environment, String address) throws Exception {
		Properties properties = new Properties();
		String username = environment.getProperty("clickhouse.username", "default");
		String password = environment.getProperty("clickhouse.password", "");
		String database = environment.getProperty("clickhouse.db", "");
		properties.setProperty("user", username == null ? "default" : username);
		properties.setProperty("password", password == null ? "" : password);
		if (database != null && !database.trim().isEmpty()) {
			properties.setProperty("database", database.trim());
		}
		Class<?> type = Class.forName("com.clickhouse.jdbc.ClickHouseDataSource");
		return (DataSource) type.getConstructor(String.class, Properties.class).newInstance(address, properties);
	}

	@Override
	public Plugin plugin() {
		return new Plugin("ClickHouse");
	}
}
