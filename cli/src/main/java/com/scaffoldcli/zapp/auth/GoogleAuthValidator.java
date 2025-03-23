package com.scaffoldcli.zapp.auth;

import org.springframework.web.client.RestTemplate;

import com.scaffoldcli.zapp.ServerAccess.AppUrls;

import org.springframework.web.client.RestClientException;
import org.springframework.http.ResponseEntity;

public class GoogleAuthValidator {
    private static RestTemplate restTemplate = new RestTemplate();

    public static boolean isValidGoogleToken(String accessToken) {
        try {
            String url = AppUrls.getGoogleTokenInfoUrl() + accessToken;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            return false;
        }
    }
}