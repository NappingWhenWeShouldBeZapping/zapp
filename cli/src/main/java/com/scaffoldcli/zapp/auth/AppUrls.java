package com.scaffoldcli.zapp.auth;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppUrls {

    @Value("${api.server-domain}")
    private String serverDomain;

    private static final String client = "http://localhost:8001/";
    private static String staticServerDomain;
    private static final String googleTokenInfo = "https://oauth2.googleapis.com/tokeninfo?access_token=";

    @PostConstruct
    public void init() {
        staticServerDomain = serverDomain;
    }

    public static String getClient(){
        return client;
    }

    public static String getServer(){
        return staticServerDomain;
    }

    public static String getGoogleTokenInfoUrl(){
        return googleTokenInfo;
    }

}
