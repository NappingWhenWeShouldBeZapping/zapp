package com.levelUp2.project_scaffolding_server.control;

import com.levelUp2.project_scaffolding_server.db.entity.ApiEndpoint;
import com.levelUp2.project_scaffolding_server.db.entity.GeminiResponse;
import com.levelUp2.project_scaffolding_server.db.entity.ProjectSpecification;
import com.levelUp2.project_scaffolding_server.db.service.AiTemplateService;
import com.levelUp2.project_scaffolding_server.db.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class AITemplateontroller {

    @Autowired
    private AiTemplateService aiService;

    @Autowired
    private TemplateService templateService;

    @PostMapping("/gemini/template")
    public ResponseEntity<GeminiResponse> generateCode(@RequestBody ProjectSpecification userInput) throws IOException, InterruptedException, IOException {

        String prompt = constructPrompt(userInput);

        return aiService.generateCode(prompt);

    }

    private String constructPrompt(ProjectSpecification spec) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("You are an AI code generator. Create a project template with the following specifications:\n\n");

        promptBuilder.append("**Project Details:**\n");
        promptBuilder.append("Programming Language: ").append(spec.getProgrammingLanguage()).append("\n");
        promptBuilder.append("Project Type: ").append(spec.getProjectType()).append("\n");
        promptBuilder.append("Project Name: ").append(spec.getProjectName()).append("\n\n");



        promptBuilder.append("**Optional Features (configure if provided):**\n");

        if (spec.getDatabaseType() != null && !spec.getDatabaseType().isEmpty()) {
            promptBuilder.append("Database Type: ").append(spec.getDatabaseType()).append("\n");
            if (spec.getDatabaseName() != null && !spec.getDatabaseName().isEmpty()) {
                promptBuilder.append("  - Database Name: ").append(spec.getDatabaseName()).append("\n");
            }
        }

        if (spec.getFramework() != null && !spec.getFramework().isEmpty()) {
            promptBuilder.append("Framework: ").append(spec.getFramework()).append("\n");
        }

        if (spec.getDependencies() != null && !spec.getDependencies().isEmpty()) {
            promptBuilder.append("Dependencies: ").append(String.join(", ", spec.getDependencies())).append("\n");
        }

        if (spec.getApiEndpoints() != null && !spec.getApiEndpoints().isEmpty()) {
            promptBuilder.append("API Endpoints:\n");
            for (ApiEndpoint endpoint : spec.getApiEndpoints()) {
                promptBuilder.append("  - Path: ").append(endpoint.getPath()).append(", Method: ").append(endpoint.getMethod()).append(", Description: ").append(endpoint.getDescription()).append("\n");
            }
        }

        if (spec.getAuthenticationMethod() != null && !spec.getAuthenticationMethod().isEmpty()) {
            promptBuilder.append("Authentication Method: ").append(spec.getAuthenticationMethod()).append("\n");
        }

        if (spec.getExceptionHandling() != null && !spec.getExceptionHandling().isEmpty()) {
            promptBuilder.append("Exception Handling Strategy: ").append(spec.getExceptionHandling()).append("\n");
        }

        if (spec.getCodeStyleGuide() != null && !spec.getCodeStyleGuide().isEmpty()) {
            promptBuilder.append("Code Style Guide: ").append(spec.getCodeStyleGuide()).append("\n");
        }

        if (spec.getTestingFramework() != null && !spec.getTestingFramework().isEmpty()) {
            promptBuilder.append("Testing Framework: ").append(spec.getTestingFramework()).append("\n");
        }

        if (spec.getTestCoverage() != null && !spec.getTestCoverage().isEmpty()) {
            promptBuilder.append("Desired Test Coverage: ").append(spec.getTestCoverage()).append("\n");
        }

        promptBuilder.append("\n**Response Format:**\n");
        promptBuilder.append("The response *must* be a JSON object with a 'file_structure' property.\n");

        promptBuilder.append("The 'file_structure' should have the format: { 'file_name': { 'content': 'code here', 'is_binary': true/false (optional)} }.\n");
        promptBuilder.append("If a file is binary (e.g., an image), include 'is_binary': true. Otherwise, omit the 'is_binary' field.\n");


        promptBuilder.append("\n**Coding and Implementation Guidelines:**\n");
        promptBuilder.append("- Ensure the generated code is safe to implement.\n");
        promptBuilder.append("- Provide comprehensive comments throughout the code.\n");
        promptBuilder.append("- *Crucially*, include comments to explicitly note any potential errors or security vulnerabilities within the code. And DO NOT include backtick in any way. \n");

        return promptBuilder.toString();
    }
}