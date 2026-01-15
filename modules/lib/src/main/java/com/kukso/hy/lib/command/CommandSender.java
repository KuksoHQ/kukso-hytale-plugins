package com.kukso.hy.lib.command;

public interface CommandSender {

    String getName();

    void sendMessage(String message);

    void sendMessage(String... messages);

    boolean hasPermission(String permission);

    boolean isPlayer();

    boolean isConsole();
}
