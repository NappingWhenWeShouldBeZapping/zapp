package com.scaffoldcli.zapp;

import com.scaffoldcli.zapp.UserProjectConfig.ProjectStructure;
import com.scaffoldcli.zapp.ServerAccess.ServerAccessHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ProjectStructure.class)
class ProjectStructureTest {

    @InjectMocks
    private ProjectStructure projectStructure;

    private static final String TEST_DIR = "MyProj";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cleanUpFiles(); // Ensure clean start before each test
    }

    @AfterEach
    void tearDown() {
        cleanUpFiles(); // Cleanup after each test
    }

    @Test
    void testGetScaffOptions() {
        String mockResponse = "{ \"123\": { \"name\": \"Test Option\" } }";

        try (MockedStatic<ServerAccessHandler> mockStatic = mockStatic(ServerAccessHandler.class)) {
            mockStatic.when(() -> ServerAccessHandler.getScaffServerRequest("123/options")).thenReturn(mockResponse);

            Map<String, String> options = ProjectStructure.getScaffOptions("123");

            assertNotNull(options);
            assertEquals(1, options.size());
            assertEquals("Test Option", options.get("123"));
        }
    }



    @Test
    void testGetScaffIdNameMap() {
        String jsonString = "{ \"001\": { \"name\": \"Template A\" }, \"002\": { \"name\": \"Template B\" } }";
        Map<String, String> result = ProjectStructure.getScaffIdNameMap(jsonString);

        assertEquals(2, result.size());
        assertEquals("Template A", result.get("001"));
        assertEquals("Template B", result.get("002"));
    }

    private void cleanUpFiles() {
        File dir = new File(TEST_DIR);
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
            dir.delete();
        }
    }
}
