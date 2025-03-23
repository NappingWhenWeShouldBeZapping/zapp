package com.scaffoldcli.zapp.zapp.auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.web.client.RestTemplate;

import com.scaffoldcli.zapp.zapp.ZappApplication;

public class AutheticateUser {

    public static void triggerUserAutheticationFlow(){
        if(!isUserAutheticated()){ 
            System.out.println("You are not autheticated. Open your brower to login...");
            if(authenticateUser()){
                System.out.println("You are now logged in.");
            }
            else {
                System.out.println("We could not log you in, please try again.");
                System.exit(1);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static Boolean authenticateUser(){
        try {
            Runtime.getRuntime().exec("cmd /c \"start " + ZappApplication.ClientUrl + "\"");
            Integer tryCount = 30;
            while (!isUserAutheticated() && tryCount > 0) {
                --tryCount;	
            }
            if (isUserAutheticated()) {
                Runtime.getRuntime().exec("cmd /c \"start " + ZappApplication.ClientUrl + "login/success\"");
            } else {
                System.err.println("Authentication failed after multiple attempts.");
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("Please authenticate your google account");
            System.exit(1);
        }
        return isUserAutheticated();
    }

    public static Boolean isUserAutheticated () {
        try { Thread.sleep(2000); } catch (InterruptedException e) { /* zzZZ */ };

        if (ZappApplication.AccessToken == null){
            try{
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