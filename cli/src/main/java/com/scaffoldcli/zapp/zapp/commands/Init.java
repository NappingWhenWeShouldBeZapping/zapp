package com.scaffoldcli.zapp.zapp.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.scaffoldcli.zapp.zapp.UserProjectConfig.ProjectStructure;
import com.scaffoldcli.zapp.zapp.lib.Util.Pair;

import reactor.core.publisher.Sinks.EmissionException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.shell.component.message.ShellMessageBuilder;
import org.springframework.shell.component.view.TerminalUI;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.component.view.control.AppView;
import org.springframework.shell.component.view.control.BoxView;
import org.springframework.shell.component.view.control.ButtonView;
import org.springframework.shell.component.view.control.GridView;
import org.springframework.shell.component.view.control.InputView;
import org.springframework.shell.component.view.control.ListView;
import org.springframework.shell.component.view.control.ListView.ItemStyle;
import org.springframework.shell.component.view.control.ListView.ListViewOpenSelectedItemEvent;
import org.springframework.shell.component.view.control.ListView.ListViewSelectedItemChangedEvent;
import org.springframework.shell.component.view.control.InputView.InputViewTextChangeEvent;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Init {
    static final String ROOT_SCAFF = "00000000000000000000000000000000";
    static final String DEFAULT_PROJECT_NAME = "MyProj";
    // Ref type helper for deep nested event generics
    private final static ParameterizedTypeReference<ListViewSelectedItemChangedEvent<String>> LISTVIEW_STRING_SELECT
        = new ParameterizedTypeReference<ListViewSelectedItemChangedEvent<String>>() {};
    private final static ParameterizedTypeReference<ListViewOpenSelectedItemEvent<String>> LISTVIEW_STRING_OPEN
        = new ParameterizedTypeReference<ListViewOpenSelectedItemEvent<String>>() {};
    private final static ParameterizedTypeReference<InputViewTextChangeEvent> INPUTVIEW_CHANGE
        = new ParameterizedTypeReference<InputViewTextChangeEvent>() {};

    private final TerminalUIBuilder terminalUIBuilder;
    private TerminalUI ui;
    private AppView app;
    private GridView grid;
	private EventLoop eventLoop;
    private ListView<String> list;
    private InputView nameInput;
    private Map<String, InputView> varInputs;
    private ButtonView renderButton;
    private JsonNode renderedBody;

    public List<Map<String, String>> prompts;
    List<String> items = new ArrayList<String>();
    Map<String, String> itemToScaff;
    private String currentScaffId = "";
    private Map<String, String> varSubs = new HashMap<>();

    public Init(TerminalUIBuilder termUIBuilder) {
        this.terminalUIBuilder = termUIBuilder;
    }

    // Load option values into this.items and this.itemToScaff
    // Return false if there are no options/we have reached the end
    boolean loadOptions(String scaffId) {
        // TODO: Fetch options from API
        this.items = new ArrayList<String>();
        this.itemToScaff = new HashMap<String, String>();

        Map<String, String> scaffIdAndOptions = ProjectStructure.getScaffOptions(scaffId);
        for (Map.Entry<String, String> entry : scaffIdAndOptions.entrySet()) {
            String cid = entry.getKey();
            String idx = cid.substring(0, 6);
            String name = entry.getValue();

            if (name != "") { this.items.add(String.format("▶ %s: %s", idx, name)); }
            else            { this.items.add(String.format("▶ %s", idx)); }
            this.itemToScaff.put(idx, cid);
        }

        if (this.items.size() == 0) { return false; }
        if (scaffId != ROOT_SCAFF) { // Cannot render at root scaff
            this.items.add("★ <HEAD>: Render at current scaff");
            this.itemToScaff.put("<HEAD>", scaffId);
        }

        return true;
    }

    // Extract item name and map to original scaff id
    // Returns (item name, scaff id)
    Pair<String, String> extractScaffIdFromItem(String item) {
        String idx = item.replace("▶", "").replace("★", "").split(":")[0].trim();
        return new Pair<String, String>(idx, itemToScaff.get(idx));
    }

    public void run() {
        //---------- Construct UI ----------//
        ui = terminalUIBuilder.build();
        eventLoop = ui.getEventLoop();

        // Fetch & load initial items
        this.currentScaffId = ROOT_SCAFF;
        loadOptions(ROOT_SCAFF);

        // Construct selection list
        list = new ListView<String>(items, ItemStyle.NOCHECK);
        list.setBorderPadding(1, 1, 1, 1);
        list.setTitle(" Select an option to scaffold ");
        list.setShowBorder(true);
        ui.configure(list);

        // Construct project name input box
        nameInput = new InputView();
        nameInput.setTitle(" Enter project name ");
        nameInput.setShowBorder(true);
        nameInput.setBorderPadding(1, 1, 1, 1);
        ui.configure(nameInput);

        // Construct grid
        grid = new GridView();
        grid.setRowSize(-1, -4);
        grid.setColumnSize(0);
        grid.addItem(nameInput, 0, 0, 1, 1, 0, 0);
        grid.addItem(list, 1, 0, 1, 1, 0, 0);

        // Construct render button
        // renderButton = new ButtonView("\u001B[92mRender\u001B[0m");
        renderButton = new ButtonView("Render");
        ui.configure(renderButton);

        // Construct app view
        app = new AppView(grid, new BoxView(), new BoxView());
        app.setShowBorder(true);
        app.setTitle(" Scaff selection ");
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
                else if (m.getPlainKey() == KeyEvent.Key.Enter && m.hasCtrl()) {
                    String txt = nameInput.getInputText().trim();
                    if (txt == "") {
                        nameInput.setTitle(" Enter project name - Must be non-empty");
                        return;
                    }
                    nameInput.setTitle(" Enter project name - " + txt);
                    ui.setFocus(list);
                }
			})
			.subscribe());

        // Handle item chosen - move to next question
        eventLoop.onDestroy(eventLoop.viewEvents(LISTVIEW_STRING_OPEN, list).subscribe(event -> {
            String chosen = event.args().item();
            if (chosen == null) return;

            //----- Fetch new items list -----//
            String itemName = "";
            if (extractScaffIdFromItem(chosen) instanceof Pair(String x, String y)) {
                itemName = x;
                currentScaffId = y;
            }

            //----- Check end reached -----//
            if (itemName.contains("<HEAD>") || !loadOptions(currentScaffId)) {
                // We have reached the end, obtain rendered scaff body & enter var sub mode

                //-- Fetch rendered & extract vars --//
                renderedBody = ProjectStructure.getRendered(currentScaffId);
                Map<String, String> vars = ProjectStructure.getAllVars(renderedBody);

                //-- Prepare varsub UI --//
                grid.clearItems();
                app.setTitle(" Substitute remaining variables ");

                // Construct row sizes
                int[] rowSizes = new int[vars.size() + 1];
                for (int r = 0; r < rowSizes.length; r++) { rowSizes[r] = -1; }   // var input
                rowSizes[rowSizes.length-1] = 3;                                    // render button

                // Construct var inputs
                varInputs = new HashMap<>();
                for (var e : vars.entrySet()) {
                    String varname = e.getKey();
                    String vardesc = e.getValue();
                    InputView varInput = new InputView();

                    varInput.setTitle(" " + varname + (vardesc != "" ? " - " + vardesc : "") + ": ");
                    varInput.setShowBorder(true);
                    varInput.setBorderPadding(1, 1, 1, 1);
                    varInputs.put(varname, varInput);
                    ui.configure(varInput);
                }

                // Construct button holder
                GridView buttonHolder = new GridView();
                buttonHolder.setRowSize(-1);
                buttonHolder.setColumnSize(-1, 24, -1);
                buttonHolder.addItem(renderButton, 0, 1, 1, 1, 0, 0);
                ui.configure(buttonHolder);

                // Add items to grid
                grid.setRowSize(rowSizes);
                int r = 0;
                for (var vi : varInputs.entrySet()) {
                    grid.addItem(vi.getValue(), r++, 0, 1, 1, 0, 0);
                }
                grid.addItem(buttonHolder, varInputs.size(), 0, 1, 1, 0, 0);
            }

            //----- Populate UI -----//
            list.setItems(items);
        }));

        renderButton.setAction(() -> {
            //-- Gather var subs --//
            varSubs = new HashMap<>();
            for (var vi : varInputs.entrySet()) {
                varSubs.put(vi.getKey(), vi.getValue().getInputText());
            }

            //-- Generate project files --//
            String inp = nameInput.getInputText().trim();
            String pname = (inp.length() == 0 ? DEFAULT_PROJECT_NAME : inp);
            ProjectStructure.renderFileSystem(pname, renderedBody.path("files"), varSubs);

            //-- Quit --//
            eventLoop.dispatch(ShellMessageBuilder.ofInterrupt());
            System.out.print("\u001B[H\u001B[2J\u001B[?25h"); // Clear screen & reset cursor pos + visibility
            System.out.println("\t\u001B[92m> Project created in " + pname + "/\u001B[0m\n");
            System.out.flush();
            System.exit(0);
            return;
        });

        //---------- Run UI ----------//
        ui.setRoot(app, true);
        ui.setFocus(list);
        try { ui.run(); } catch (EmissionException e) {
            System.out.println("\n\n\t\u001B[92m>Emission dropped\u001B[0m\n\n");
        }
    }
}