package com.levelUp2.project_scaffolding_server.db.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String generateFilesFromGeminiResponse(String geminiResponse, String outputDir) throws IOException {
        try {
            final JsonNode jsonNode = objectMapper.readTree(geminiResponse);

            // Extract the JSON string from the "text" property
            String fileStructureJson = extractFileStructureJson(jsonNode);


            if (fileStructureJson == null) {
                throw new IllegalArgumentException("Gemini response does not contain file_structure");
            }

            System.out.printf(fileStructureJson);


            final JsonNode fileStructureNode = objectMapper.readTree(fileStructureJson);
            return createFilesFromAIJson(fileStructureNode, outputDir);
        } catch (Exception e) {
            System.err.println("Code failure " + e.getMessage());
            throw e;
        }
    }

    private String extractFileStructureJson(JsonNode jsonNode) {
        try {
            JsonNode contentNode = jsonNode.get("candidates").get(0).get("content");
            JsonNode partsNode = contentNode.get("parts").get(0);
            String text = partsNode.get("text").asText();
            // Extract the JSON string from the code block

            //Find the code block
            Pattern pattern = Pattern.compile("```json\\n(.*)```", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        } catch (Exception e) {
            return null; // Or throw an exception if you prefer
        }
    }

    private String createFilesFromAIJson(JsonNode fileStructureNode, String outputDir) throws IOException {
        try {
            StringBuilder details = new StringBuilder();

            // Ensure output directory exists
            Path outputPath = Paths.get(outputDir);
            Files.createDirectories(outputPath);

            System.out.printf("===========================================");
            System.out.println();
            System.out.printf(fileStructureNode.toString());
            System.out.println();
            //Check if files exists before running code to see that there's no code injection
            JsonNode fileStructureN = fileStructureNode.get("file_structure");
            if (fileStructureN == null) {
                return "The file, Maven Build was not found, security issues may have removed files that are not standard. Please edit the response and have it try again.";
            }

            //Cycle through code
            Iterator<Map.Entry<String, JsonNode>> filePaths = fileStructureN.fields();
            System.out.println(filePaths);
            while (filePaths.hasNext()) {
                Map.Entry<String, JsonNode> field = filePaths.next();

                String key = field.getKey();
                JsonNode value = field.getValue();

                System.out.println(key);
                System.out.println(value);


//                JsonNode fileNode = fileStructureNode.get(filePath);
//                System.out.printf(filePath);
                if (value == null || !value.isObject()) {
                    System.err.println("Not valid format, skipped " + key);
                    continue;
                }

                JsonNode contentNode = value.get("content");
                JsonNode isBinaryNode = value.get("is_binary");
                if (contentNode == null || !contentNode.isTextual()) {
                    System.err.println("Not a proper Json Type, skipped " + key);
                    continue;
                }

                String content = contentNode.asText();
                System.out.printf("=======================CONTENT===========================");
                System.out.printf(content);
                boolean isBinary = value.has("is_binary") && value.get("is_binary").asBoolean(false);
                createFile(outputDir, key, content, isBinary); //Function to create files

                details.append("Created file " + key);

            }
            return details.toString();
        } catch (Exception e) {
            System.err.println("Something went wrong, skip" + e.getMessage());
            throw e;
        }

    }

    private void createFile(String outputDir, String filePath, String content, boolean isBinary) throws IOException {
        Path directoryPath = Paths.get(outputDir, filePath).getParent();
        if (directoryPath != null) {
            Files.createDirectories(directoryPath); // Create the necessary directories
        }

        Path finalPath = Paths.get(outputDir, filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(finalPath.toFile()))) {
            writer.write(content);
        }
    }


}
