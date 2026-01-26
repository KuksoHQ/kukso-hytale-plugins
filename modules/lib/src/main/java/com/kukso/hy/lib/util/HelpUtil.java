package com.kukso.hy.lib.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.pages.CommandListPage;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class HelpUtil {
    private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
    @Nonnull
    public static CompletableFuture<Void> openHelpUI(@Nonnull CommandContext context, @Nullable String initialCommand) {
        if (context.isPlayer()) {
            Ref<EntityStore> playerRef = context.senderAsPlayerRef();
            if (playerRef != null && playerRef.isValid()) {
                Store<EntityStore> store = playerRef.getStore();
                World world = ((EntityStore)store.getExternalData()).getWorld();
                String resolvedCommand = resolveCommandName(initialCommand);
                return CompletableFuture.runAsync(() -> {
                    Player playerComponent = (Player)store.getComponent(playerRef, Player.getComponentType());
                    PlayerRef playerRefComponent = (PlayerRef)store.getComponent(playerRef, PlayerRef.getComponentType());
                    if (playerComponent != null && playerRefComponent != null) {
                        playerComponent.getPageManager().openCustomPage(playerRef, store, new CommandListPage(playerRefComponent, resolvedCommand));
                    }

                }, world);
            } else {
                context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
                return CompletableFuture.completedFuture((Void) null);
            }
        } else {
            return CompletableFuture.completedFuture((Void) null);
        }
    }

    @Nullable
    private static String resolveCommandName(@Nullable String commandNameOrAlias) {
        if (commandNameOrAlias == null) {
            return null;
        } else {
            String lowerName = commandNameOrAlias.toLowerCase();
            Map<String, AbstractCommand> commands = CommandManager.get().getCommandRegistration();
            if (commands.containsKey(lowerName)) {
                return lowerName;
            } else {
                for(Map.Entry<String, AbstractCommand> entry : commands.entrySet()) {
                    Set<String> aliases = ((AbstractCommand)entry.getValue()).getAliases();
                    if (aliases != null && aliases.contains(lowerName)) {
                        return (String)entry.getKey();
                    }
                }

                return lowerName;
            }
        }
    }
}
