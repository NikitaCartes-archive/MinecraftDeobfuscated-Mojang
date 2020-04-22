/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class StopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("stop").requires(commandSourceStack -> commandSourceStack.hasPermission(4))).executes(commandContext -> {
            ((CommandSourceStack)commandContext.getSource()).sendSuccess(new TranslatableComponent("commands.stop.stopping"), true);
            ((CommandSourceStack)commandContext.getSource()).getServer().halt(false);
            return 1;
        }));
    }
}

