package com.example.todolist.service.impl;

import com.example.todolist.dto.AiDecomposeResponse;
import com.example.todolist.service.AiDecomposeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class AiDecomposeServiceImpl implements AiDecomposeService {

    private static final int TITLE_MAX_LENGTH = 255;
    private static final int MIN_SUBTASKS = 5;
    private static final int MAX_SUBTASKS = 8;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.base-url:}")
    private String aiBaseUrl;

    @Value("${ai.api-key:}")
    private String aiApiKey;

    @Value("${ai.model:}")
    private String aiModel;

    @Value("${ai.timeout-ms:30000}")
    private long aiTimeoutMs;

    @Override
    public AiDecomposeResponse decompose(String goalTitle, LocalDateTime dueDate) {
        String normalizedGoal = normalizeTitle(goalTitle, "goalTitle");
        validateConfig();

        try {
            String rawContent = callUpstream(normalizedGoal, dueDate);
            Map<String, Object> payload = parseJsonObject(rawContent);

            String parentTitle = normalizeTitle(stringOrNull(payload.get("parentTitle")), "parentTitle");
            List<String> subtasks = parseSubtasks(payload.get("subtasks"));

            if (subtasks.size() < MIN_SUBTASKS || subtasks.size() > MAX_SUBTASKS) {
                throw new AiUpstreamException("Subtask count is out of range (5-8)");
            }

            return new AiDecomposeResponse(parentTitle, subtasks);
        } catch (AiUpstreamException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUpstreamException("Decompose failed, please retry", e);
        }
    }

    private String callUpstream(String goalTitle, LocalDateTime dueDate) {
        String url = aiBaseUrl.endsWith("/") ? aiBaseUrl + "chat/completions" : aiBaseUrl + "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiModel);
        requestBody.put("temperature", 0.4);
        requestBody.put("max_tokens", 1200);
        requestBody.put("messages", buildMessages(goalTitle, dueDate));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiApiKey);

        RestTemplate restTemplate = createRestTemplate();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );
        } catch (RestClientException e) {
            throw new AiUpstreamException("Failed to call AI service", e);
        }

        if (response.getStatusCode().isError()) {
            throw new AiUpstreamException("AI service returned error status: " + response.getStatusCode().value());
        }

        String body = response.getBody();
        if (body == null || body.trim().isEmpty()) {
            throw new AiUpstreamException("AI service returned empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new AiUpstreamException("AI response missing choices");
            }

            String content = choices.get(0).path("message").path("content").asText(null);
            if (content == null || content.trim().isEmpty()) {
                throw new AiUpstreamException("AI response missing content");
            }
            return content;
        } catch (AiUpstreamException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUpstreamException("Failed to parse AI response", e);
        }
    }

    private List<Map<String, String>> buildMessages(String goalTitle, LocalDateTime dueDate) {
        String systemPrompt = "You are a task decomposition assistant. Break the goal into 5 to 8 actionable subtasks."
                + "Return JSON only, without any extra explanation."
                + "Format: {\"parentTitle\":\"...\",\"subtasks\":[\"...\",\"...\"]}";

        StringBuilder userPrompt = new StringBuilder("Decompose this goal: ").append(goalTitle);
        if (dueDate != null) {
            userPrompt.append(". Due at: ")
                    .append(dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt));
        messages.add(message("user", userPrompt.toString()));
        return messages;
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private RestTemplate createRestTemplate() {
        int timeout = (int) Math.min(Math.max(aiTimeoutMs, 1000L), Integer.MAX_VALUE);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }

    private Map<String, Object> parseJsonObject(String rawContent) {
        try {
            String json = extractJson(rawContent);
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new AiUpstreamException("AI response is not valid JSON");
        }
    }

    private String extractJson(String rawContent) {
        String content = rawContent.trim();
        if (content.startsWith("```")) {
            int firstNewline = content.indexOf('\n');
            int lastFence = content.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                content = content.substring(firstNewline + 1, lastFence).trim();
            }
        }

        int firstBrace = content.indexOf('{');
        int lastBrace = content.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return content.substring(firstBrace, lastBrace + 1);
        }
        return content;
    }

    private List<String> parseSubtasks(Object subtasksObj) {
        if (!(subtasksObj instanceof List<?>)) {
            throw new AiUpstreamException("AI response missing subtasks array");
        }
        List<?> list = (List<?>) subtasksObj;

        List<String> normalized = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof String)) {
                continue;
            }
            normalized.add(normalizeTitle((String) item, "subtask"));
        }

        List<String> deduplicated = new ArrayList<>(new LinkedHashSet<>(normalized));
        if (deduplicated.size() < MIN_SUBTASKS || deduplicated.size() > MAX_SUBTASKS) {
            throw new AiUpstreamException("Subtask count is out of range (5-8)");
        }
        return deduplicated;
    }

    private String normalizeTitle(String title, String fieldName) {
        if (title == null) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        String normalized = title.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        if (normalized.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException(fieldName + " length exceeds 255");
        }
        return normalized;
    }

    private String stringOrNull(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private void validateConfig() {
        if (aiBaseUrl == null || aiBaseUrl.trim().isEmpty()) {
            throw new AiUpstreamException("AI_BASE_URL is missing");
        }
        if (aiApiKey == null || aiApiKey.trim().isEmpty()) {
            throw new AiUpstreamException("AI_API_KEY is missing");
        }
        if (aiModel == null || aiModel.trim().isEmpty()) {
            throw new AiUpstreamException("AI_MODEL is missing");
        }
        if (aiTimeoutMs <= 0) {
            aiTimeoutMs = 30000L;
        }
    }
}
