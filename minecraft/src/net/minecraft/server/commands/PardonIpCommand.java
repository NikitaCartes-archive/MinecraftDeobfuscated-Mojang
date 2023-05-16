package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.IpBanList;

public class PardonIpCommand {
	private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("commands.pardonip.invalid"));
	private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.pardonip.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("pardon-ip")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.argument("target", StringArgumentType.word())
						.suggests(
							(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
									commandContext.getSource().getServer().getPlayerList().getIpBans().getUserList(), suggestionsBuilder
								)
						)
						.executes(commandContext -> unban(commandContext.getSource(), StringArgumentType.getString(commandContext, "target")))
				)
		);
	}

	private static int unban(CommandSourceStack commandSourceStack, String string) throws CommandSyntaxException {
		if (!InetAddresses.isInetAddress(string)) {
			throw ERROR_INVALID.create();
		} else {
			IpBanList ipBanList = commandSourceStack.getServer().getPlayerList().getIpBans();
			if (!ipBanList.isBanned(string)) {
				throw ERROR_NOT_BANNED.create();
			} else {
				ipBanList.remove(string);
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.pardonip.success", string), true);
				return 1;
			}
		}
	}
}
