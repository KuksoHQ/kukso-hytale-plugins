package com.kukso.hy.lib.commands.kuksolib;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.kukso.hy.lib.util.ColorUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PlayerInfoCommand extends AbstractPlayerCommand {

    private final RequiredArg<PlayerRef> targetArg;

    public PlayerInfoCommand() {
        super("playerinfo", "Get information about a player");
        //this.targetArg = targetArg; RequiredArg<PlayerRef> targetArg
        this.targetArg = withRequiredArg("player", "Player to teleport to", ArgTypes.PLAYER_REF);
        requirePermission("kukso.command.kuksolib.playerinfo");
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        PlayerRef target = commandContext.get(targetArg);

        if (target == null) {
            playerRef.sendMessage(ColorUtil.translation("&cPlayer not found: " + targetArg));
            return;
        }

        displayPlayerInfo(playerRef, target);
    }

    @SuppressWarnings("removal")
    private void displayPlayerInfo(PlayerRef sender, PlayerRef target) {
        //PlayerRef ref = target.getPlayerRef();
        PlayerRef ref = target;

        sender.sendMessage(ColorUtil.translation("&e&l=== Player Info: " + ref.getUsername() + " ==="));
        sender.sendMessage(ColorUtil.translation("&7UUID: &f" + target.getUuid()));
        sender.sendMessage(ColorUtil.translation("&7World: &f" + target.getWorldUuid()));
        sender.sendMessage(ColorUtil.translation("&7Language: &f" + ref.getLanguage()));
    }

}
