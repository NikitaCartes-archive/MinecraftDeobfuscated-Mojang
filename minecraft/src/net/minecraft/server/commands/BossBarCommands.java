package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;

public class BossBarCommands {
	private static final DynamicCommandExceptionType ERROR_ALREADY_EXISTS = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.bossbar.create.failed", object)
	);
	private static final DynamicCommandExceptionType ERROR_DOESNT_EXIST = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.bossbar.unknown", object)
	);
	private static final SimpleCommandExceptionType ERROR_NO_PLAYER_CHANGE = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.bossbar.set.players.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_NO_NAME_CHANGE = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.bossbar.set.name.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_NO_COLOR_CHANGE = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.bossbar.set.color.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_NO_STYLE_CHANGE = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.bossbar.set.style.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_NO_VALUE_CHANGE = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.bossbar.set.value.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_NO_MAX_CHANGE = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.bossbar.set.max.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_ALREADY_HIDDEN = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.bossbar.set.visibility.unchanged.hidden")
	);
	private static final SimpleCommandExceptionType ERROR_ALREADY_VISIBLE = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.bossbar.set.visibility.unchanged.visible")
	);
	public static final SuggestionProvider<CommandSourceStack> SUGGEST_BOSS_BAR = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(
			commandContext.getSource().getServer().getCustomBossEvents().getIds(), suggestionsBuilder
		);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("bossbar")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("add")
						.then(
							Commands.argument("id", ResourceLocationArgument.id())
								.then(
									Commands.argument("name", ComponentArgument.textComponent())
										.executes(
											commandContext -> createBar(
													commandContext.getSource(), ResourceLocationArgument.getId(commandContext, "id"), ComponentArgument.getComponent(commandContext, "name")
												)
										)
								)
						)
				)
				.then(
					Commands.literal("remove")
						.then(
							Commands.argument("id", ResourceLocationArgument.id())
								.suggests(SUGGEST_BOSS_BAR)
								.executes(commandContext -> removeBar(commandContext.getSource(), getBossBar(commandContext)))
						)
				)
				.then(Commands.literal("list").executes(commandContext -> listBars(commandContext.getSource())))
				.then(
					Commands.literal("set")
						.then(
							Commands.argument("id", ResourceLocationArgument.id())
								.suggests(SUGGEST_BOSS_BAR)
								.then(
									Commands.literal("name")
										.then(
											Commands.argument("name", ComponentArgument.textComponent())
												.executes(commandContext -> setName(commandContext.getSource(), getBossBar(commandContext), ComponentArgument.getComponent(commandContext, "name")))
										)
								)
								.then(
									Commands.literal("color")
										.then(
											Commands.literal("pink").executes(commandContext -> setColor(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarColor.PINK))
										)
										.then(
											Commands.literal("blue").executes(commandContext -> setColor(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarColor.BLUE))
										)
										.then(
											Commands.literal("red").executes(commandContext -> setColor(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarColor.RED))
										)
										.then(
											Commands.literal("green").executes(commandContext -> setColor(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarColor.GREEN))
										)
										.then(
											Commands.literal("yellow")
												.executes(commandContext -> setColor(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarColor.YELLOW))
										)
										.then(
											Commands.literal("purple")
												.executes(commandContext -> setColor(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarColor.PURPLE))
										)
										.then(
											Commands.literal("white").executes(commandContext -> setColor(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarColor.WHITE))
										)
								)
								.then(
									Commands.literal("style")
										.then(
											Commands.literal("progress")
												.executes(commandContext -> setStyle(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarOverlay.PROGRESS))
										)
										.then(
											Commands.literal("notched_6")
												.executes(commandContext -> setStyle(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarOverlay.NOTCHED_6))
										)
										.then(
											Commands.literal("notched_10")
												.executes(commandContext -> setStyle(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarOverlay.NOTCHED_10))
										)
										.then(
											Commands.literal("notched_12")
												.executes(commandContext -> setStyle(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarOverlay.NOTCHED_12))
										)
										.then(
											Commands.literal("notched_20")
												.executes(commandContext -> setStyle(commandContext.getSource(), getBossBar(commandContext), BossEvent.BossBarOverlay.NOTCHED_20))
										)
								)
								.then(
									Commands.literal("value")
										.then(
											Commands.argument("value", IntegerArgumentType.integer(0))
												.executes(
													commandContext -> setValue(commandContext.getSource(), getBossBar(commandContext), IntegerArgumentType.getInteger(commandContext, "value"))
												)
										)
								)
								.then(
									Commands.literal("max")
										.then(
											Commands.argument("max", IntegerArgumentType.integer(1))
												.executes(commandContext -> setMax(commandContext.getSource(), getBossBar(commandContext), IntegerArgumentType.getInteger(commandContext, "max")))
										)
								)
								.then(
									Commands.literal("visible")
										.then(
											Commands.argument("visible", BoolArgumentType.bool())
												.executes(commandContext -> setVisible(commandContext.getSource(), getBossBar(commandContext), BoolArgumentType.getBool(commandContext, "visible")))
										)
								)
								.then(
									Commands.literal("players")
										.executes(commandContext -> setPlayers(commandContext.getSource(), getBossBar(commandContext), Collections.emptyList()))
										.then(
											Commands.argument("targets", EntityArgument.players())
												.executes(
													commandContext -> setPlayers(commandContext.getSource(), getBossBar(commandContext), EntityArgument.getOptionalPlayers(commandContext, "targets"))
												)
										)
								)
						)
				)
				.then(
					Commands.literal("get")
						.then(
							Commands.argument("id", ResourceLocationArgument.id())
								.suggests(SUGGEST_BOSS_BAR)
								.then(Commands.literal("value").executes(commandContext -> getValue(commandContext.getSource(), getBossBar(commandContext))))
								.then(Commands.literal("max").executes(commandContext -> getMax(commandContext.getSource(), getBossBar(commandContext))))
								.then(Commands.literal("visible").executes(commandContext -> getVisible(commandContext.getSource(), getBossBar(commandContext))))
								.then(Commands.literal("players").executes(commandContext -> getPlayers(commandContext.getSource(), getBossBar(commandContext))))
						)
				)
		);
	}

	private static int getValue(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent) {
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.get.value", customBossEvent.getDisplayName(), customBossEvent.getValue()), true);
		return customBossEvent.getValue();
	}

	private static int getMax(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent) {
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.get.max", customBossEvent.getDisplayName(), customBossEvent.getMax()), true);
		return customBossEvent.getMax();
	}

	private static int getVisible(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent) {
		if (customBossEvent.isVisible()) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.get.visible.visible", customBossEvent.getDisplayName()), true);
			return 1;
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.get.visible.hidden", customBossEvent.getDisplayName()), true);
			return 0;
		}
	}

	private static int getPlayers(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent) {
		if (customBossEvent.getPlayers().isEmpty()) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.get.players.none", customBossEvent.getDisplayName()), true);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent(
					"commands.bossbar.get.players.some",
					customBossEvent.getDisplayName(),
					customBossEvent.getPlayers().size(),
					ComponentUtils.formatList(customBossEvent.getPlayers(), Player::getDisplayName)
				),
				true
			);
		}

		return customBossEvent.getPlayers().size();
	}

	private static int setVisible(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, boolean bl) throws CommandSyntaxException {
		if (customBossEvent.isVisible() == bl) {
			if (bl) {
				throw ERROR_ALREADY_VISIBLE.create();
			} else {
				throw ERROR_ALREADY_HIDDEN.create();
			}
		} else {
			customBossEvent.setVisible(bl);
			if (bl) {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.set.visible.success.visible", customBossEvent.getDisplayName()), true);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.set.visible.success.hidden", customBossEvent.getDisplayName()), true);
			}

			return 0;
		}
	}

	private static int setValue(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, int i) throws CommandSyntaxException {
		if (customBossEvent.getValue() == i) {
			throw ERROR_NO_VALUE_CHANGE.create();
		} else {
			customBossEvent.setValue(i);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.set.value.success", customBossEvent.getDisplayName(), i), true);
			return i;
		}
	}

	private static int setMax(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, int i) throws CommandSyntaxException {
		if (customBossEvent.getMax() == i) {
			throw ERROR_NO_MAX_CHANGE.create();
		} else {
			customBossEvent.setMax(i);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.set.max.success", customBossEvent.getDisplayName(), i), true);
			return i;
		}
	}

	private static int setColor(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, BossEvent.BossBarColor bossBarColor) throws CommandSyntaxException {
		if (customBossEvent.getColor().equals(bossBarColor)) {
			throw ERROR_NO_COLOR_CHANGE.create();
		} else {
			customBossEvent.setColor(bossBarColor);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.set.color.success", customBossEvent.getDisplayName()), true);
			return 0;
		}
	}

	private static int setStyle(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, BossEvent.BossBarOverlay bossBarOverlay) throws CommandSyntaxException {
		if (customBossEvent.getOverlay().equals(bossBarOverlay)) {
			throw ERROR_NO_STYLE_CHANGE.create();
		} else {
			customBossEvent.setOverlay(bossBarOverlay);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.set.style.success", customBossEvent.getDisplayName()), true);
			return 0;
		}
	}

	private static int setName(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, Component component) throws CommandSyntaxException {
		Component component2 = ComponentUtils.updateForEntity(commandSourceStack, component, null, 0);
		if (customBossEvent.getName().equals(component2)) {
			throw ERROR_NO_NAME_CHANGE.create();
		} else {
			customBossEvent.setName(component2);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.set.name.success", customBossEvent.getDisplayName()), true);
			return 0;
		}
	}

	private static int setPlayers(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, Collection<ServerPlayer> collection) throws CommandSyntaxException {
		boolean bl = customBossEvent.setPlayers(collection);
		if (!bl) {
			throw ERROR_NO_PLAYER_CHANGE.create();
		} else {
			if (customBossEvent.getPlayers().isEmpty()) {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.set.players.success.none", customBossEvent.getDisplayName()), true);
			} else {
				commandSourceStack.sendSuccess(
					new TranslatableComponent(
						"commands.bossbar.set.players.success.some",
						customBossEvent.getDisplayName(),
						collection.size(),
						ComponentUtils.formatList(collection, Player::getDisplayName)
					),
					true
				);
			}

			return customBossEvent.getPlayers().size();
		}
	}

	private static int listBars(CommandSourceStack commandSourceStack) {
		Collection<CustomBossEvent> collection = commandSourceStack.getServer().getCustomBossEvents().getEvents();
		if (collection.isEmpty()) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.list.bars.none"), false);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.bossbar.list.bars.some", collection.size(), ComponentUtils.formatList(collection, CustomBossEvent::getDisplayName)),
				false
			);
		}

		return collection.size();
	}

	private static int createBar(CommandSourceStack commandSourceStack, ResourceLocation resourceLocation, Component component) throws CommandSyntaxException {
		CustomBossEvents customBossEvents = commandSourceStack.getServer().getCustomBossEvents();
		if (customBossEvents.get(resourceLocation) != null) {
			throw ERROR_ALREADY_EXISTS.create(resourceLocation.toString());
		} else {
			CustomBossEvent customBossEvent = customBossEvents.create(resourceLocation, ComponentUtils.updateForEntity(commandSourceStack, component, null, 0));
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.create.success", customBossEvent.getDisplayName()), true);
			return customBossEvents.getEvents().size();
		}
	}

	private static int removeBar(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent) {
		CustomBossEvents customBossEvents = commandSourceStack.getServer().getCustomBossEvents();
		customBossEvent.removeAllPlayers();
		customBossEvents.remove(customBossEvent);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.bossbar.remove.success", customBossEvent.getDisplayName()), true);
		return customBossEvents.getEvents().size();
	}

	public static CustomBossEvent getBossBar(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
		ResourceLocation resourceLocation = ResourceLocationArgument.getId(commandContext, "id");
		CustomBossEvent customBossEvent = commandContext.getSource().getServer().getCustomBossEvents().get(resourceLocation);
		if (customBossEvent == null) {
			throw ERROR_DOESNT_EXIST.create(resourceLocation.toString());
		} else {
			return customBossEvent;
		}
	}
}
