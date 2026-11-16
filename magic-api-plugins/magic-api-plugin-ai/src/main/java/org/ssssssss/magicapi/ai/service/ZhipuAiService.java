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
 * 智谱AI（GLM）服务实现
 */
public class ZhipuAiService implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(ZhipuAiService.class);

    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    public ZhipuAiService(String apiUrl, String apiKey, String model) {
        this.apiUrl = apiUrl != null && !apiUrl.isEmpty() ? apiUrl : "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        this.apiKey = apiKey;
        this.model = model != null && !model.isEmpty() ? model : "glm-4";
        this.restTemplate = new RestTemplate();
    }

    private ArrayNode buildMessages(AiChatRequest request) {
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", buildSystemPrompt(request));
        if (request.getCurrentCode() != null && !request.getCurrentCode().trim().isEmpty()) {
            ObjectNode codeContextMsg = messages.addObject();
            codeContextMsg.put("role", "system");
            codeContextMsg.put("content", "当前编辑器中的代码：\n```magicscript\n" + request.getCurrentCode() + "\n```\n请在回答时结合以上代码上下文。");
        }
        List<AiChatMessage> history = request.getHistory();
        if (history != null) {
            for (AiChatMessage msg : history) {
                ObjectNode historyMsg = messages.addObject();
                historyMsg.put("role", msg.getRole());
                historyMsg.put("content", msg.getContent());
            }
        }
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
            requestBody.put("stream", false);
            requestBody.set("messages", buildMessages(request));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCodeValue() != 200) {
                logger.error("智谱AI接口请求失败，状态码: {}", response.getStatusCodeValue());
                return new AiChatResponse("AI服务请求失败，状态码: " + response.getStatusCodeValue());
            }

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("");

            return new AiChatResponse(content);

        } catch (Exception e) {
            logger.error("智谱AI服务调用异常", e);
            return new AiChatResponse("AI服务调用异常: " + e.getMessage());
        }
    }

    @Override
    public void chatStream(AiChatRequest request, Consumer<String> onToken) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("stream", true);
            requestBody.set("messages", buildMessages(request));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
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
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        JsonNode chunk = objectMapper.readTree(data);
                        String content = chunk.path("choices").path(0)
                                .path("delta").path("content").asText(null);
                        if (content != null) {
                            onToken.accept(content);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("智谱AI流式调用异常", e);
            onToken.accept("\n\nAI服务流式调用异常: " + e.getMessage());
        }
    }
}
