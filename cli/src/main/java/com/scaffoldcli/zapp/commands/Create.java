package com.scaffoldcli.zapp.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scaffoldcli.zapp.ServerAccess.ServerAccessHandler;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Create {
    // Set of common directories/files to ignore
    private static final Set<String> IGNORED_DIRECTORIES = new HashSet<>();
    private static final List<FileData> fileDataList = new ArrayList<>();
    private static final Map<String, Object> jsonMap = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static ServerAccessHandler serverAccessHandler = new ServerAccessHandler();

    private static final Set<String> IGNORED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "bin", "exe", "dll", "class", "jpg", "jpeg", "png", "gif", "bmp", "mp3", "mp4", "avi", "zip", "tar", "gz",
            "rar", "iso", "apk", "exe", "dmg",
            "log", "out", "jar"
    ));

    static {
        IGNORED_DIRECTORIES.add("node_modules");
        IGNORED_DIRECTORIES.add(".git");
        IGNORED_DIRECTORIES.add(".idea");
        IGNORED_DIRECTORIES.add("zapp.log");
        IGNORED_DIRECTORIES.add(".vscode");
        IGNORED_DIRECTORIES.add(".DS_Store");
        IGNORED_DIRECTORIES.add("Thumbs.db");
    }

    public void run() {
        String directoryPath = System.getProperty("user.dir");

        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            readFilesRecursively(directory, directoryPath);
            writeDataToJsonFile();
        } else {
            System.out.println("Provided path is not a valid directory.");
        }
    }

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
        try {
            String relativePath = getRelativePath(file, rootPath);
            StringBuilder contentBuilder = new StringBuilder();

            jsonMap.put("parent", null);

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + relativePath);
                e.printStackTrace();
            }
            fileDataList.add(new FileData(relativePath, contentBuilder.toString()));

        } catch (Exception e) {
            System.err.println("Error processing file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private static String getRelativePath(File file, String rootPath) throws IOException {
        Path filePath = Paths.get(file.getAbsolutePath());
        Path root = Paths.get(rootPath);
        return root.relativize(filePath).toString().replaceAll("\\\\", "/");
    }

    private static void writeDataToJsonFile() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        jsonMap.put("insertions", fileDataList);

        String scaffName = getScaffName();
        String scaffDescr = getScaffDescr();

        jsonMap.put("scaffName", scaffName);
        jsonMap.put("scaffDescr", scaffDescr);

        String json = gson.toJson(jsonMap);

        serverAccessHandler.createScaffServerRequest(json);
    }

    public static String getScaffName() {
        System.out.print("New scaff name:");
        String name = "";
        while (name.isEmpty()) {
            name = scanner.nextLine();
        }
        return name;
    }

    public static String getScaffDescr() {
        System.out.print("Give a quick description:");
        String descr = "";
        while (descr.isEmpty()) {
            descr = scanner.nextLine();
        }
        scanner.close();
        return descr;
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
