package org.ssssssss.magicapi.ai.service;

import org.ssssssss.magicapi.ai.model.AiChatMessage;
import org.ssssssss.magicapi.ai.model.AiChatRequest;
import org.ssssssss.magicapi.ai.model.AiChatResponse;

import java.util.List;
import java.util.function.Consumer;

/**
 * AI服务接口
 */
public interface AiService {

    /**
     * 发送对话消息，获取AI回复（同步）
     */
    AiChatResponse chat(AiChatRequest request);

    /**
     * 流式对话，逐块推送AI回复内容
     * 默认实现降级为同步一次推送，子类可覆盖实现真正的流式
     */
    default void chatStream(AiChatRequest request, Consumer<String> onToken) {
        AiChatResponse resp = chat(request);
        onToken.accept(resp.getMessage());
    }

    /**
     * 构建系统提示词（根据用户消息动态匹配 Skill 模块）
     */
    default String buildSystemPrompt(String userMessage) {
        return SkillPromptLoader.buildPrompt(userMessage);
    }

    /**
     * 构建系统提示词（无参兼容版本，加载默认 Skill）
     */
    default String buildSystemPrompt() {
        return buildSystemPrompt("");
    }

    /**
     * 将历史消息列表转为带格式的字符串（供部分不支持多轮对话的API使用）
     */
    default String buildHistoryContext(List<AiChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (AiChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                sb.append("用户: ").append(msg.getContent()).append("\n");
            } else if ("assistant".equals(msg.getRole())) {
                sb.append("助手: ").append(msg.getContent()).append("\n");
            }
        }
        return sb.toString();
    }
}
