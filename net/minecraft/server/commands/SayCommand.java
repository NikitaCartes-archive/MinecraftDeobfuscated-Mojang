/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;

public class SayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("say").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("message", MessageArgument.message()).executes(commandContext -> {
            MessageArgument.ChatMessage chatMessage = MessageArgument.getChatMessage(commandContext, "message");
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            PlayerList playerList = commandSourceStack.getServer().getPlayerList();
            chatMessage.resolve(commandSourceStack).thenAcceptAsync(filteredText -> playerList.broadcastChatMessage((FilteredText<PlayerChatMessage>)filteredText, commandSourceStack, ChatType.bind(ChatType.SAY_COMMAND, commandSourceStack)), (Executor)commandSourceStack.getServer());
            return 1;
        })));
    }
}

