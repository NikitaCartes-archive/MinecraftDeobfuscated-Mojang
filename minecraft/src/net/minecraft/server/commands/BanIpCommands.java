package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

public class BanIpCommands {
	private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType(Component.translatable("commands.banip.invalid"));
	private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.banip.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("ban-ip")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.argument("target", StringArgumentType.word())
						.executes(commandContext -> banIpOrName(commandContext.getSource(), StringArgumentType.getString(commandContext, "target"), null))
						.then(
							Commands.argument("reason", MessageArgument.message())
								.executes(
									commandContext -> banIpOrName(
											commandContext.getSource(), StringArgumentType.getString(commandContext, "target"), MessageArgument.getMessage(commandContext, "reason")
										)
								)
						)
				)
		);
	}

	private static int banIpOrName(CommandSourceStack commandSourceStack, String string, @Nullable Component component) throws CommandSyntaxException {
		if (InetAddresses.isInetAddress(string)) {
			return banIp(commandSourceStack, string, component);
		} else {
			ServerPlayer serverPlayer = commandSourceStack.getServer().getPlayerList().getPlayerByName(string);
			if (serverPlayer != null) {
				return banIp(commandSourceStack, serverPlayer.getIpAddress(), component);
			} else {
				throw ERROR_INVALID_IP.create();
			}
		}
	}

	private static int banIp(CommandSourceStack commandSourceStack, String string, @Nullable Component component) throws CommandSyntaxException {
		IpBanList ipBanList = commandSourceStack.getServer().getPlayerList().getIpBans();
		if (ipBanList.isBanned(string)) {
			throw ERROR_ALREADY_BANNED.create();
		} else {
			List<ServerPlayer> list = commandSourceStack.getServer().getPlayerList().getPlayersWithAddress(string);
			IpBanListEntry ipBanListEntry = new IpBanListEntry(string, null, commandSourceStack.getTextName(), null, component == null ? null : component.getString());
			ipBanList.add(ipBanListEntry);
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.banip.success", string, ipBanListEntry.getReason()), true);
			if (!list.isEmpty()) {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.banip.info", list.size(), EntitySelector.joinNames(list)), true);
			}

			for (ServerPlayer serverPlayer : list) {
				serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned"));
			}

			return list.size();
		}
	}
}
