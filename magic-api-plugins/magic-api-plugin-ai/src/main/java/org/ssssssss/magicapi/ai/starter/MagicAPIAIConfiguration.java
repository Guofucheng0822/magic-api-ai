package org.ssssssss.magicapi.ai.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.ssssssss.magicapi.ai.service.DatabaseSchemaCatalog;
import org.ssssssss.magicapi.ai.service.MagicAIService;
import org.ssssssss.magicapi.ai.service.OpenAICompatibleClient;
import org.ssssssss.magicapi.ai.web.MagicAIController;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;

@Configuration
@EnableConfigurationProperties(MagicAIConfig.class)
@ConditionalOnProperty(prefix = "magic-api.ai", name = "enable", havingValue = "true", matchIfMissing = true)
public class MagicAPIAIConfiguration implements MagicPluginConfiguration {

	private final MagicAIConfig config;

	private MagicAIService magicAIService;

	public MagicAPIAIConfiguration(MagicAIConfig config) {
		this.config = config;
	}

	@Bean
	public OpenAICompatibleClient openAICompatibleClient() {
		return new OpenAICompatibleClient(config);
	}

	@Bean
	public DatabaseSchemaCatalog databaseSchemaCatalog(MagicDynamicDataSource magicDynamicDataSource, Environment environment, ApplicationContext applicationContext) {
		return new DatabaseSchemaCatalog(config, magicDynamicDataSource, environment, applicationContext);
	}

	@Bean
	public MagicAIService magicAIService(OpenAICompatibleClient openAICompatibleClient, DatabaseSchemaCatalog databaseSchemaCatalog) {
		this.magicAIService = new MagicAIService(config, openAICompatibleClient, databaseSchemaCatalog);
		return this.magicAIService;
	}

	@Override
	public Plugin plugin() {
		return new Plugin("AI助手", "MagicAI", "magic-ai.1.0.0.iife.js");
	}

	@Override
	public MagicControllerRegister controllerRegister() {
		return (mapping, configuration) ->
				mapping.registerController(new MagicAIController(configuration, magicAIService));
	}
}
