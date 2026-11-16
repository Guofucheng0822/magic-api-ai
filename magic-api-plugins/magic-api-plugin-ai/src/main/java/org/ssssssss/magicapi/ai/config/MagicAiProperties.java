package org.ssssssss.magicapi.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI配置属性
 */
@ConfigurationProperties(prefix = "magic-api.ai")
public class MagicAiProperties {

    /**
     * 是否启用AI功能
     */
    private boolean enabled = false;

    /**
     * 默认活跃提供商（无持久化选择时使用）
     */
    private String activeProvider = "openai";

    /**
     * 默认活跃模型（无持久化选择时使用）
     */
    private String activeModel = "";

    /**
     * 多提供商配置，key为提供商名称（如 deepseek、openai、dashscope、zhipu、minimax）
     */
    private Map<String, ProviderConfig> providers = new LinkedHashMap<>();

    // --- 向下兼容：旧的单提供商字段 ---

    /**
     * AI服务提供商类型：openai, deepseek, dashscope, zhipu, minimax
     */
    private String provider = "openai";

    /**
     * API地址
     */
    private String apiUrl = "https://api.openai.com/v1/chat/completions";

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 使用的模型
     */
    private String model = "gpt-3.5-turbo";

    /**
     * 获取有效的提供商配置。
     * 如果配置了 providers map 则直接返回；
     * 否则从旧的单提供商字段合成一个单条目 map，实现向下兼容。
     */
    public Map<String, ProviderConfig> getEffectiveProviders() {
        if (!providers.isEmpty()) {
            return providers;
        }
        if (provider != null && apiKey != null) {
            ProviderConfig config = new ProviderConfig();
            config.setApiUrl(apiUrl);
            config.setApiKey(apiKey);
            config.setModel(model);
            Map<String, ProviderConfig> map = new LinkedHashMap<>();
            map.put(provider.toLowerCase(), config);
            return map;
        }
        return Collections.emptyMap();
    }

    // --- Getters / Setters ---

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getActiveProvider() {
        return activeProvider;
    }

    public void setActiveProvider(String activeProvider) {
        this.activeProvider = activeProvider;
    }

    public String getActiveModel() {
        return activeModel;
    }

    public void setActiveModel(String activeModel) {
        this.activeModel = activeModel;
    }

    public Map<String, ProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, ProviderConfig> providers) {
        this.providers = providers;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 单个提供商的配置
     */
    public static class ProviderConfig {

        private String label;

        private String apiUrl;

        private String apiKey;

        private String model;

        private List<String> models = new ArrayList<>();

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<String> getModels() {
            return models;
        }

        public void setModels(List<String> models) {
            this.models = models;
        }
    }
}
