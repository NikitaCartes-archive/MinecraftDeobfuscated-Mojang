/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;

public class DeOpCommands {
    private static final SimpleCommandExceptionType ERROR_NOT_OP = new SimpleCommandExceptionType(Component.translatable("commands.deop.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("deop").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().getOpNames(), suggestionsBuilder)).executes(commandContext -> DeOpCommands.deopPlayers((CommandSourceStack)commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets")))));
    }

    private static int deopPlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
        PlayerList playerList = commandSourceStack.getServer().getPlayerList();
        int i = 0;
        for (GameProfile gameProfile : collection) {
            if (!playerList.isOp(gameProfile)) continue;
            playerList.deop(gameProfile);
            ++i;
            commandSourceStack.sendSuccess(Component.translatable("commands.deop.success", collection.iterator().next().getName()), true);
        }
        if (i == 0) {
            throw ERROR_NOT_OP.create();
        }
        commandSourceStack.getServer().kickUnlistedPlayers(commandSourceStack);
        return i;
    }
}

