package org.ssssssss.magicapi.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.ssssssss.magicapi.ai.model.AiChatMessage;
import org.ssssssss.magicapi.ai.model.AiChatRequest;
import org.ssssssss.magicapi.ai.model.AiChatResponse;

import java.util.List;

/**
 * 阿里百炼（DashScope）AI服务实现
 */
public class DashScopeService implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(DashScopeService.class);

    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    public DashScopeService(String apiUrl, String apiKey, String model) {
        this.apiUrl = apiUrl != null && !apiUrl.isEmpty() ? apiUrl : "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        this.apiKey = apiKey;
        this.model = model != null && !model.isEmpty() ? model : "qwen-turbo";
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();

            ObjectNode model = requestBody.putObject("model");
            model.put("name", this.model);

            ObjectNode input = requestBody.putObject("input");
            ArrayNode messages = input.putArray("messages");

            // 系统提示词
            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", buildSystemPrompt());

            // 代码上下文
            if (request.getCurrentCode() != null && !request.getCurrentCode().trim().isEmpty()) {
                ObjectNode codeContextMsg = messages.addObject();
                codeContextMsg.put("role", "system");
                codeContextMsg.put("content", "当前编辑器中的代码：\n```magicscript\n" + request.getCurrentCode() + "\n```\n请在回答时结合以上代码上下文。");
            }

            // 历史消息
            List<AiChatMessage> history = request.getHistory();
            if (history != null) {
                for (AiChatMessage msg : history) {
                    ObjectNode historyMsg = messages.addObject();
                    historyMsg.put("role", msg.getRole());
                    historyMsg.put("content", msg.getContent());
                }
            }

            // 当前用户消息
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", request.getMessage());

            ObjectNode parameters = requestBody.putObject("parameters");
            parameters.put("result_format", "message");

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCodeValue() != 200) {
                logger.error("DashScope接口请求失败，状态码: {}", response.getStatusCodeValue());
                return new AiChatResponse("AI服务请求失败，状态码: " + response.getStatusCodeValue());
            }

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson
                    .path("output")
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("");

            return new AiChatResponse(content);

        } catch (Exception e) {
            logger.error("DashScope服务调用异常", e);
            return new AiChatResponse("AI服务调用异常: " + e.getMessage());
        }
    }
}
