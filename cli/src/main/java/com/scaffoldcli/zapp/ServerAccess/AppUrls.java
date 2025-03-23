package com.scaffoldcli.zapp.ServerAccess;

public class AppUrls {
    private static final String client = "http://localhost:8001/";
    private static final String server="http://13.245.75.144:8080";
    private static final String googleTokenInfo = "https://oauth2.googleapis.com/tokeninfo?access_token=";

    public static String getClient(){
        return client;
    }

    public static String getServer(){
        return server;
    }

    public static String getGoogleTokenInfoUrl(){
        return googleTokenInfo;
    }

}
