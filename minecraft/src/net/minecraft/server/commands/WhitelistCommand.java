package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
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
	private static final SimpleCommandExceptionType ERROR_ALREADY_ENABLED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.whitelist.alreadyOn")
	);
	private static final SimpleCommandExceptionType ERROR_ALREADY_DISABLED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.whitelist.alreadyOff")
	);
	private static final SimpleCommandExceptionType ERROR_ALREADY_WHITELISTED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.whitelist.add.failed")
	);
	private static final SimpleCommandExceptionType ERROR_NOT_WHITELISTED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.whitelist.remove.failed")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("whitelist")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(Commands.literal("on").executes(commandContext -> enableWhitelist(commandContext.getSource())))
				.then(Commands.literal("off").executes(commandContext -> disableWhitelist(commandContext.getSource())))
				.then(Commands.literal("list").executes(commandContext -> showList(commandContext.getSource())))
				.then(
					Commands.literal("add")
						.then(
							Commands.argument("targets", GameProfileArgument.gameProfile())
								.suggests(
									(commandContext, suggestionsBuilder) -> {
										PlayerList playerList = commandContext.getSource().getServer().getPlayerList();
										return SharedSuggestionProvider.suggest(
											playerList.getPlayers()
												.stream()
												.filter(serverPlayer -> !playerList.getWhiteList().isWhiteListed(serverPlayer.getGameProfile()))
												.map(serverPlayer -> serverPlayer.getGameProfile().getName()),
											suggestionsBuilder
										);
									}
								)
								.executes(commandContext -> addPlayers(commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets")))
						)
				)
				.then(
					Commands.literal("remove")
						.then(
							Commands.argument("targets", GameProfileArgument.gameProfile())
								.suggests(
									(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
											commandContext.getSource().getServer().getPlayerList().getWhiteListNames(), suggestionsBuilder
										)
								)
								.executes(commandContext -> removePlayers(commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets")))
						)
				)
				.then(Commands.literal("reload").executes(commandContext -> reload(commandContext.getSource())))
		);
	}

	private static int reload(CommandSourceStack commandSourceStack) {
		commandSourceStack.getServer().getPlayerList().reloadWhiteList();
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.reloaded"), true);
		commandSourceStack.getServer().kickUnlistedPlayers(commandSourceStack);
		return 1;
	}

	private static int addPlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
		UserWhiteList userWhiteList = commandSourceStack.getServer().getPlayerList().getWhiteList();
		int i = 0;

		for (GameProfile gameProfile : collection) {
			if (!userWhiteList.isWhiteListed(gameProfile)) {
				UserWhiteListEntry userWhiteListEntry = new UserWhiteListEntry(gameProfile);
				userWhiteList.add(userWhiteListEntry);
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.add.success", ComponentUtils.getDisplayName(gameProfile)), true);
				i++;
			}
		}

		if (i == 0) {
			throw ERROR_ALREADY_WHITELISTED.create();
		} else {
			return i;
		}
	}

	private static int removePlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
		UserWhiteList userWhiteList = commandSourceStack.getServer().getPlayerList().getWhiteList();
		int i = 0;

		for (GameProfile gameProfile : collection) {
			if (userWhiteList.isWhiteListed(gameProfile)) {
				UserWhiteListEntry userWhiteListEntry = new UserWhiteListEntry(gameProfile);
				userWhiteList.remove(userWhiteListEntry);
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.remove.success", ComponentUtils.getDisplayName(gameProfile)), true);
				i++;
			}
		}

		if (i == 0) {
			throw ERROR_NOT_WHITELISTED.create();
		} else {
			commandSourceStack.getServer().kickUnlistedPlayers(commandSourceStack);
			return i;
		}
	}

	private static int enableWhitelist(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		PlayerList playerList = commandSourceStack.getServer().getPlayerList();
		if (playerList.isUsingWhitelist()) {
			throw ERROR_ALREADY_ENABLED.create();
		} else {
			playerList.setUsingWhiteList(true);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.enabled"), true);
			commandSourceStack.getServer().kickUnlistedPlayers(commandSourceStack);
			return 1;
		}
	}

	private static int disableWhitelist(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		PlayerList playerList = commandSourceStack.getServer().getPlayerList();
		if (!playerList.isUsingWhitelist()) {
			throw ERROR_ALREADY_DISABLED.create();
		} else {
			playerList.setUsingWhiteList(false);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.disabled"), true);
			return 1;
		}
	}

	private static int showList(CommandSourceStack commandSourceStack) {
		String[] strings = commandSourceStack.getServer().getPlayerList().getWhiteListNames();
		if (strings.length == 0) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.none"), false);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.whitelist.list", strings.length, String.join(", ", strings)), false);
		}

		return strings.length;
	}
}
