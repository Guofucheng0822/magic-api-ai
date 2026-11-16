package org.ssssssss.magicapi.ai.model;

/**
 * AI模型选择，用于运行时切换提供商和模型
 */
public class AiModelSelection {

    private String provider;

    private String model;

    public AiModelSelection() {
    }

    public AiModelSelection(String provider, String model) {
        this.provider = provider;
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
