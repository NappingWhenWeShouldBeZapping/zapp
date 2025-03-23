package com.levelUp2.project_scaffolding_server.control;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.ResponseEntity;

public class GoogleAuthValidator {
    private static RestTemplate restTemplate = new RestTemplate();

    public static boolean isValidGoogleToken(String accessToken) {
        if(accessToken == null){
            return false;
        }
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?access_token=" + accessToken;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            return false;
        }
    }
}