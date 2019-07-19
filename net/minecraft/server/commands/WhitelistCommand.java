/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;

public class WhitelistCommand {
    private static final SimpleCommandExceptionType ERROR_ALREADY_ENABLED = new SimpleCommandExceptionType(new TranslatableComponent("commands.whitelist.alreadyOn", new Object[0]));
    private static final SimpleCommandExceptionType ERROR_ALREADY_DISABLED = new SimpleCommandExceptionType(new TranslatableComponent("commands.whitelist.alreadyOff", new Object[0]));
    private static final SimpleCommandExceptionType ERROR_ALREADY_WHITELISTED = new SimpleCommandExceptionType(new TranslatableComponent("commands.whitelist.add.failed", new Object[0]));
    private static final SimpleCommandExceptionType ERROR_NOT_WHITELISTED = new SimpleCommandExceptionType(new TranslatableComponent("commands.whitelist.remove.failed", new Object[0]));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("whitelist").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(Commands.literal("on").executes(commandContext -> WhitelistCommand.enableWhitelist((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("off").executes(commandContext -> WhitelistCommand.disableWhitelist((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("list").executes(commandContext -> WhitelistCommand.showList((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("add").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
            PlayerList playerList = ((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList();
            return SharedSuggestionProvider.suggest(playerList.getPlayers().stream().filter(serverPlayer -> !playerList.getWhiteList().isWhiteListed(serverPlayer.getGameProfile())).map(serverPlayer -> serverPlayer.getGameProfile().getName()), suggestionsBuilder);
        }).executes(commandContext -> WhitelistCommand.addPlayers((CommandSourceStack)commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets")))))).then(Commands.literal("remove").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().getWhiteListNames(), suggestionsBuilder)).executes(commandContext -> WhitelistCommand.removePlayers((CommandSourceStack)commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets")))))).then(Commands.literal("reload").executes(commandContext -> WhitelistCommand.reload((CommandSourceStack)commandContext.getSource()))));
    }

    private static int reload(CommandSourceStack commandSourceStack) {
        commandSourceStack.getServer().getPlayerList().reloadWhiteList();
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.reloaded", new Object[0]), true);
        commandSourceStack.getServer().kickUnlistedPlayers(commandSourceStack);
        return 1;
    }

    private static int addPlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
        UserWhiteList userWhiteList = commandSourceStack.getServer().getPlayerList().getWhiteList();
        int i = 0;
        for (GameProfile gameProfile : collection) {
            if (userWhiteList.isWhiteListed(gameProfile)) continue;
            UserWhiteListEntry userWhiteListEntry = new UserWhiteListEntry(gameProfile);
            userWhiteList.add(userWhiteListEntry);
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.add.success", ComponentUtils.getDisplayName(gameProfile)), true);
            ++i;
        }
        if (i == 0) {
            throw ERROR_ALREADY_WHITELISTED.create();
        }
        return i;
    }

    private static int removePlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
        UserWhiteList userWhiteList = commandSourceStack.getServer().getPlayerList().getWhiteList();
        int i = 0;
        for (GameProfile gameProfile : collection) {
            if (!userWhiteList.isWhiteListed(gameProfile)) continue;
            UserWhiteListEntry userWhiteListEntry = new UserWhiteListEntry(gameProfile);
            userWhiteList.remove(userWhiteListEntry);
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.remove.success", ComponentUtils.getDisplayName(gameProfile)), true);
            ++i;
        }
        if (i == 0) {
            throw ERROR_NOT_WHITELISTED.create();
        }
        commandSourceStack.getServer().kickUnlistedPlayers(commandSourceStack);
        return i;
    }

    private static int enableWhitelist(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        PlayerList playerList = commandSourceStack.getServer().getPlayerList();
        if (playerList.isUsingWhitelist()) {
            throw ERROR_ALREADY_ENABLED.create();
        }
        playerList.setUsingWhiteList(true);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.enabled", new Object[0]), true);
        commandSourceStack.getServer().kickUnlistedPlayers(commandSourceStack);
        return 1;
    }

    private static int disableWhitelist(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        PlayerList playerList = commandSourceStack.getServer().getPlayerList();
        if (!playerList.isUsingWhitelist()) {
            throw ERROR_ALREADY_DISABLED.create();
        }
        playerList.setUsingWhiteList(false);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.disabled", new Object[0]), true);
        return 1;
    }

    private static int showList(CommandSourceStack commandSourceStack) {
        CharSequence[] strings = commandSourceStack.getServer().getPlayerList().getWhiteListNames();
        if (strings.length == 0) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.none", new Object[0]), false);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.list", strings.length, String.join((CharSequence)", ", strings)), false);
        }
        return strings.length;
    }
}

