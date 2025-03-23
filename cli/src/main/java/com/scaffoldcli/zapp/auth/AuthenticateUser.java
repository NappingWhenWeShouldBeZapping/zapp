package com.scaffoldcli.zapp.auth;

import com.scaffoldcli.zapp.ZappApplication;
import com.scaffoldcli.zapp.lib.Text;
import com.scaffoldcli.zapp.lib.Text.Colour;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AuthenticateUser {

    public static void triggerUserAuthenticationFlow() {
        if (!isUserAuthenticated()) {
            Text.print("You are not authenticated. Open your browser to login...");
            if (authenticateUser()) {
                Text.print("Your are now logged in.");
            } else {
                Text.print("We could not log you in, please try again.", Colour.bright_red);
                System.exit(1);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static Boolean authenticateUser() {
        try {
            Runtime.getRuntime().exec("cmd /c \"start " + ZappApplication.ClientUrl + "\"");
            Integer tryCount = 30;
            while (!isUserAuthenticated() && tryCount > 0) {
                --tryCount;
            }
            if (isUserAuthenticated()) {
                Runtime.getRuntime().exec("cmd /c \"start " + ZappApplication.ClientUrl + "login/success\"");
            } else {
                Text.print("Authentication failed after multiple attempts.", Colour.bright_red);
                System.exit(1);
            }
        } catch (IOException e) {
            Text.print("Please authenticate your google account", Colour.bright_red);
            System.exit(1);
        }
        return isUserAuthenticated();
    }

    public static Boolean isUserAuthenticated() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }

        if (ZappApplication.AccessToken == null) {
            try {
                ZappApplication.AccessToken = Files.readString(Paths.get(ZappApplication.AccessTokenFilePath));
            } catch (IOException e) {
                return false;
            }
        }
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForObject(userInfoUrl + "?access_token=" + ZappApplication.AccessToken, String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}