package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

public class TitleCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("title")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("targets", EntityArgument.players())
						.then(Commands.literal("clear").executes(commandContext -> clearTitle(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"))))
						.then(Commands.literal("reset").executes(commandContext -> resetTitle(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"))))
						.then(
							Commands.literal("title")
								.then(
									Commands.argument("title", ComponentArgument.textComponent())
										.executes(
											commandContext -> showTitle(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													ComponentArgument.getComponent(commandContext, "title"),
													"title",
													ClientboundSetTitleTextPacket::new
												)
										)
								)
						)
						.then(
							Commands.literal("subtitle")
								.then(
									Commands.argument("title", ComponentArgument.textComponent())
										.executes(
											commandContext -> showTitle(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													ComponentArgument.getComponent(commandContext, "title"),
													"subtitle",
													ClientboundSetSubtitleTextPacket::new
												)
										)
								)
						)
						.then(
							Commands.literal("actionbar")
								.then(
									Commands.argument("title", ComponentArgument.textComponent())
										.executes(
											commandContext -> showTitle(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													ComponentArgument.getComponent(commandContext, "title"),
													"actionbar",
													ClientboundSetActionBarTextPacket::new
												)
										)
								)
						)
						.then(
							Commands.literal("times")
								.then(
									Commands.argument("fadeIn", IntegerArgumentType.integer(0))
										.then(
											Commands.argument("stay", IntegerArgumentType.integer(0))
												.then(
													Commands.argument("fadeOut", IntegerArgumentType.integer(0))
														.executes(
															commandContext -> setTimes(
																	commandContext.getSource(),
																	EntityArgument.getPlayers(commandContext, "targets"),
																	IntegerArgumentType.getInteger(commandContext, "fadeIn"),
																	IntegerArgumentType.getInteger(commandContext, "stay"),
																	IntegerArgumentType.getInteger(commandContext, "fadeOut")
																)
														)
												)
										)
								)
						)
				)
		);
	}

	private static int clearTitle(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection) {
		ClientboundClearTitlesPacket clientboundClearTitlesPacket = new ClientboundClearTitlesPacket(false);

		for (ServerPlayer serverPlayer : collection) {
			serverPlayer.connection.send(clientboundClearTitlesPacket);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.title.cleared.single", ((ServerPlayer)collection.iterator().next()).getDisplayName()), true
			);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.title.cleared.multiple", collection.size()), true);
		}

		return collection.size();
	}

	private static int resetTitle(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection) {
		ClientboundClearTitlesPacket clientboundClearTitlesPacket = new ClientboundClearTitlesPacket(true);

		for (ServerPlayer serverPlayer : collection) {
			serverPlayer.connection.send(clientboundClearTitlesPacket);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.title.reset.single", ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.title.reset.multiple", collection.size()), true);
		}

		return collection.size();
	}

	private static int showTitle(
		CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Component component, String string, Function<Component, Packet<?>> function
	) throws CommandSyntaxException {
		for (ServerPlayer serverPlayer : collection) {
			serverPlayer.connection.send((Packet<?>)function.apply(ComponentUtils.updateForEntity(commandSourceStack, component, serverPlayer, 0)));
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.title.show." + string + ".single", ((ServerPlayer)collection.iterator().next()).getDisplayName()), true
			);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.title.show." + string + ".multiple", collection.size()), true);
		}

		return collection.size();
	}

	private static int setTimes(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, int i, int j, int k) {
		ClientboundSetTitlesAnimationPacket clientboundSetTitlesAnimationPacket = new ClientboundSetTitlesAnimationPacket(i, j, k);

		for (ServerPlayer serverPlayer : collection) {
			serverPlayer.connection.send(clientboundSetTitlesAnimationPacket);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.title.times.single", ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.title.times.multiple", collection.size()), true);
		}

		return collection.size();
	}
}
