package com.kukso.hy.lib.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseCommand implements CmdInterface {

    private final String name;
    private final String description;
    private final String usage;
    private final String permission;
    private final List<String> aliases;

    protected BaseCommand(String name, String description) {
        this(name, description, "/" + name, null);
    }

    protected BaseCommand(String name, String description, String usage, String permission, String... aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.aliases = new ArrayList<>(Arrays.asList(aliases));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    protected void sendUsage(CommandSender sender) {
        sender.sendMessage("Usage: " + usage);
    }
}
