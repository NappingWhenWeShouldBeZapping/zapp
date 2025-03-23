package com.scaffoldcli.zapp.ServerAccess;

import com.scaffoldcli.zapp.ZappApplication;
import com.scaffoldcli.zapp.auth.AutheticateUser;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class ServerAccessHandler {

    private static String reqScaff(String scaffId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = ZappApplication.ServerUrl + "/scaff/" + scaffId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + ZappApplication.AccessToken);

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
        String url = ZappApplication.ServerUrl + "/gemini/template";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + ZappApplication.AccessToken);

        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);


        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }



    public static void createScaffServerRequest(String jsonBody) {
        try {
            AutheticateUser.triggerUserAutheticationFlow();
            boolean authenticated = false;
            for (int i = 0; i < 30 && !authenticated; i++) {
                if (AutheticateUser.isUserAutheticated()) authenticated = true;
            }
            if (authenticated) {
                System.out.println("\n\n\t\u001B[92m> Authenticated - launching CLI\u001B[0m\n\n");
                Integer statusCode = createScaff(jsonBody);

                if (statusCode.equals(201)) {
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

    private static Integer createScaff(String jsonBody) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = ZappApplication.ServerUrl + "scaff/create";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + ZappApplication.AccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        System.out.println("\n\n\tCreating your scaff\n\n%n");

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        return response.getStatusCode().value();
    }

    public static String deleteScaffServerRequest(String scaffId) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = ZappApplication.ServerUrl + "/scaff/" + scaffId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + ZappApplication.AccessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.DELETE, entity, String.class);
            if (response.getStatusCode() == HttpStatus.NO_CONTENT || response.getStatusCode() == HttpStatus.OK) {
                return "\u001B[92mScaffold deleted successfully.\u001B[0m";
            } else {
                return "\u001B[91mFailed to delete scaffold: " + response.getStatusCode() + "\u001B[0m";
            }
        } catch (ResourceAccessException e) {
            return "\u001B[91m> API server unreachable - failed to delete scaffold.\u001B[0m";
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return "\u001B[93m> You are not authenticated - please log in first.\u001B[0m";
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "\u001B[91m> Scaffold ID not found.\u001B[0m";
            } else {
                return "\u001B[91m> Failed to delete scaffold: " + e.getMessage() + "\u001B[0m";
            }
        } catch (Exception e) {
            return "\u001B[91m> Unexpected error occurred while deleting scaffold: " + e.getMessage() + "\u001B[0m";
        }
    }
}
