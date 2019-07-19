/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(new TranslatableComponent("commands.teammsg.failed.noteam", new Object[0]));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("teammsg").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("message", MessageArgument.message()).executes(commandContext -> TeamMsgCommand.sendMessage((CommandSourceStack)commandContext.getSource(), MessageArgument.getMessage(commandContext, "message")))));
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("tm").redirect(literalCommandNode));
    }

    private static int sendMessage(CommandSourceStack commandSourceStack, Component component) throws CommandSyntaxException {
        Entity entity = commandSourceStack.getEntityOrException();
        PlayerTeam playerTeam = (PlayerTeam)entity.getTeam();
        if (playerTeam == null) {
            throw ERROR_NOT_ON_TEAM.create();
        }
        Consumer<Style> consumer = style -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.type.team.hover", new Object[0]))).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
        Component component2 = playerTeam.getFormattedDisplayName().withStyle(consumer);
        for (Component component3 : component2.getSiblings()) {
            component3.withStyle(consumer);
        }
        List<ServerPlayer> list = commandSourceStack.getServer().getPlayerList().getPlayers();
        for (ServerPlayer serverPlayer : list) {
            if (serverPlayer == entity) {
                serverPlayer.sendMessage(new TranslatableComponent("chat.type.team.sent", component2, commandSourceStack.getDisplayName(), component.deepCopy()));
                continue;
            }
            if (serverPlayer.getTeam() != playerTeam) continue;
            serverPlayer.sendMessage(new TranslatableComponent("chat.type.team.text", component2, commandSourceStack.getDisplayName(), component.deepCopy()));
        }
        return list.size();
    }
}

