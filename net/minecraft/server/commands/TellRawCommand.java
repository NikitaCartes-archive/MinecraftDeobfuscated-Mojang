/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class TellRawCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tellraw").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("targets", EntityArgument.players()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("message", ComponentArgument.textComponent()).executes(commandContext -> {
            int i = 0;
            for (ServerPlayer serverPlayer : EntityArgument.getPlayers(commandContext, "targets")) {
                serverPlayer.sendSystemMessage(ComponentUtils.updateForEntity((CommandSourceStack)commandContext.getSource(), ComponentArgument.getComponent(commandContext, "message"), (Entity)serverPlayer, 0), ChatType.TELLRAW_COMMAND);
                ++i;
            }
            return i;
        }))));
    }
}

