package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
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
													ClientboundSetTitlesPacket.Type.TITLE
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
													ClientboundSetTitlesPacket.Type.SUBTITLE
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
													ClientboundSetTitlesPacket.Type.ACTIONBAR
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
		ClientboundSetTitlesPacket clientboundSetTitlesPacket = new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.CLEAR, null);

		for (ServerPlayer serverPlayer : collection) {
			serverPlayer.connection.send(clientboundSetTitlesPacket);
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
		ClientboundSetTitlesPacket clientboundSetTitlesPacket = new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.RESET, null);

		for (ServerPlayer serverPlayer : collection) {
			serverPlayer.connection.send(clientboundSetTitlesPacket);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.title.reset.single", ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.title.reset.multiple", collection.size()), true);
		}

		return collection.size();
	}

	private static int showTitle(
		CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Component component, ClientboundSetTitlesPacket.Type type
	) throws CommandSyntaxException {
		for (ServerPlayer serverPlayer : collection) {
			serverPlayer.connection.send(new ClientboundSetTitlesPacket(type, ComponentUtils.updateForEntity(commandSourceStack, component, serverPlayer, 0)));
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				new TranslatableComponent(
					"commands.title.show." + type.name().toLowerCase(Locale.ROOT) + ".single", ((ServerPlayer)collection.iterator().next()).getDisplayName()
				),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.title.show." + type.name().toLowerCase(Locale.ROOT) + ".multiple", collection.size()), true
			);
		}

		return collection.size();
	}

	private static int setTimes(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, int i, int j, int k) {
		ClientboundSetTitlesPacket clientboundSetTitlesPacket = new ClientboundSetTitlesPacket(i, j, k);

		for (ServerPlayer serverPlayer : collection) {
			serverPlayer.connection.send(clientboundSetTitlesPacket);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.title.times.single", ((ServerPlayer)collection.iterator().next()).getDisplayName()), true);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.title.times.multiple", collection.size()), true);
		}

		return collection.size();
	}
}
