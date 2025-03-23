package com.levelUp2.project_scaffolding_server.db.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelUp2.project_scaffolding_server.db.entity.GeminiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AiTemplateService {
//    private static final Logger logger = LoggerFactory.getLogger(GeminiCodeGenerator.class);


    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Autowired
    private ObjectMapper objectMapper;

    public ResponseEntity<GeminiResponse> generateCode(String prompt) throws IOException, InterruptedException {
        String apiUrlWithKey = String.format("%s?key=%s", apiUrl, apiKey);

        Map<String, Object> requestBodyMap = new HashMap<>();
        Map<String, String> partsMap = new HashMap<>();
        partsMap.put("text", prompt);
        List<Map<String, String>> partsList = List.of(partsMap);

        Map<String, List<Map<String, String>>> contentsMap = new HashMap<>();
        contentsMap.put("parts", partsList);

        requestBodyMap.put("contents", List.of(contentsMap));

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(requestBodyMap);
        } catch (IOException e) {
//            logger.error("Error serializing request body to JSON: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GeminiResponse(null, null, false, "Failed to serialize request body to JSON: " + e.getMessage()));
        }

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrlWithKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(java.time.Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
//                logger.debug("Gemini API Response: {}", responseBody);

                try {
                    JsonNode root = objectMapper.readTree(responseBody);
//                    JsonNode candidates = root.get("candidates");

                    Optional<String> generatedCode = Optional.empty();

                    return Optional.ofNullable(root)
                            .map(r -> r.get("candidates"))
                            .filter(JsonNode::isArray)
                            .filter(candidates -> !candidates.isEmpty())
                            .map(candidates -> candidates.get(0).get("content"))
                            .map(content -> content.get("parts"))
                            .filter(JsonNode::isArray)
                            .filter(parts -> !parts.isEmpty())
                            .map(parts -> parts.get(0).get("text"))
                            .filter(JsonNode::isTextual)
                            .map(JsonNode::asText)
                            .map(code -> ResponseEntity.ok(new GeminiResponse(code, responseBody, true, null)))
                            .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body(new GeminiResponse(null, responseBody, false, "Unexpected response format: Could not extract generated code")));

//                    if (candidates != null && candidates.isArray() && !candidates.isEmpty()) {
//                        JsonNode content = candidates.get(0).get("content");
//                        if (content != null) {
//                            JsonNode parts = content.get("parts");
//                            if (parts != null && parts.isArray() && !parts.isEmpty()) {
//                                JsonNode textNode = parts.get(0).get("text");
//                                if (textNode != null && textNode.isTextual()) {
//                                    generatedCode = textNode.asText();
//                                } else {
////                                    logger.warn("Unexpected response format: 'text' field not found or is not a text node");
//                                    return ResponseEntity.badRequest()
//                                            .body(new GeminiResponse(null, responseBody, false, "Unexpected response format: 'text' field not found or is not a text node"));
//                                }
//                            } else {
////                                logger.warn("Unexpected response format: 'parts' array is empty or not found");
//                                return ResponseEntity.badRequest()
//                                        .body(new GeminiResponse(null, responseBody, false, "Unexpected response format: 'parts' array is empty or not found"));
//                            }
//                        } else {
////                            logger.warn("Unexpected response format: 'content' node not found");
//                            return ResponseEntity.badRequest()
//                                    .body(new GeminiResponse(null, responseBody, false, "Unexpected response format: 'content' node not found"));
//                        }
//                    } else {
////                        logger.warn("Unexpected response format: 'candidates' array is empty or not found");
//                        return ResponseEntity.badRequest()
//                                .body(new GeminiResponse(null, responseBody, false, "Unexpected response format: 'candidates' array is empty or not found"));
//                    }
//
//                    return ResponseEntity.ok(new GeminiResponse(generatedCode, responseBody, true, null));

                } catch (IOException e) {
//                    logger.error("Error parsing Gemini API response: {}", e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new GeminiResponse(null, responseBody, false, "Error parsing Gemini API response: " + e.getMessage()));
                }

            } else {
//                logger.error("Gemini API call failed with code: {}, message: {}", response.statusCode(), response.body());
                return ResponseEntity.status(HttpStatus.valueOf(response.statusCode()))  //Propagate the HTTP status code
                        .body(new GeminiResponse(null, response.body(), false, "Gemini API call failed with code: " + response.statusCode() + ", message: " + response.body()));
            }

        } catch (IOException | InterruptedException e) {
//            logger.error("Error during Gemini API call: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GeminiResponse(null, null, false, "Gemini API call failed: " + e.getMessage()));
        }
    }

}
