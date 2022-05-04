/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.SignedMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.players.PlayerList;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("me").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("action", MessageArgument.message()).executes(commandContext -> {
            SignedMessage signedMessage = MessageArgument.getSignedMessage(commandContext, "action");
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            if (commandSourceStack.isPlayer()) {
                ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
                serverPlayer.getTextFilter().processStreamMessage(signedMessage.content().getString()).thenAcceptAsync(filteredText -> {
                    PlayerList playerList = commandSourceStack.getServer().getPlayerList();
                    playerList.broadcastChatMessage(signedMessage, (TextFilter.FilteredText)filteredText, serverPlayer, ChatType.EMOTE_COMMAND);
                }, (Executor)commandSourceStack.getServer());
            } else {
                commandSourceStack.getServer().getPlayerList().broadcastChatMessage(signedMessage, commandSourceStack.asChatSender(), ChatType.EMOTE_COMMAND);
            }
            return 1;
        })));
    }
}

