package com.scaffoldcli.zapp.auth;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class AuthDetails {
    private static final String accessTokenFilePath = System.getProperty("user.home") +  "\\ZappAccessToken.txt";
    private static String accessToken = null;

    public static String getAccessToken() {
        if (accessToken == null) {
            try {
                accessToken = Files.readString(Paths.get(accessTokenFilePath));
            } catch (IOException | OutOfMemoryError | SecurityException e) {
                return "";
            }
        }
        return accessToken;
    }

    public static void setAccessToken(String newAccessToken) {
        accessToken = newAccessToken;
        try {
            Files.write(Paths.get(getAccessTokenFilePath()), accessToken.getBytes());
        } catch (InvalidPathException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String getAccessTokenFilePath() {
        return accessTokenFilePath;
    }
}
