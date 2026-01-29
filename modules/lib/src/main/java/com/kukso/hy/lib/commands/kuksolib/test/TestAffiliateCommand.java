package com.kukso.hy.lib.commands.kuksolib.test;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.kukso.hy.lib.util.MessageUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

class TestAffiliateCommand extends AbstractPlayerCommand {

    MessageUtil messageUtil = new MessageUtil();

    TestAffiliateCommand() {
        super("affiliate", "Show affiliate link");
        addAliases("sponsor");
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {

    }
}
