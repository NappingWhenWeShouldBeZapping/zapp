package com.scaffoldcli.zapp.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scaffoldcli.zapp.lib.Text;
import com.scaffoldcli.zapp.lib.Text.Colour;
import com.scaffoldcli.zapp.net.ZappAPIRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CreateScaffCommand implements Command {
    private static final Set<String> IGNORED_DIRECTORIES = new HashSet<>();
    private static final List<FileData> fileDataList = new ArrayList<>();
    private static final Map<String, Object> jsonMap = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final Set<String> IGNORED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "bin", "exe", "dll", "class", "jpg", "jpeg", "png", "gif", "bmp", "mp3", "mp4", "avi", "zip", "tar", "gz",
            "rar", "iso", "apk", "exe", "dmg",
            "log", "out", "jar"
    ));

    static {
        IGNORED_DIRECTORIES.add("node_modules");
        IGNORED_DIRECTORIES.add(".git");
        IGNORED_DIRECTORIES.add(".idea");
        IGNORED_DIRECTORIES.add(".vscode");
        IGNORED_DIRECTORIES.add(".DS_Store");
        IGNORED_DIRECTORIES.add("Thumbs.db");
    }

    private String[] options;

    private static void readFilesRecursively(File dir, String rootPath) {
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (shouldIgnore(file)) {
                    continue;
                }

                if (file.isDirectory()) {
                    readFilesRecursively(file, rootPath);
                } else {
                    processFile(file, rootPath);
                }
            }
        }
    }

    private static boolean shouldIgnore(File file) {
        if (IGNORED_DIRECTORIES.contains(file.getName())) {
            return true;
        }

        String fileName = file.getName();
        String fileExtension = getFileExtension(fileName);
        return IGNORED_EXTENSIONS.contains(fileExtension);
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private static void processFile(File file, String rootPath) {
        String relativePath = null;
        try {
            relativePath = getRelativePath(file, rootPath);
        } catch (IOException e) {
            Text.print("Error reading file: " + relativePath, Colour.bright_red, true);
        }

        jsonMap.put("parent", null);

        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            Text.print("Error reading file: " + relativePath, Colour.bright_red, true);
        }
        fileDataList.add(new CreateScaffCommand.FileData(relativePath, contentBuilder.toString()));
    }

    private static String getRelativePath(File file, String rootPath) throws IOException {
        Path filePath = Paths.get(file.getAbsolutePath());
        Path root = Paths.get(rootPath);
        return root.relativize(filePath).toString().replaceAll("\\\\", "/");
    }

    private static String fileDataToJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        jsonMap.put("insertions", fileDataList);
        return gson.toJson(jsonMap);
    }

    @Override
    public void run() {
        String scaffName = Text.input("What do you want to call your scaff?");
        String scaffDescr = Text.input("Give you scaff a brief description.");

        jsonMap.put("scaffName", scaffName);
        jsonMap.put("scaffDescr", scaffDescr);

        String directoryPath = System.getProperty("user.dir");

        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            readFilesRecursively(directory, directoryPath);
            String fileDataJson = fileDataToJson();
            sendCreateScaffRequest(fileDataJson);
        } else {
            Text.print("Provided path is not a valid directory.", Colour.bright_red, true);
        }
    }

    private void sendCreateScaffRequest(String json) {
        HttpResponse<String> response = new ZappAPIRequest().post("/scaff/", json);

        switch (response.statusCode()) {
            case 201:
                Text.print("Your scaff was create successfully.", Colour.bright_green, true);
                break;
            case 400:
                Text.print("Malformed request. Please try again.", Colour.bright_red);
                break;
            case 404:
                Text.print("Could not find the requested resource on the server. Please try again.", Colour.bright_red);
                break;
            case 500:
                Text.print("Internal server error. Please try again later.", Colour.bright_red);
                break;
            default:
                Text.print("Could not create your scaff. Please try again.", Colour.bright_red);
                break;
        }
        System.exit(0);
    }

    private static class FileData {
        private final String relativePath;
        private final String content;

        public FileData(String relativePath, String content) {
            this.relativePath = relativePath;
            this.content = content;
        }
    }
}
