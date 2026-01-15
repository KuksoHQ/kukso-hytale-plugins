package com.kukso.hy.lib;

import com.kukso.hy.lib.command.CmdRegistrar;
import com.kukso.hy.lib.command.commands.HelpCommand;
import com.kukso.hy.lib.command.commands.ReloadCommand;

public class Main {

    private static Main instance;
    private CmdRegistrar cmdRegistrar;

    public static Main getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;

        // Initialize command registrar
        cmdRegistrar = new CmdRegistrar();

        // Register all commands
        registerCommands();

        System.out.println("[KuksoLib] Enabled successfully!");
    }

    public void onDisable() {
        if (cmdRegistrar != null) {
            cmdRegistrar.clear();
        }
        System.out.println("[KuksoLib] Disabled successfully!");
    }

    private void registerCommands() {
        cmdRegistrar.registerAll(
                new HelpCommand(),
                new ReloadCommand()
        );

        System.out.println("[KuksoLib] Registered " + cmdRegistrar.getCommandCount() + " commands.");
    }

    public CmdRegistrar getCmdRegistrar() {
        return cmdRegistrar;
    }
}
