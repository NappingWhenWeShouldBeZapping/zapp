package com.scaffoldcli.zapp.auth;

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

    public static boolean authenticateUser() {
        try {
            new ProcessBuilder("cmd", "/c", "start", AppUrls.getClient()).start();

            int tryCount = 30;
            while (!isUserAutheticated() && tryCount > 0) {
                --tryCount;
                Thread.sleep(500);
            }

            if (isUserAutheticated()) {
                new ProcessBuilder("cmd", "/c", "start", AppUrls.getClient() + "login/success").start();
            } else {
                Text.print("Authentication failed after multiple attempts.", Colour.bright_red);
                System.exit(1);
            }
        } catch (IOException | SecurityException | InterruptedException e) {
            Text.print("Please authenticate your Google account", Colour.bright_red);
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