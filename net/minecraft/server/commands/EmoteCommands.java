/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("me").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("action", MessageArgument.message()).executes(commandContext -> {
            MessageArgument.ChatMessage chatMessage = MessageArgument.getChatMessage(commandContext, "action");
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            PlayerList playerList = commandSourceStack.getServer().getPlayerList();
            chatMessage.resolve(commandSourceStack, filteredText -> playerList.broadcastChatMessage((FilteredText<PlayerChatMessage>)filteredText, commandSourceStack, ChatType.bind(ChatType.EMOTE_COMMAND, commandSourceStack)));
            return 1;
        })));
    }
}

