package com.scaffoldcli.zapp.auth;

import java.io.IOException;

import com.scaffoldcli.zapp.ServerAccess.AppUrls;

public class AutheticateUser {

    public static void triggerUserAutheticationFlow(){
        if(!isUserAutheticated()){ 
            System.out.println("You are not autheticated. Open your brower to login...");
            if(authenticateUser()){
                System.out.println("Your are now logged in.");
            }
            else {
                System.out.println("We could not log you in, please try again.");
                System.exit(1);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean authenticateUser(){
        try {
            Runtime.getRuntime().exec("cmd /c \"start " + AppUrls.getClient() + "\"");
            Integer tryCount = 30;
            while (!isUserAutheticated() && tryCount > 0) {
                --tryCount;	
            }
            if (isUserAutheticated()) {
                Runtime.getRuntime().exec("cmd /c \"start " + AppUrls.getClient() + "login/success\"");
            } else {
                System.err.println("Authentication failed after multiple attempts.");
                System.exit(1);
            }
        } catch (IOException | SecurityException e) {
            System.err.println("Please authenticate your google account");
            System.exit(1);
        }
        return isUserAutheticated();
    }

    public static boolean isUserAutheticated () {
        try { Thread.sleep(2000); } catch (InterruptedException e) { /* zzZZ */ };

        return GoogleAuthValidator.isValidGoogleToken(AuthDetails.getAccessToken());
    }
}