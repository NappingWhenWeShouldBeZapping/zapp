package com.scaffoldcli.zapp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scaffoldcli.zapp.auth.AuthDetails;
import com.scaffoldcli.zapp.auth.AutheticateUser;
import com.scaffoldcli.zapp.auth.GoogleAuthValidator;

@ExtendWith(MockitoExtension.class)
class AutheticateUserTest {

    private final String dummyToken = "DummyToken";

    @BeforeEach
    void setUp() throws IOException {
        // Mock the access token file if needed
        String tokenPath = "DummyAccessToken.txt";
        if (tokenPath != null && !Files.exists(Paths.get(tokenPath))) {
            Files.createFile(Paths.get(tokenPath));
        }
    }

    @Test
    void testIsUserAuthenticated() throws IOException {
        try (
            MockedStatic<AuthDetails> authDetailsMock = mockStatic(AuthDetails.class);
            MockedStatic<GoogleAuthValidator> googleAuthMock = mockStatic(GoogleAuthValidator.class)
        ) {
            // Mock static methods
            authDetailsMock.when(AuthDetails::getAccessToken).thenReturn(dummyToken);
            googleAuthMock.when(() -> GoogleAuthValidator.isValidGoogleToken(dummyToken)).thenReturn(true);

            // Set access token
            AuthDetails.setAccessToken(dummyToken); // Ensure it is properly set

            // Call the method
            boolean isAuthenticated = AutheticateUser.isUserAutheticated();

            // Assertion (change to `assertTrue(isAuthenticated);` if expected to be authenticated)
            assertTrue(isAuthenticated);
        }
    }
}
