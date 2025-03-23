package com.scaffoldcli.zapp;

import com.scaffoldcli.zapp.commands.CreateScaffCommand;
import com.scaffoldcli.zapp.commands.Init;
import com.scaffoldcli.zapp.lib.Text;
import com.scaffoldcli.zapp.net.ZappAPIRequest;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import static com.scaffoldcli.zapp.net.ZappAPIRequest.checkUserAuth;

// Much pain was suffered getting this bullshit to work
// For future reference, the holy comment which saved the day:
// [https://github.com/spring-projects/spring-shell/issues/1096#issuecomment-2210468543]

// Spring TUI docs here: [https://docs.spring.io/spring-shell/reference/tui/index.html]
// Reference repo here: [https://github.com/spring-projects/spring-shell]

@ShellComponent
public class CLICommand extends AbstractShellComponent {

    private final TerminalUIBuilder terminalUIBuilder;

    public CLICommand(TerminalUIBuilder terminalUIBuilder) {
        this.terminalUIBuilder = terminalUIBuilder;
    }

    @ShellMethod
    public void init() {
        Init cli = new Init(terminalUIBuilder);
        cli.run();
    }

    @ShellMethod
    public void create() {
        checkUserAuth();
        CreateScaffCommand cli = new CreateScaffCommand();
        cli.run();
    }

}
