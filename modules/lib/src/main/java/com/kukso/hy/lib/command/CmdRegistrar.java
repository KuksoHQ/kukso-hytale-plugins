package com.kukso.hy.lib.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmdRegistrar {

    private static CmdRegistrar instance;
    private final Map<String, CmdInterface> commands;
    private final Map<String, String> aliasMap;

    public CmdRegistrar() {
        instance = this;
        this.commands = new HashMap<>();
        this.aliasMap = new HashMap<>();
    }

    public static CmdRegistrar getInstance() {
        return instance;
    }

    public void register(CmdInterface command) {
        String name = command.getName().toLowerCase();
        commands.put(name, command);

        for (String alias : command.getAliases()) {
            aliasMap.put(alias.toLowerCase(), name);
        }
    }

    public void registerAll(CmdInterface... commands) {
        for (CmdInterface command : commands) {
            register(command);
        }
    }

    public void unregister(String name) {
        CmdInterface command = commands.remove(name.toLowerCase());
        if (command != null) {
            for (String alias : command.getAliases()) {
                aliasMap.remove(alias.toLowerCase());
            }
        }
    }

    public CmdInterface getCommand(String name) {
        String lowerName = name.toLowerCase();
        CmdInterface command = commands.get(lowerName);
        if (command == null) {
            String mappedName = aliasMap.get(lowerName);
            if (mappedName != null) {
                command = commands.get(mappedName);
            }
        }
        return command;
    }

    public boolean dispatch(CommandSender sender, String commandLine) {
        String[] parts = commandLine.split(" ");
        if (parts.length == 0) {
            return false;
        }

        String label = parts[0];
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        return dispatch(sender, label, args);
    }

    public boolean dispatch(CommandSender sender, String label, String[] args) {
        CmdInterface command = getCommand(label);
        if (command == null) {
            sender.sendMessage("Unknown command: " + label);
            return false;
        }

        String permission = command.getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            sender.sendMessage("You don't have permission to use this command.");
            return false;
        }

        return command.execute(sender, label, args);
    }

    public List<String> tabComplete(CommandSender sender, String commandLine) {
        String[] parts = commandLine.split(" ", -1);
        if (parts.length == 0) {
            return new ArrayList<>();
        }

        if (parts.length == 1) {
            List<String> completions = new ArrayList<>();
            String prefix = parts[0].toLowerCase();
            for (String name : commands.keySet()) {
                if (name.startsWith(prefix)) {
                    completions.add(name);
                }
            }
            for (String alias : aliasMap.keySet()) {
                if (alias.startsWith(prefix)) {
                    completions.add(alias);
                }
            }
            return completions;
        }

        String label = parts[0];
        CmdInterface command = getCommand(label);
        if (command == null) {
            return new ArrayList<>();
        }

        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        return command.tabComplete(sender, args);
    }

    public List<CmdInterface> getCommands() {
        return new ArrayList<>(commands.values());
    }

    public int getCommandCount() {
        return commands.size();
    }

    public void clear() {
        commands.clear();
        aliasMap.clear();
    }
}
