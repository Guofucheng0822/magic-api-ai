package org.ssssssss.magicapi.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.ai.service.*;
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
        logger.info("Magic AI Coding 模块已启用，提供商: {}", properties.getProvider());
    }

    @Bean
    public AiService aiService() {
        String provider = properties.getProvider();
        String apiUrl = properties.getApiUrl();
        String apiKey = properties.getApiKey();
        String model = properties.getModel();

        logger.info("初始化AI服务，提供商: {}, 模型: {}", provider, model);

        switch (provider.toLowerCase()) {
            case "dashscope":
            case "aliyun":
                return new DashScopeService(apiUrl, apiKey, model);
            case "zhipu":
            case "glm":
                return new ZhipuAiService(apiUrl, apiKey, model);
            case "minimax":
                return new MiniMaxService(apiUrl, apiKey, model);
            case "deepseek":
                return new DeepSeekService(apiUrl, apiKey, model);
            case "openai":
            default:
                return new OpenAiService(apiUrl, apiKey, model);
        }
    }

    @Bean
    public MagicAiController magicAiController(MagicConfiguration configuration, AiService aiService) {
        return new MagicAiController(configuration, aiService);
    }

    @Override
    public Plugin plugin() {
        return new Plugin("AI Coding", "MagicAI", "magic-ai.1.0.0.iife.js");
    }

    @Override
    public MagicControllerRegister controllerRegister() {
        return (mapping, configuration) -> {
            MagicAiController controller = magicAiController(configuration, aiService());
            mapping.registerController(controller);
            logger.info("AI Coding 控制器已注册");
        };
    }
}
