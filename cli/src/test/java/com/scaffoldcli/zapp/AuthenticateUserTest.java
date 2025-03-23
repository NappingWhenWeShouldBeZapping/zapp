package com.scaffoldcli.zapp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scaffoldcli.zapp.auth.AuthenticateUser;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserTest {

    @Test
    void testIsUserAuthenticated() throws IOException {
        String tokenPath = ZappApplication.AccessTokenFilePath;

        if (tokenPath != null && !Files.exists(Paths.get(tokenPath))) {
            Files.createFile(Paths.get(tokenPath));
        }

        Files.writeString(Paths.get(tokenPath), "dummyAccessToken");
        try (MockedStatic<AuthenticateUser> mockedStatic = mockStatic(AuthenticateUser.class)) {
            mockedStatic.when(AuthenticateUser::triggerUserAuthenticationFlow).thenAnswer(invocation -> {
                return null;
            });
            mockedStatic.when(AuthenticateUser::authenticateUser).thenReturn(true);

            boolean isAuthenticated = AuthenticateUser.isUserAuthenticated();

            assertFalse(isAuthenticated);
        }
    }
}