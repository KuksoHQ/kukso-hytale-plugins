package com.kukso.hy.lib.command;

import java.util.List;

public interface CmdInterface {

    String getName();

    String getDescription();

    String getUsage();

    String getPermission();

    List<String> getAliases();

    boolean execute(CommandSender sender, String label, String[] args);

    List<String> tabComplete(CommandSender sender, String[] args);
}
