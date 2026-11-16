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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

/**
 * Claude (Anthropic) AI服务实现
 */
public class ClaudeService implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(ClaudeService.class);

    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    public ClaudeService(String apiUrl, String apiKey, String model) {
        this.apiUrl = apiUrl != null && !apiUrl.isEmpty() ? apiUrl : "https://api.anthropic.com/v1/messages";
        this.apiKey = apiKey;
        this.model = model != null && !model.isEmpty() ? model : "claude-sonnet-4-20250514";
        this.restTemplate = new RestTemplate();
    }

    private String buildSystemPromptFull(AiChatRequest request) {
        StringBuilder systemPrompt = new StringBuilder(buildSystemPrompt(request));
        if (request.getCurrentCode() != null && !request.getCurrentCode().trim().isEmpty()) {
            systemPrompt.append("\n\n当前编辑器中的代码：\n```magicscript\n")
                    .append(request.getCurrentCode())
                    .append("\n```\n请在回答时结合以上代码上下文。");
        }
        return systemPrompt.toString();
    }

    private ArrayNode buildMessages(AiChatRequest request) {
        ArrayNode messages = objectMapper.createArrayNode();
        // 历史消息（Claude messages 只支持 user 和 assistant）
        List<AiChatMessage> history = request.getHistory();
        if (history != null) {
            for (AiChatMessage msg : history) {
                String role = msg.getRole();
                if ("user".equals(role) || "assistant".equals(role)) {
                    ObjectNode historyMsg = messages.addObject();
                    historyMsg.put("role", role);
                    historyMsg.put("content", msg.getContent());
                }
            }
        }
        // 当前用户消息
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", request.getMessage());
        return messages;
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 4096);
            requestBody.put("system", buildSystemPromptFull(request));
            requestBody.set("messages", buildMessages(request));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCodeValue() != 200) {
                logger.error("Claude接口请求失败，状态码: {}", response.getStatusCodeValue());
                return new AiChatResponse("AI服务请求失败，状态码: " + response.getStatusCodeValue());
            }

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            // Claude 响应格式: { "content": [{"type": "text", "text": "..."}] }
            JsonNode contentArray = responseJson.path("content");
            StringBuilder contentBuilder = new StringBuilder();
            if (contentArray.isArray()) {
                for (JsonNode block : contentArray) {
                    if ("text".equals(block.path("type").asText())) {
                        contentBuilder.append(block.path("text").asText(""));
                    }
                }
            }

            return new AiChatResponse(contentBuilder.toString());

        } catch (Exception e) {
            logger.error("Claude服务调用异常", e);
            return new AiChatResponse("AI服务调用异常: " + e.getMessage());
        }
    }

    @Override
    public void chatStream(AiChatRequest request, Consumer<String> onToken) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 4096);
            requestBody.put("stream", true);
            requestBody.put("system", buildSystemPromptFull(request));
            requestBody.set("messages", buildMessages(request));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setRequestProperty("Accept", "text/event-stream");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        JsonNode chunk = objectMapper.readTree(data);
                        String type = chunk.path("type").asText("");

                        if ("content_block_delta".equals(type)) {
                            String text = chunk.path("delta").path("text").asText(null);
                            if (text != null) {
                                onToken.accept(text);
                            }
                        } else if ("message_stop".equals(type)) {
                            break;
                        } else if ("error".equals(type)) {
                            String errMsg = chunk.path("error").path("message").asText("未知错误");
                            logger.error("Claude流式错误: {}", errMsg);
                            onToken.accept("\n\nClaude错误: " + errMsg);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Claude流式调用异常", e);
            onToken.accept("\n\nAI服务流式调用异常: " + e.getMessage());
        }
    }
}
