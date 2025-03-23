package com.scaffoldcli.zapp.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.scaffoldcli.zapp.lib.Text;
import com.scaffoldcli.zapp.lib.Text.Colour;
import com.scaffoldcli.zapp.models.GetScaffOptionsResponse;
import com.scaffoldcli.zapp.models.GetScaffsResponse;
import com.scaffoldcli.zapp.net.ZappAPIRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.shell.component.message.ShellMessageBuilder;
import org.springframework.shell.component.view.TerminalUI;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.component.view.control.AppView;
import org.springframework.shell.component.view.control.BoxView;
import org.springframework.shell.component.view.control.ListView;
import org.springframework.shell.component.view.control.ListView.ItemStyle;
import org.springframework.shell.component.view.control.ListView.ListViewOpenSelectedItemEvent;
import org.springframework.shell.component.view.control.ListView.ListViewSelectedItemChangedEvent;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

public class Init implements Command {
    private final static ParameterizedTypeReference<ListViewSelectedItemChangedEvent<String>> LISTVIEW_STRING_SELECT
            = new ParameterizedTypeReference<ListViewSelectedItemChangedEvent<String>>() {
    };
    private final static ParameterizedTypeReference<ListViewOpenSelectedItemEvent<String>> LISTVIEW_STRING_OPEN
            = new ParameterizedTypeReference<ListViewOpenSelectedItemEvent<String>>() {
    };
    private final TerminalUIBuilder terminalUIBuilder;
    private final List<String> test_items = new ArrayList<>();
    private List<GetScaffsResponse> items;
    private List<String> _items;
    private TerminalUI ui;
    private AppView app;
    private EventLoop eventLoop;
    private Map<String, String> itemToScaff;
    private String currentScaffId = "";

    public Init(TerminalUIBuilder termUIBuilder) {
        this.terminalUIBuilder = termUIBuilder;
    }

    public static void generateScaffFiles(String json) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String outputDirectory = "output/";
        new File(outputDirectory).mkdirs();

        createFilesAndDirectories(outputDirectory, jsonObject.getAsJsonObject("files"));
        Text.print("Scaff generated. Happy coding...", Text.Colour.bright_green);
    }

    private static void createFilesAndDirectories(String basePath, JsonObject filesObject) {
        for (Map.Entry<String, JsonElement> entry : filesObject.entrySet()) {
            String path = basePath + entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                new File(path).mkdirs();
                createFilesAndDirectories(path + "/", value.getAsJsonObject());
            } else if (value.isJsonPrimitive()) {
                createFile(path, value.getAsString());
            }
        }
    }

    private static void createFile(String filePath, String content) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(content);
            }
        } catch (IOException e) {
            Text.print("Error writing file: " + filePath, Text.Colour.red);
        }
    }

    private static void fetchRender(String scaffId) {
        HttpResponse<String> res = new ZappAPIRequest().get(String.format("/scaff/%s/rendered", scaffId));

        if (res.statusCode() == 200) {
            generateScaffFiles(res.body());
        } else {
            Text.print("Could not get scaff file structure. Please try again", Text.Colour.bright_red, true);
        }
    }

    private void handleMoreOptions(String response) {
        Map<String, GetScaffsResponse> options = GetScaffOptionsResponse.fromJson(response);

        for (Map.Entry<String, GetScaffsResponse> entry : options.entrySet()) {
            this.test_items.clear();
            GetScaffsResponse scaff = entry.getValue();

            this.test_items.add(scaff.getName());
            this.itemToScaff.put(scaff.getName(), scaff.getId());
        }
    }

    private boolean loadOptions() {
        this.itemToScaff = new HashMap<String, String>();

        Text.print("Waiting for server response...", Colour.bright_yellow, true);
        HttpResponse<String> response = new ZappAPIRequest().get("/scaff");

        this.items = GetScaffsResponse.fromJson(response.body());

        this.currentScaffId = this.items.getFirst().getId();

        for (GetScaffsResponse item : this.items) {
            this.itemToScaff.put(item.getName(), item.getId());
        }

        return !this.items.isEmpty();
    }

    private void getScaffData(String scaffId) {
        HttpResponse<String> response = new ZappAPIRequest().get(String.format("/scaff/%s/options", scaffId));

        if (response.statusCode() == 200) {
            if (Objects.equals(response.body(), "204")) {
                fetchRender(scaffId);
                System.out.println("\u001B[?25h");
                System.out.flush();
                System.exit(0);
            } else {
                handleMoreOptions(response.body());
            }
        } else {
            Text.print("Could not get scaff options. Please try again.", Text.Colour.bright_red, true);
        }
    }

    @Override
    public void run() {
        //---------- Construct UI ----------//
        ui = terminalUIBuilder.build();
        eventLoop = ui.getEventLoop();

        loadOptions();

        for (GetScaffsResponse item : this.items) {
            test_items.add(item.getName());
        }

        // Construct selection list
        ListView<String> list = new ListView<String>(test_items, ItemStyle.NOCHECK);
        list.setBorderPadding(1, 1, 1, 1);
        list.setTitle(" Select an option to scaffold ");
        list.setShowBorder(true);
        ui.configure(list);

        // Construct app view
        app = new AppView(list, new BoxView(), new BoxView());
        ui.configure(app);

        //---------- Setup event listeners ----------//
        // Handle quit
        eventLoop.onDestroy(eventLoop.keyEvents()
                .doOnNext(m -> {
                    if (m.getPlainKey() == KeyEvent.Key.q) {
                        eventLoop.dispatch(ShellMessageBuilder.ofInterrupt());
                        System.out.println("\n\n\t\u001B[94m> Zapp Init terminated - No project created\u001B[0m\n\n");
                        System.out.println("\u001B[?25h"); // Restore cursor visibility
                        System.out.flush();
                        System.exit(0);
                    }
                })
                .subscribe());

// Handle item chosen - move to next question
        eventLoop.onDestroy(eventLoop.viewEvents(LISTVIEW_STRING_OPEN, list).subscribe(event -> {
            String chosen = event.args().item();
            if (chosen == null) return;

            String scaffId = this.itemToScaff.get(chosen);
            getScaffData(scaffId);

            //----- Populate UI -----//
            list.setTitle(chosen + " has more options");
            list.setItems(test_items);
        }));
        // list.shortcut(KeyEvent.Key.Enter, () -> {});

        //---------- Run UI ----------//
        ui.setRoot(app, true);
        ui.setFocus(list);
        ui.run();
    }
}