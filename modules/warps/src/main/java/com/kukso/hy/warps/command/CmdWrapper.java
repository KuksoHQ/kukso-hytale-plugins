package com.kukso.hy.warps.command;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.kukso.hy.lib.command.CmdInterface;

import java.util.Arrays;

public class CmdWrapper extends CommandBase {
    private final CmdInterface cmd;

    public CmdWrapper(CmdInterface cmd) {
        super(cmd.getName(), cmd.getDescription());
        this.cmd = cmd;
        setAllowsExtraArguments(true);
    }

    @Override
    protected void executeSync(CommandContext context) {
        String input = context.getInputString();
        String[] parts = input != null ? input.trim().split("\\s+") : new String[0];
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        cmd.execute(context.sender(), args);
    }
}
