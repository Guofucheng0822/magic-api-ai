package org.ssssssss.magicapi.ai.web;

import org.ssssssss.magicapi.ai.model.AiChatRequest;
import org.ssssssss.magicapi.ai.model.AiChatResponse;
import org.ssssssss.magicapi.ai.service.AiService;
import org.ssssssss.magicapi.core.annotation.Valid;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * AI Coding 控制器
 */
public class MagicAiController extends MagicController implements MagicExceptionHandler {

    private final AiService aiService;

    public MagicAiController(MagicConfiguration configuration, AiService aiService) {
        super(configuration);
        this.aiService = aiService;
    }

    /**
     * AI对话接口
     */
    @PostMapping("/ai/chat")
    @ResponseBody
    @Valid
    public JsonBean<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return new JsonBean<>(400, "消息内容不能为空");
        }
        AiChatResponse response = aiService.chat(request);
        return new JsonBean<>(response);
    }
}
