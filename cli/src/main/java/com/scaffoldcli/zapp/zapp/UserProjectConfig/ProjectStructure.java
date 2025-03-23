package com.scaffoldcli.zapp.zapp.UserProjectConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaffoldcli.zapp.zapp.ServerAccess.ServerAccessHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectStructure {
    private static Pattern TEMPLATE_VAR = Pattern.compile("<<<(\\w+)>>>");

    // Get all children of the current scaff as { id: name } options
    public static Map<String, String> getScaffOptions(String scaffId) {
        String scaffOptions = ServerAccessHandler.getScaffServerRequest(scaffId + "/options");
        return getScaffIdNameMap(scaffOptions);
    }

    // Fetch rendered scaff as parseable { vars: ..., files: ... } JSON object
    public static JsonNode getRendered(String scaffId) {
        String resp = ServerAccessHandler.getScaffServerRequest(scaffId + "/rendered");
        ObjectMapper om = new ObjectMapper();
        JsonNode body = null;

        try { body = om.readTree(resp); }
        catch (IOException e) { System.exit(1); return null; }

        return body;
    }

    // Get all valid predefined & implicit vars from a rendered scaff body
    // Returns { var_name: description } mapping
    public static Map<String, String> getAllVars(JsonNode renderedBody) {
        Map<String, String> res = new HashMap<>();

        JsonNode vars = renderedBody.path("vars");
        JsonNode files = renderedBody.path("files");

        res = extractPredefinedTemplateVars(vars);
        Set<String> implicit = objExtractTemplateVars(files);
        for (var v : implicit) { res.put(v, "<implicit>"); }
        return res;
    }

    private static Map<String, String> extractPredefinedTemplateVars(JsonNode varsNode) {
        Map<String, String> res = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = varsNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            res.put(field.getKey(), field.getValue().asText());
        }

        return res;
    }

    // Recursively get all template variable names in all files in the hierarchy
    private static Set<String> objExtractTemplateVars(JsonNode filesNode) {
        Set<String> res = new HashSet<>();
        Iterator<Map.Entry<String, JsonNode>> fields = filesNode.fields();

        // Iterate through all { fileName: node } key-value pairs in current node
        while (fields.hasNext()) {
            JsonNode value = fields.next().getValue();
            res.addAll( value.isObject() ? objExtractTemplateVars(value) : strExtractTemplateVars(value.asText()) ); // ? folder : file
        }
        return res;
    }

    // Extract all templated variables names from a given string
    private static Set<String> strExtractTemplateVars(String content) {
        Set<String> res = new HashSet<>();

        Matcher matcher = TEMPLATE_VAR.matcher(content);
        while (matcher.find()) { res.add(matcher.group(1)); }

        return res;
    }

    // Substitute all <<<var_name_here>>> occurences in `content` with `replacement`
    private static String substituteVar(String content, String var, String replacement) {
        if (content != null) {
            String pattern = "<<<(" + Pattern.quote(var) + ")>>>";
            content = content.replaceAll(pattern, replacement);
        }
        return content;
    }

    public static void renderFileSystem(String parentDir, JsonNode filesNode, Map<String, String> varSubs) {
        Iterator<Map.Entry<String, JsonNode>> fields = filesNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();
            String objPath = parentDir + "/" + key;

            if (value.isObject()) { // Folder
                File dir = new File(objPath);
                if (!dir.exists()) { dir.mkdirs(); }
                renderFileSystem(objPath, value, varSubs);
            } else { // File
                String fileContent = value.asText();
                for (var e : varSubs.entrySet()) { fileContent = substituteVar(fileContent, e.getKey(), e.getValue()); } // Sub all vars

                try { Files.write(Paths.get(objPath), fileContent.getBytes()); } // Write file
                catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    public static Map<String, String> getScaffIdNameMap(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> scaffNames = new HashMap<>();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();

                JsonNode value = field.getValue();
                String name = value.get("name").asText();
                scaffNames.put(key, name);
            }
        } catch (IOException e) { e.printStackTrace(); }

        return scaffNames;
    }
}
