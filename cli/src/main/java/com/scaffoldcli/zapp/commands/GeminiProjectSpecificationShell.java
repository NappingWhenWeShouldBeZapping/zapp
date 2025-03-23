package com.scaffoldcli.zapp.commands;


import com.scaffoldcli.zapp.ServerAccess.ServerAccessHandler;
import com.scaffoldcli.zapp.lib.AITemplateService;
import com.scaffoldcli.zapp.lib.Text;
import com.scaffoldcli.zapp.models.GeminiResponse;
import com.scaffoldcli.zapp.models.ProjectSpecification;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

public class GeminiProjectSpecificationShell implements Command {
    private final Validator validator;
    private ProjectSpecification projectSpecification = new ProjectSpecification();
    private final AITemplateService aiService;

    public GeminiProjectSpecificationShell() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        this.aiService = new AITemplateService();

    }


    public void startProjectSpecification() {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Starting interactive project specification session...\n");

        System.out.print("Enter Project Name: ");
        projectSpecification.setProjectName(scanner.nextLine());

        System.out.print("Enter Project Type: ");
        projectSpecification.setProjectType(scanner.nextLine());

        System.out.print("Enter Programming Language: ");
        projectSpecification.setProgrammingLanguage(scanner.nextLine());

        System.out.print("Enter Database Type (or leave blank): ");
        projectSpecification.setDatabaseType(scanner.nextLine());

        System.out.print("Enter Database Name (or leave blank): ");
        projectSpecification.setDatabaseName(scanner.nextLine());

        System.out.print("Enter Framework (or leave blank): ");
        projectSpecification.setFramework(scanner.nextLine());

        System.out.print("Enter Authentication Method (or leave blank): ");
        projectSpecification.setAuthenticationMethod(scanner.nextLine());

        System.out.print("Enter Exception Handling strategy (or leave blank): ");
        projectSpecification.setExceptionHandling(scanner.nextLine());

        System.out.print("Enter Code Style Guide (or leave blank): ");
        projectSpecification.setCodeStyleGuide(scanner.nextLine());

        System.out.print("Enter Testing Framework (or leave blank): ");
        projectSpecification.setTestingFramework(scanner.nextLine());

        System.out.print("Enter Test Coverage (or leave blank): ");
        projectSpecification.setTestCoverage(scanner.nextLine());

        Set<ConstraintViolation<ProjectSpecification>> violations = validator.validate(projectSpecification);

        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation errors:\n");
            for (ConstraintViolation<ProjectSpecification> violation : violations) {
                errorMessage.append("  - ").append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("\n");
            }
            Text.print(errorMessage.toString(), Text.Colour.bright_red);
        } else {
            generateProjectFiles();
        }


    }

    public void generateProjectFiles() {
        try {
            ResponseEntity<GeminiResponse> geminiResponse = ServerAccessHandler.createAITemplate(projectSpecification);

            if (!geminiResponse.getStatusCode().is2xxSuccessful() || geminiResponse.getBody() == null) {
                Text.print("Failed to generate code from Gemini API: " + geminiResponse.getStatusCode(), Text.Colour.bright_red);
            } else {
                GeminiResponse responseBody = geminiResponse.getBody();

                if (!responseBody.isSuccess()) {
                    Text.print("Gemini API reported an error: " + responseBody.getErrorMessage(), Text.Colour.bright_red);
                } else {
                    String response = aiService.generateFilesFromGeminiResponse(responseBody.getGeneratedCode(), projectSpecification.getProjectName());
                    Text.print(response, Text.Colour.green);
                }
            }


        } catch (IOException e) {
            Text.print(e.getMessage(), Text.Colour.bright_red);
//            return e.getMessage();
        } catch (Exception e) {
            Text.print("Unexpected error during project file generation: " + e.getMessage(), Text.Colour.bright_red);
//            return "Unexpected error during project file generation: " + e.getMessage();
        }
    }

    @Override
    public void run() {

        startProjectSpecification();

    }


}
