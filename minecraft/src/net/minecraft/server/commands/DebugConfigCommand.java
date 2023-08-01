package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public class DebugConfigCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("debugconfig")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.literal("config")
						.then(
							Commands.argument("target", EntityArgument.player())
								.executes(commandContext -> config(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "target")))
						)
				)
				.then(
					Commands.literal("unconfig")
						.then(
							Commands.argument("target", UuidArgument.uuid())
								.suggests(
									(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(getUuidsInConfig(commandContext.getSource().getServer()), suggestionsBuilder)
								)
								.executes(commandContext -> unconfig(commandContext.getSource(), UuidArgument.getUuid(commandContext, "target")))
						)
				)
		);
	}

	private static Iterable<String> getUuidsInConfig(MinecraftServer minecraftServer) {
		Set<String> set = new HashSet();

		for (Connection connection : minecraftServer.getConnection().getConnections()) {
			if (connection.getPacketListener() instanceof ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl) {
				set.add(serverConfigurationPacketListenerImpl.getOwner().getId().toString());
			}
		}

		return set;
	}

	private static int config(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer) {
		GameProfile gameProfile = serverPlayer.getGameProfile();
		serverPlayer.connection.switchToConfig();
		commandSourceStack.sendSuccess(() -> Component.literal("Switched player " + gameProfile.getName() + "(" + gameProfile.getId() + ") to config mode"), false);
		return 1;
	}

	private static int unconfig(CommandSourceStack commandSourceStack, UUID uUID) {
		for (Connection connection : commandSourceStack.getServer().getConnection().getConnections()) {
			PacketListener var5 = connection.getPacketListener();
			if (var5 instanceof ServerConfigurationPacketListenerImpl) {
				ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl = (ServerConfigurationPacketListenerImpl)var5;
				if (serverConfigurationPacketListenerImpl.getOwner().getId().equals(uUID)) {
					serverConfigurationPacketListenerImpl.returnToWorld();
				}
			}
		}

		commandSourceStack.sendFailure(Component.literal("Can't find player to unconfig"));
		return 0;
	}
}
