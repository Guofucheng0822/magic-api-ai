package org.ssssssss.magicapi.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
     * AI服务提供商类型：openai, azure, custom
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
}
