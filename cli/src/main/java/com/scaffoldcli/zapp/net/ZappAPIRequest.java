package com.scaffoldcli.zapp.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.scaffoldcli.zapp.ServerAccess.AppUrls;
import com.scaffoldcli.zapp.auth.AuthDetails;
import com.scaffoldcli.zapp.auth.AutheticateUser;
import com.scaffoldcli.zapp.lib.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static com.scaffoldcli.zapp.auth.AutheticateUser.triggerUserAutheticationFlow;

public class ZappAPIRequest {
    private final String baseURL = AppUrls.getServer();
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public ZappAPIRequest() {
    }

    public HttpResponse<String> get(String endpoint) {
        endpoint = replaceFirstSlash(endpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + endpoint))
                .header("Authorization", String.format("Bearer %s", AuthDetails.getAccessToken()))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 401){
                triggerUserAutheticationFlow();
                return get(endpoint);
            }
            return response;
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
                .header("Authorization", String.format("Bearer %s", AuthDetails.getAccessToken()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(req)))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 401){
                triggerUserAutheticationFlow();
                return post(endpoint, body);
            }
            return response;
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
        AutheticateUser.triggerUserAutheticationFlow();

        endpoint = replaceFirstSlash(endpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + endpoint))
                .header("Authorization", String.format("Bearer %s", AuthDetails.getAccessToken()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 401){
                triggerUserAutheticationFlow();
                return put(endpoint, body);
            }
            return response;
        } catch (IOException e) {
            Text.print("Couldn't connect to the zapp server. Please try again.", Text.Colour.bright_red, true);
            System.exit(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public HttpResponse<String> delete(String endpoint) throws InterruptedException, IOException {
        AuthDetails.getAccessToken();

        endpoint = replaceFirstSlash(endpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseURL + endpoint))
                .header("Authorization", String.format("Bearer %s", AuthDetails.getAccessToken()))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 401){
                triggerUserAutheticationFlow();
                return delete(endpoint);
            }
            return response;
        } catch (IOException e) {
            Text.print("Couldn't connect to the zapp server. Please try again.", Text.Colour.bright_red, true);
            System.exit(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private String replaceFirstSlash(String endpoint) {
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.replaceFirst("/", "");
        }
        return endpoint;
    }
}
