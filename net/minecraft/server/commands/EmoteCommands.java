/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("me").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("action", StringArgumentType.greedyString()).executes(commandContext -> {
            ((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().broadcastMessage(new TranslatableComponent("chat.type.emote", ((CommandSourceStack)commandContext.getSource()).getDisplayName(), StringArgumentType.getString(commandContext, "action")));
            return 1;
        })));
    }
}

