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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
    private static final Style SUGGEST_STYLE = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.type.team.hover"))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
    private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(Component.translatable("commands.teammsg.failed.noteam"));

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
        MutableComponent component2 = playerTeam.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
        List<ServerPlayer> list = commandSourceStack.getServer().getPlayerList().getPlayers();
        for (ServerPlayer serverPlayer : list) {
            if (serverPlayer == entity) {
                serverPlayer.sendUnsignedMessageFrom(Component.translatable("chat.type.team.sent", component2, commandSourceStack.getDisplayName(), component), entity.getUUID());
                continue;
            }
            if (serverPlayer.getTeam() != playerTeam) continue;
            serverPlayer.sendUnsignedMessageFrom(Component.translatable("chat.type.team.text", component2, commandSourceStack.getDisplayName(), component), entity.getUUID());
        }
        return list.size();
    }
}

