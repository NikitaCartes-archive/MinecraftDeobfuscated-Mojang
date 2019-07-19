/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class SayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("say").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("message", MessageArgument.message()).executes(commandContext -> {
            Component component = MessageArgument.getMessage(commandContext, "message");
            ((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().broadcastMessage(new TranslatableComponent("chat.type.announcement", ((CommandSourceStack)commandContext.getSource()).getDisplayName(), component));
            return 1;
        })));
    }
}

