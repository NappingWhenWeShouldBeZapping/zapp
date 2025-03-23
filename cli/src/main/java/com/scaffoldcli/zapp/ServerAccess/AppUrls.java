package com.scaffoldcli.zapp.ServerAccess;

import org.springframework.beans.factory.annotation.Value;

public class AppUrls {

    @Value("${api.server-domain}")
    private static String serverDomain;

    private static final String client = "http://localhost:8001/";
    private static final String googleTokenInfo = "https://oauth2.googleapis.com/tokeninfo?access_token=";

    public static String getClient(){
        return client;
    }

    public static String getServer(){
        return serverDomain;
    }

    public static String getGoogleTokenInfoUrl(){
        return googleTokenInfo;
    }

}
