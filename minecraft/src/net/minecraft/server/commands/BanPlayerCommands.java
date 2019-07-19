package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

public class BanPlayerCommands {
	private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(new TranslatableComponent("commands.ban.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("ban")
				.requires(commandSourceStack -> commandSourceStack.getServer().getPlayerList().getBans().isEnabled() && commandSourceStack.hasPermission(3))
				.then(
					Commands.argument("targets", GameProfileArgument.gameProfile())
						.executes(commandContext -> banPlayers(commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets"), null))
						.then(
							Commands.argument("reason", MessageArgument.message())
								.executes(
									commandContext -> banPlayers(
											commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets"), MessageArgument.getMessage(commandContext, "reason")
										)
								)
						)
				)
		);
	}

	private static int banPlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection, @Nullable Component component) throws CommandSyntaxException {
		UserBanList userBanList = commandSourceStack.getServer().getPlayerList().getBans();
		int i = 0;

		for (GameProfile gameProfile : collection) {
			if (!userBanList.isBanned(gameProfile)) {
				UserBanListEntry userBanListEntry = new UserBanListEntry(
					gameProfile, null, commandSourceStack.getTextName(), null, component == null ? null : component.getString()
				);
				userBanList.add(userBanListEntry);
				i++;
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.ban.success", ComponentUtils.getDisplayName(gameProfile), userBanListEntry.getReason()), true
				);
				ServerPlayer serverPlayer = commandSourceStack.getServer().getPlayerList().getPlayer(gameProfile.getId());
				if (serverPlayer != null) {
					serverPlayer.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.banned"));
				}
			}
		}

		if (i == 0) {
			throw ERROR_ALREADY_BANNED.create();
		} else {
			return i;
		}
	}
}
