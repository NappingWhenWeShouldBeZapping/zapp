package com.levelUp2.project_scaffolding_server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelUp2.project_scaffolding_server.db.entity.User;
import com.levelUp2.project_scaffolding_server.db.service.UserService;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AuthenticateUser {
    private static UserService userService;

    private static final Map<String, String> googleUserDetails = new HashMap<>();

    public static void setUserService(UserService service) {
        userService = service;
    }

    public static boolean getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

        RestTemplate restTemplate = new RestTemplate();
        try {
            String userInfo = restTemplate.getForObject(userInfoUrl + "?access_token=" + accessToken, String.class);

            AuthenticateUser.createOrLoginUser(userInfo);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static User getUserFromMap() {
        User user = new User();

        String emailAddress = googleUserDetails.get("email");
        String username = googleUserDetails.get("email").split("@")[0];

        user.setUsername(username);
        user.setEmail(emailAddress);
        return user;
    }

    private static void createOrLoginUser(String userDetails) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(userDetails);
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();
                googleUserDetails.put(key, value.asText());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        User user = AuthenticateUser.getUserFromMap();
        userService.createUser(user);
    }

    public static String getEmail(){
        return googleUserDetails.get("email");
    }
}
