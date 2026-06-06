//package com.kukso.hy.warps.command;
//
//import com.hypixel.hytale.component.Ref;
//import com.hypixel.hytale.component.Store;
//import com.hypixel.hytale.server.core.command.system.CommandContext;
//import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
//import com.hypixel.hytale.server.core.universe.PlayerRef;
//import com.hypixel.hytale.server.core.universe.world.World;
//import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
//import com.kukso.hy.lib.command.CommandInterface;
//
//import java.util.Arrays;
//
///**
// * Wraps a CmdInterface as an AbstractPlayerCommand for standalone player commands.
// * This allows using the centralized CmdInterface pattern while still registering
// * commands as standalone player commands (e.g., /delwarp instead of /warps del).
// */
//public class PlayerCmdWrapper extends AbstractPlayerCommand {
//    private final CommandInterface cmd;
//
//    public PlayerCmdWrapper(CommandInterface cmd) {
//        super(cmd.getName(), cmd.getDescription());
//        this.cmd = cmd;
//        setAllowsExtraArguments(true);
//    }
//
//    @Override
//    protected boolean canGeneratePermission() {
//        return false;
//    }
//
//    @Override
//    protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef player, World world) {
//        String input = ctx.getInputString();
//        String[] parts = input != null ? input.trim().split("\\s+") : new String[0];
//        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
//
//        cmd.execute(ctx.sender(), args);
//    }
//}
