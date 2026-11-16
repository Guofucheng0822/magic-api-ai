package org.ssssssss.magicapi.ai.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.ai.model.AiChatRequest;
import org.ssssssss.magicapi.ai.model.AiChatResponse;
import org.ssssssss.magicapi.ai.service.AiService;
import org.ssssssss.magicapi.ai.service.AiServiceManager;
import org.ssssssss.magicapi.ai.service.ApiCreateService;
import org.ssssssss.magicapi.ai.service.DatasourceSchemaService;
import org.ssssssss.magicapi.ai.service.ScriptQueryService;
import org.ssssssss.magicapi.core.annotation.Valid;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI Coding 控制器
 */
public class MagicAiController extends MagicController implements MagicExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(MagicAiController.class);

    private final AiServiceManager aiServiceManager;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MagicAiController(MagicConfiguration configuration, AiServiceManager aiServiceManager) {
        super(configuration);
        this.aiServiceManager = aiServiceManager;
    }

    /**
     * AI对话接口（同步），provider和model由前端每次请求携带
     */
    @PostMapping("/ai/chat")
    @ResponseBody
    @Valid
    public JsonBean<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return new JsonBean<>(400, "消息内容不能为空");
        }
        AiChatResponse response = aiServiceManager.getService(request.getProvider(), request.getModel()).chat(request);
        return new JsonBean<>(response);
    }

    /**
     * AI对话接口（流式SSE），逐块推送AI回复
     */
    @PostMapping(value = "/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    @Valid
    public SseEmitter chatStream(@RequestBody AiChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        executor.execute(() -> {
            try {
                AiService service = aiServiceManager.getService(request.getProvider(), request.getModel());
                service.chatStream(request, token -> {
                    try {
                        // 手动 JSON 编码，确保换行符等被转义为 \n
                        // StringHttpMessageConverter 优先级高于 Jackson，直接写已编码的字符串
                        String jsonToken = objectMapper.writeValueAsString(token);
                        emitter.send(SseEmitter.event().data(jsonToken));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                logger.error("流式AI服务调用异常", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                    emitter.complete();
                } catch (IOException ignored) {
                    emitter.completeWithError(e);
                }
            }
        });
        return emitter;
    }

    /**
     * 获取已配置的提供商列表（含模型列表），供前端选择器使用
     */
    @PostMapping("/ai/config/get")
    @ResponseBody
    @Valid
    public JsonBean<List<Map<String, Object>>> getProviders() {
        return new JsonBean<>(aiServiceManager.getConfiguredProviders());
    }

    @GetMapping("/ai/datasources")
    @ResponseBody
    @Valid
    public JsonBean<List<Map<String, String>>> datasources() {
        DatasourceSchemaService service = DatasourceSchemaService.get();
        if (service == null) {
            return new JsonBean<>(java.util.Collections.emptyList());
        }
        return new JsonBean<>(service.optionalSources());
    }

    @GetMapping("/ai/schema")
    @ResponseBody
    @Valid
    public JsonBean<List<Map<String, Object>>> schema() {
        DatasourceSchemaService service = DatasourceSchemaService.get();
        if (service == null) {
            return new JsonBean<>(-1, "表结构服务未就绪");
        }
        return new JsonBean<>(service.describe());
    }

    @PostMapping("/ai/query")
    @ResponseBody
    @Valid
    public JsonBean<Map<String, Object>> query(@RequestBody Map<String, String> body) {
        try {
            return new JsonBean<>(ScriptQueryService.run(body == null ? null : body.get("script")));
        } catch (Exception e) {
            logger.warn("AI 查询失败: {}", e.getMessage());
            return new JsonBean<>(-1, e.getMessage());
        }
    }

    @PostMapping("/ai/export")
    @Valid
    public ResponseEntity<byte[]> export(@RequestBody Map<String, String> body) {
        try {
            byte[] csv = ScriptQueryService.exportCsv(body == null ? null : body.get("script"));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"query.csv\"")
                    .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                    .body(csv);
        } catch (Exception e) {
            logger.warn("AI 导出失败: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage() == null ? "导出失败".getBytes(java.nio.charset.StandardCharsets.UTF_8) : e.getMessage().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    @PostMapping("/ai/create")
    @ResponseBody
    @Valid
	public JsonBean<Map<String, String>> create(@RequestBody Map<String, Object> body) {
        try {
            return new JsonBean<>(ApiCreateService.get().create(body));
        } catch (Exception e) {
            logger.warn("新建接口失败: {}", e.getMessage());
            return new JsonBean<>(-1, e.getMessage());
        }
    }
}
