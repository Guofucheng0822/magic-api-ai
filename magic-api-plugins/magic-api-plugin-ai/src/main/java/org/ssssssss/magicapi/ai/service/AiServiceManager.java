package org.ssssssss.magicapi.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.ai.config.MagicAiProperties;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI服务管理器，支持按请求动态选择提供商和模型
 */
public class AiServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceManager.class);

    private final Map<String, MagicAiProperties.ProviderConfig> providerConfigs;

    private final ConcurrentHashMap<String, AiService> serviceCache = new ConcurrentHashMap<>();

    public AiServiceManager(MagicAiProperties properties) {
        this.providerConfigs = properties.getEffectiveProviders();
        logger.info("AI服务管理器初始化，已配置提供商: {}", providerConfigs.keySet());
    }

    /**
     * 根据请求指定的提供商和模型获取AI服务实例
     */
    public AiService getService(String provider, String model) {
        if (provider == null || provider.isEmpty()) {
            // 使用第一个配置的提供商作为默认
            provider = providerConfigs.keySet().iterator().next();
        }
        String cacheKey = provider + ":" + (model != null ? model : "");
        String finalProvider = provider;
        String finalModel = model;
        return serviceCache.computeIfAbsent(cacheKey, k -> createService(finalProvider, finalModel));
    }

    /**
     * 获取所有已配置的提供商信息（名称、标签、可选模型列表）
     */
    public List<Map<String, Object>> getConfiguredProviders() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, MagicAiProperties.ProviderConfig> entry : providerConfigs.entrySet()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("key", entry.getKey());
            MagicAiProperties.ProviderConfig config = entry.getValue();
            info.put("label", config.getLabel() != null ? config.getLabel() : entry.getKey());
            info.put("models", config.getModels() != null ? config.getModels() : Collections.emptyList());
            result.add(info);
        }
        return result;
    }

    private AiService createService(String provider, String model) {
        MagicAiProperties.ProviderConfig config = providerConfigs.get(provider);
        if (config == null) {
            throw new IllegalStateException("未找到提供商配置: " + provider);
        }
        String apiUrl = config.getApiUrl();
        String apiKey = config.getApiKey();
        // 优先使用请求指定的模型，否则使用提供商默认模型
        String effectiveModel = (model != null && !model.isEmpty()) ? model : config.getModel();

        logger.info("创建AI服务实例: 提供商={}, 模型={}", provider, effectiveModel);

        switch (provider.toLowerCase()) {
            case "dashscope":
            case "aliyun":
                return new DashScopeService(apiUrl, apiKey, effectiveModel);
            case "zhipu":
            case "glm":
                return new ZhipuAiService(apiUrl, apiKey, effectiveModel);
            case "minimax":
                return new MiniMaxService(apiUrl, apiKey, effectiveModel);
            case "deepseek":
                return new DeepSeekService(apiUrl, apiKey, effectiveModel);
            case "claude":
            case "anthropic":
                return new ClaudeService(apiUrl, apiKey, effectiveModel);
            case "openai":
            default:
                return new OpenAiService(apiUrl, apiKey, effectiveModel);
        }
    }
}
