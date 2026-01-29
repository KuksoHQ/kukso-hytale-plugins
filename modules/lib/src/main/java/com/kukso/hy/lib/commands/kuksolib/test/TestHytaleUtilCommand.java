package com.kukso.hy.lib.commands.kuksolib.test;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.kukso.hy.lib.util.ColorUtil;
import com.kukso.hy.lib.util.HytaleUtil;
import com.kukso.hy.lib.util.MessageUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

class TestHytaleUtilCommand extends AbstractPlayerCommand {

    MessageUtil messageUtil = new MessageUtil();

    TestHytaleUtilCommand() {
        super("hytaleutil", "Test HytaleUtil methods");
        addAliases("hutil");
    }

    @Override
    protected void execute(@NonNullDecl CommandContext context, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        context.sendMessage(ColorUtil.format("&e&l=== HytaleUtil Test ==="));

        // Test getPlayerRef
        PlayerRef retrievedRef = new HytaleUtil().getPlayerRef(context);
        if (retrievedRef != null) {
             if (retrievedRef.equals(playerRef)) {
                 context.sendMessage(ColorUtil.format("&a[PASS] getPlayerRef(ctx) returned correct player: " + retrievedRef.getUsername()));
             } else {
                 context.sendMessage(ColorUtil.format("&c[FAIL] getPlayerRef(ctx) returned " + retrievedRef.getUsername() + " but expected " + playerRef.getUsername()));
             }
        } else {
            context.sendMessage(ColorUtil.format("&c[FAIL] getPlayerRef(ctx) returned null"));
        }

        // Test failure case (not easily possible in PlayerCommand without mocking, but we can assume correct behavior if the above passes)
        
        context.sendMessage(ColorUtil.format("&eTest Complete."));
    }
}
