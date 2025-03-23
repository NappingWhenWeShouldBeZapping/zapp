package com.scaffoldcli.zapp.models;

import lombok.Data;

@Data
public class GeminiResponse {
    private String generatedCode;
    private String rawResponse;
    private boolean success;
    private String errorMessage;

    public GeminiResponse(String generatedCode, String rawResponse, boolean success, String errorMessage) {
        this.generatedCode = generatedCode;
        this.rawResponse = rawResponse;
        this.success = success;
        this.errorMessage = errorMessage;
    }
}