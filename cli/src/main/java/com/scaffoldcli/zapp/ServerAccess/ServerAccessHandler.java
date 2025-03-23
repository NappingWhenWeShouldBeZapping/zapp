package com.scaffoldcli.zapp.ServerAccess;

import com.scaffoldcli.zapp.auth.AuthDetails;
import com.scaffoldcli.zapp.auth.AutheticateUser;

import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class ServerAccessHandler {

    private static String reqScaff(String scaffId) {
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(AppUrls.getServer());
        String url = AppUrls.getServer() + "/scaff/" + scaffId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + AuthDetails.getAccessToken());

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        return response.getBody();
    }

    public static String getScaffServerRequest(String scaffId) {
        String res = "";
        try {
            res = reqScaff(scaffId);
        } catch (ResourceAccessException e) {
            System.out.println("\n\n\t\u001B[91m> API server unreachable - exiting\u001B[0m\n\n");
            System.exit(0);
        } catch (HttpClientErrorException e) {
            System.out.println("\n\t\u001B[93m> You are not authenticated - polling for auth\u001B[0m");
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                AutheticateUser.triggerUserAutheticationFlow();
                boolean authenticated = false;
                for (int i = 0; i < 30 && !authenticated; i++) {
                    if (AutheticateUser.isUserAutheticated()) authenticated = true;
                }
                if (authenticated) {
                    System.out.println("\n\n\t\u001B[92m> Authenticated - launching CLI\u001B[0m\n\n");
                    res = reqScaff(scaffId);
                } else {
                    System.out.println("\n\n\t\u001B[91m> Failed to authenticate user - exiting\u001B[0m\n\n");
                    System.exit(0);
                }
            } else {
                System.out.println("\n\n\t\u001B[91m> Failed connecting to API - exiting\u001B[0m\n\n");
                System.exit(1);
            }
        }
        return res;
    }

    public static String createAITemplate(String jsonBody) {
        RestTemplate restTemplate = new RestTemplate();
        String url = AppUrls.getServer() + "/gemini/template";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + AuthDetails.getAccessToken());

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);


        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }



    public void createScaffServerRequest(String jsonBody) {
        try {
            AutheticateUser.triggerUserAutheticationFlow();
            boolean authenticated = false;
            for (int i = 0; i < 30 && !authenticated; i++) {
                if (AutheticateUser.isUserAutheticated()) authenticated = true;
            }
            if (authenticated) {
                System.out.println("\n\n\t\u001B[92m> Authenticated - launching CLI\u001B[0m\n\n");
                int statusCode = createScaff(jsonBody);

                if (statusCode == 201) {
                    System.out.println("\n\n\u001B[92m> Your scaff was created successfully\u001B[0m\n\n");
                    System.exit(0);
                }
            } else {
                System.out.println("\n\n\t\u001B[91m> Failed to authenticate user - exiting\u001B[0m\n\n");
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("\n\n\t\u001B[91m> Failed connecting to API - exiting\u001B[0m\n\n");
            System.exit(1);
        }
    }

    private int createScaff(String jsonBody) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = AppUrls.getServer() + "scaff/create";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + AuthDetails.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        System.out.println("\n\n\tCreating your scaff\n\n%n");

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        return response.getStatusCode().value();
    }
}
