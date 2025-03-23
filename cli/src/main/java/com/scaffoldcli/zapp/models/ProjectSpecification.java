package com.scaffoldcli.zapp.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProjectSpecification {

    @NotBlank(message = "Programming language is mandatory")
    @Size(min = 1, max = 20, message = "Programming language must be at most 20 characters, and has at least one character")
    private String programmingLanguage;

    @NotBlank(message = "Project type is mandatory")
    @Size(min = 1, max = 20, message = "Project type must be at most 20 characters, and has at least one character")
    private String projectType;


    @NotBlank(message = "Project name is mandatory")
    @Size(min = 2, max = 30, message = "Project name must be at most 20 characters, and has at least two characters")
    private String projectName;

    private String databaseType;
    private String databaseName;
    private String framework;
    private List<String> dependencies;
    private List<ApiEndpoint> apiEndpoints;
    private String authenticationMethod;
    private String exceptionHandling;
    private String codeStyleGuide;
    private String testingFramework;
    private String testCoverage;


}


