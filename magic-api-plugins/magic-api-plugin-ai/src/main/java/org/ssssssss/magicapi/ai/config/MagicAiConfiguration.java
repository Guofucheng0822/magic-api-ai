package org.ssssssss.magicapi.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.ai.service.AiServiceManager;
import org.ssssssss.magicapi.ai.web.MagicAiController;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;

/**
 * AI模块配置类
 */
@Configuration
@EnableConfigurationProperties(MagicAiProperties.class)
@ConditionalOnProperty(prefix = "magic-api.ai", name = "enabled", havingValue = "true")
public class MagicAiConfiguration implements MagicPluginConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MagicAiConfiguration.class);

    private final MagicAiProperties properties;

    public MagicAiConfiguration(MagicAiProperties properties) {
        this.properties = properties;
        logger.info("Magic AI Coding 模块已启用");
    }

    @Bean
    public AiServiceManager aiServiceManager() {
        return new AiServiceManager(properties);
    }

    @Bean
    public MagicAiController magicAiController(MagicConfiguration configuration, AiServiceManager aiServiceManager) {
        return new MagicAiController(configuration, aiServiceManager);
    }

    @Override
    public Plugin plugin() {
        return new Plugin("AI Coding", "MagicAI", "magic-ai.1.0.0.iife.js");
    }

    @Override
    public MagicControllerRegister controllerRegister() {
        return (mapping, configuration) -> {
            MagicAiController controller = magicAiController(configuration, aiServiceManager());
            mapping.registerController(controller);
            logger.info("AI Coding 控制器已注册");
        };
    }
}
