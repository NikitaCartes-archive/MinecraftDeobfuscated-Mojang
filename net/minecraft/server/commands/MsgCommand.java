/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class MsgCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("msg").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.players()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("message", MessageArgument.message()).executes(commandContext -> MsgCommand.sendMessage((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), MessageArgument.getMessage(commandContext, "message"))))));
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("tell").redirect(literalCommandNode));
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("w").redirect(literalCommandNode));
    }

    private static int sendMessage(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Component component) {
        Consumer<Component> consumer;
        UUID uUID = commandSourceStack.getEntity() == null ? Util.NIL_UUID : commandSourceStack.getEntity().getUUID();
        Entity entity = commandSourceStack.getEntity();
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            consumer = component2 -> serverPlayer.sendMessage(Component.translatable("commands.message.display.outgoing", component2, component).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), serverPlayer.getUUID());
        } else {
            consumer = component2 -> commandSourceStack.sendSuccess(Component.translatable("commands.message.display.outgoing", component2, component).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        }
        for (ServerPlayer serverPlayer2 : collection) {
            consumer.accept(serverPlayer2.getDisplayName());
            serverPlayer2.sendMessage(Component.translatable("commands.message.display.incoming", commandSourceStack.getDisplayName(), component).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), uUID);
        }
        return collection.size();
    }
}

