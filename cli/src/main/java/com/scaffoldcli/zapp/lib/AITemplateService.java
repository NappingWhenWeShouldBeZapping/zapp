package com.scaffoldcli.zapp.lib;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AITemplateService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String generateFilesFromGeminiResponse(String geminiResponse, String outputDir) throws IOException {
        try {

            String fileStructureJson = extractFileStructureJson(geminiResponse);

            if (fileStructureJson == null) {
                String errorMessage = "Gemini response does not contain file_structure or cannot extract JSON from code block";
                throw new IllegalArgumentException(errorMessage);
            }

            JsonNode fileStructureNode = objectMapper.readTree(fileStructureJson);

            return createFilesFromAIJson(fileStructureNode, outputDir);

        } catch (IOException e) {
            String errorMessage = "Error parsing extracted file structure: " + e.getMessage();
            throw new IOException(errorMessage, e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error during file generation: " + e.getMessage();
            throw new IOException(errorMessage, e);
        }
    }


    private String extractFileStructureJson(String text) {
        try {

            Pattern pattern = Pattern.compile("```json\\n(.*)```", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                return matcher.group(1);
            } else {
                return text;
            }

        } catch (Exception e) {
            return null;
        }
    }

    private String createFilesFromAIJson(JsonNode fileStructureNode, String outputDir) throws IOException {


        try {
            StringBuilder details = new StringBuilder();

            Path outputPath = Paths.get(outputDir);
            Files.createDirectories(outputPath);

            JsonNode fileStructureN = fileStructureNode.get("file_structure");

            if (fileStructureN == null) {
                return "TInvalid file structure: file structure node not found.";
            }

            Iterator<Map.Entry<String, JsonNode>> filePaths = fileStructureN.fields();
            while (filePaths.hasNext()) {
                Map.Entry<String, JsonNode> field = filePaths.next();

                String key = field.getKey();
                JsonNode value = field.getValue();

                System.out.println(key);
                System.out.println(value);
                if (value == null || !value.isObject()) {
                    details.append("Skipped invalid file format: ").append(key).append("\n");
                    continue;
                }

                JsonNode contentNode = value.get("content");
                System.out.println(contentNode);
                if (contentNode == null || !contentNode.isTextual()) {
                    details.append("Skipped invalid file: content missing or not text for ").append(key).append("\n");
                    continue;
                }

                String content = contentNode.asText();
                boolean isBinary = value.has("is_binary") && value.get("is_binary").asBoolean(false);

                Path filePath = Paths.get(outputDir, key);
                createFile(filePath, content, isBinary);

                details.append("Created file: ").append(key).append("\n");
            }
            return details.toString();

        } catch (IOException e) {
            String errorMessage = "Error during file creation: " + e.getMessage();
            throw new IOException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Unexpected error during file creation: " + e.getMessage();
            throw new IOException(errorMessage, e);
        }


    }

    private void createFile(Path filePath, String content, boolean isBinary) throws IOException {
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        if (isBinary) {
            byte[] decodedContent = Base64.getDecoder().decode(content);
            Files.write(filePath, decodedContent);
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                writer.write(content);
            }
        }
    }


}
