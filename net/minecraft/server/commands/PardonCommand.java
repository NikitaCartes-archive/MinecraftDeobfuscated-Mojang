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
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.players.UserBanList;

public class PardonCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.pardon.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("pardon").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().getBans().getUserList(), suggestionsBuilder)).executes(commandContext -> PardonCommand.pardonPlayers((CommandSourceStack)commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets")))));
    }

    private static int pardonPlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
        UserBanList userBanList = commandSourceStack.getServer().getPlayerList().getBans();
        int i = 0;
        for (GameProfile gameProfile : collection) {
            if (!userBanList.isBanned(gameProfile)) continue;
            userBanList.remove(gameProfile);
            ++i;
            commandSourceStack.sendSuccess(Component.translatable("commands.pardon.success", ComponentUtils.getDisplayName(gameProfile)), true);
        }
        if (i == 0) {
            throw ERROR_NOT_BANNED.create();
        }
        return i;
    }
}

