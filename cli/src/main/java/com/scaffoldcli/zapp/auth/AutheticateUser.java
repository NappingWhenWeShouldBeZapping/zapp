package com.scaffoldcli.zapp.auth;

import com.scaffoldcli.zapp.ServerAccess.AppUrls;
import com.scaffoldcli.zapp.lib.Text;
import com.scaffoldcli.zapp.lib.Text.Colour;

import java.io.IOException;

public class AutheticateUser {

    public static void triggerUserAutheticationFlow() {
        if (!isUserAutheticated()) {
            Text.print("You are not authenticated. Open your browser to login...", Colour.yellow);
            if (authenticateUser()) {
                Text.print("Your are now logged in.", Colour.bright_green);
            } else {
                Text.print("We could not log you in, please try again.", Colour.bright_red);
                System.exit(1);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean authenticateUser() {
        try {
            Runtime.getRuntime().exec("cmd /c \"start " + AppUrls.getClient() + "\"");
            Integer tryCount = 30;
            while (!isUserAutheticated() && tryCount > 0) {
                --tryCount;
            }
            if (isUserAutheticated()) {
                Runtime.getRuntime().exec("cmd /c \"start " + AppUrls.getClient() + "login/success\"");
            } else {
                Text.print("Authentication failed after multiple attempts.", Colour.bright_red);
                System.exit(1);
            }
        } catch (IOException | SecurityException e) {
            Text.print("Please authenticate your google account", Colour.bright_red);
            System.exit(1);
        }
        return isUserAutheticated();
    }

    public static boolean isUserAutheticated() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) { /* zzZZ */ }

        return GoogleAuthValidator.isValidGoogleToken(AuthDetails.getAccessToken());
    }
}