package com.scaffoldcli.zapp.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.scaffoldcli.zapp.lib.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static com.scaffoldcli.zapp.auth.AuthDetails.getAccessToken;
import static com.scaffoldcli.zapp.auth.AutheticateUser.triggerUserAutheticationFlow;

public class ZappAPIRequest {
    private static String authToken;
    private final String baseURL = "http://13.246.35.49:8080/";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public ZappAPIRequest() {
    }

    public static void checkUserAuth() {
        if (authToken == null || authToken.isEmpty()) {
            triggerUserAutheticationFlow();
            authToken = getAccessToken();
        }
    }

    public HttpResponse<String> get(String endpoint) {
        endpoint = replaceFirstSlash(endpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + endpoint))
                .header("Authorization", String.format("Bearer %s", authToken))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            Text.print("Couldn't connect to the zapp server. Please try again.", Text.Colour.bright_red, true);
            System.exit(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public HttpResponse<String> post(String endpoint, String body) {
        endpoint = replaceFirstSlash(endpoint);

        Map<String, Object> req = convertJsonStringToMap(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + endpoint))
                .header("Authorization", String.format("Bearer %s", authToken))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(req)))
                .build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            Text.print("Couldn't connect to the zapp server. Please try again.", Text.Colour.bright_red, true);
            System.exit(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private Map<String, Object> convertJsonStringToMap(String body) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();

        return gson.fromJson(body, type);
    }

    public HttpResponse<String> put(String endpoint, Map<String, Object> body) throws InterruptedException, IOException {
        checkUserAuth();

        endpoint = replaceFirstSlash(endpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + endpoint))
                .header("Authorization", String.format("Bearer %s", authToken))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> delete(String endpoint) throws InterruptedException, IOException {
        checkUserAuth();

        endpoint = replaceFirstSlash(endpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + endpoint))
                .header("Authorization", String.format("Bearer %s", authToken))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String replaceFirstSlash(String endpoint) {
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.replaceFirst("/", "");
        }
        return endpoint;
    }
}
