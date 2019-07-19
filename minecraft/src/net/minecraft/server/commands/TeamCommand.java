package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class TeamCommand {
	private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EXISTS = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.add.duplicate")
	);
	private static final DynamicCommandExceptionType ERROR_TEAM_NAME_TOO_LONG = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.team.add.longName", object)
	);
	private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EMPTY = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.empty.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_NAME = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.option.name.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_COLOR = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.option.color.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.option.friendlyfire.alreadyEnabled")
	);
	private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.option.friendlyfire.alreadyDisabled")
	);
	private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.option.seeFriendlyInvisibles.alreadyEnabled")
	);
	private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.option.seeFriendlyInvisibles.alreadyDisabled")
	);
	private static final SimpleCommandExceptionType ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.option.nametagVisibility.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.option.deathMessageVisibility.unchanged")
	);
	private static final SimpleCommandExceptionType ERROR_TEAM_COLLISION_UNCHANGED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.team.option.collisionRule.unchanged")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("team")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("list")
						.executes(commandContext -> listTeams(commandContext.getSource()))
						.then(
							Commands.argument("team", TeamArgument.team())
								.executes(commandContext -> listMembers(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team")))
						)
				)
				.then(
					Commands.literal("add")
						.then(
							Commands.argument("team", StringArgumentType.word())
								.executes(commandContext -> createTeam(commandContext.getSource(), StringArgumentType.getString(commandContext, "team")))
								.then(
									Commands.argument("displayName", ComponentArgument.textComponent())
										.executes(
											commandContext -> createTeam(
													commandContext.getSource(), StringArgumentType.getString(commandContext, "team"), ComponentArgument.getComponent(commandContext, "displayName")
												)
										)
								)
						)
				)
				.then(
					Commands.literal("remove")
						.then(
							Commands.argument("team", TeamArgument.team())
								.executes(commandContext -> deleteTeam(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team")))
						)
				)
				.then(
					Commands.literal("empty")
						.then(
							Commands.argument("team", TeamArgument.team())
								.executes(commandContext -> emptyTeam(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team")))
						)
				)
				.then(
					Commands.literal("join")
						.then(
							Commands.argument("team", TeamArgument.team())
								.executes(
									commandContext -> joinTeam(
											commandContext.getSource(),
											TeamArgument.getTeam(commandContext, "team"),
											Collections.singleton(commandContext.getSource().getEntityOrException().getScoreboardName())
										)
								)
								.then(
									Commands.argument("members", ScoreHolderArgument.scoreHolders())
										.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
										.executes(
											commandContext -> joinTeam(
													commandContext.getSource(),
													TeamArgument.getTeam(commandContext, "team"),
													ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "members")
												)
										)
								)
						)
				)
				.then(
					Commands.literal("leave")
						.then(
							Commands.argument("members", ScoreHolderArgument.scoreHolders())
								.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
								.executes(commandContext -> leaveTeam(commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "members")))
						)
				)
				.then(
					Commands.literal("modify")
						.then(
							Commands.argument("team", TeamArgument.team())
								.then(
									Commands.literal("displayName")
										.then(
											Commands.argument("displayName", ComponentArgument.textComponent())
												.executes(
													commandContext -> setDisplayName(
															commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), ComponentArgument.getComponent(commandContext, "displayName")
														)
												)
										)
								)
								.then(
									Commands.literal("color")
										.then(
											Commands.argument("value", ColorArgument.color())
												.executes(
													commandContext -> setColor(
															commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), ColorArgument.getColor(commandContext, "value")
														)
												)
										)
								)
								.then(
									Commands.literal("friendlyFire")
										.then(
											Commands.argument("allowed", BoolArgumentType.bool())
												.executes(
													commandContext -> setFriendlyFire(
															commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), BoolArgumentType.getBool(commandContext, "allowed")
														)
												)
										)
								)
								.then(
									Commands.literal("seeFriendlyInvisibles")
										.then(
											Commands.argument("allowed", BoolArgumentType.bool())
												.executes(
													commandContext -> setFriendlySight(
															commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), BoolArgumentType.getBool(commandContext, "allowed")
														)
												)
										)
								)
								.then(
									Commands.literal("nametagVisibility")
										.then(
											Commands.literal("never")
												.executes(commandContext -> setNametagVisibility(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.Visibility.NEVER))
										)
										.then(
											Commands.literal("hideForOtherTeams")
												.executes(
													commandContext -> setNametagVisibility(
															commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.Visibility.HIDE_FOR_OTHER_TEAMS
														)
												)
										)
										.then(
											Commands.literal("hideForOwnTeam")
												.executes(
													commandContext -> setNametagVisibility(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.Visibility.HIDE_FOR_OWN_TEAM)
												)
										)
										.then(
											Commands.literal("always")
												.executes(commandContext -> setNametagVisibility(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.Visibility.ALWAYS))
										)
								)
								.then(
									Commands.literal("deathMessageVisibility")
										.then(
											Commands.literal("never")
												.executes(
													commandContext -> setDeathMessageVisibility(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.Visibility.NEVER)
												)
										)
										.then(
											Commands.literal("hideForOtherTeams")
												.executes(
													commandContext -> setDeathMessageVisibility(
															commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.Visibility.HIDE_FOR_OTHER_TEAMS
														)
												)
										)
										.then(
											Commands.literal("hideForOwnTeam")
												.executes(
													commandContext -> setDeathMessageVisibility(
															commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.Visibility.HIDE_FOR_OWN_TEAM
														)
												)
										)
										.then(
											Commands.literal("always")
												.executes(
													commandContext -> setDeathMessageVisibility(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.Visibility.ALWAYS)
												)
										)
								)
								.then(
									Commands.literal("collisionRule")
										.then(
											Commands.literal("never")
												.executes(commandContext -> setCollision(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.CollisionRule.NEVER))
										)
										.then(
											Commands.literal("pushOwnTeam")
												.executes(
													commandContext -> setCollision(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.CollisionRule.PUSH_OWN_TEAM)
												)
										)
										.then(
											Commands.literal("pushOtherTeams")
												.executes(
													commandContext -> setCollision(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.CollisionRule.PUSH_OTHER_TEAMS)
												)
										)
										.then(
											Commands.literal("always")
												.executes(commandContext -> setCollision(commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), Team.CollisionRule.ALWAYS))
										)
								)
								.then(
									Commands.literal("prefix")
										.then(
											Commands.argument("prefix", ComponentArgument.textComponent())
												.executes(
													commandContext -> setPrefix(
															commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), ComponentArgument.getComponent(commandContext, "prefix")
														)
												)
										)
								)
								.then(
									Commands.literal("suffix")
										.then(
											Commands.argument("suffix", ComponentArgument.textComponent())
												.executes(
													commandContext -> setSuffix(
															commandContext.getSource(), TeamArgument.getTeam(commandContext, "team"), ComponentArgument.getComponent(commandContext, "suffix")
														)
												)
										)
								)
						)
				)
		);
	}

	private static int leaveTeam(CommandSourceStack commandSourceStack, Collection<String> collection) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (String string : collection) {
			scoreboard.removePlayerFromTeam(string);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.team.leave.success.single", collection.iterator().next()), true);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.team.leave.success.multiple", collection.size()), true);
		}

		return collection.size();
	}

	private static int joinTeam(CommandSourceStack commandSourceStack, PlayerTeam playerTeam, Collection<String> collection) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (String string : collection) {
			scoreboard.addPlayerToTeam(string, playerTeam);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.team.join.success.single", collection.iterator().next(), playerTeam.getFormattedDisplayName()), true
			);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.team.join.success.multiple", collection.size(), playerTeam.getFormattedDisplayName()), true
			);
		}

		return collection.size();
	}

	private static int setNametagVisibility(CommandSourceStack commandSourceStack, PlayerTeam playerTeam, Team.Visibility visibility) throws CommandSyntaxException {
		if (playerTeam.getNameTagVisibility() == visibility) {
			throw ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED.create();
		} else {
			playerTeam.setNameTagVisibility(visibility);
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.team.option.nametagVisibility.success", playerTeam.getFormattedDisplayName(), visibility.getDisplayName()), true
			);
			return 0;
		}
	}

	private static int setDeathMessageVisibility(CommandSourceStack commandSourceStack, PlayerTeam playerTeam, Team.Visibility visibility) throws CommandSyntaxException {
		if (playerTeam.getDeathMessageVisibility() == visibility) {
			throw ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED.create();
		} else {
			playerTeam.setDeathMessageVisibility(visibility);
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.team.option.deathMessageVisibility.success", playerTeam.getFormattedDisplayName(), visibility.getDisplayName()), true
			);
			return 0;
		}
	}

	private static int setCollision(CommandSourceStack commandSourceStack, PlayerTeam playerTeam, Team.CollisionRule collisionRule) throws CommandSyntaxException {
		if (playerTeam.getCollisionRule() == collisionRule) {
			throw ERROR_TEAM_COLLISION_UNCHANGED.create();
		} else {
			playerTeam.setCollisionRule(collisionRule);
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.team.option.collisionRule.success", playerTeam.getFormattedDisplayName(), collisionRule.getDisplayName()), true
			);
			return 0;
		}
	}

	private static int setFriendlySight(CommandSourceStack commandSourceStack, PlayerTeam playerTeam, boolean bl) throws CommandSyntaxException {
		if (playerTeam.canSeeFriendlyInvisibles() == bl) {
			if (bl) {
				throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED.create();
			} else {
				throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED.create();
			}
		} else {
			playerTeam.setSeeFriendlyInvisibles(bl);
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.team.option.seeFriendlyInvisibles." + (bl ? "enabled" : "disabled"), playerTeam.getFormattedDisplayName()), true
			);
			return 0;
		}
	}

	private static int setFriendlyFire(CommandSourceStack commandSourceStack, PlayerTeam playerTeam, boolean bl) throws CommandSyntaxException {
		if (playerTeam.isAllowFriendlyFire() == bl) {
			if (bl) {
				throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED.create();
			} else {
				throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED.create();
			}
		} else {
			playerTeam.setAllowFriendlyFire(bl);
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.team.option.friendlyfire." + (bl ? "enabled" : "disabled"), playerTeam.getFormattedDisplayName()), true
			);
			return 0;
		}
	}

	private static int setDisplayName(CommandSourceStack commandSourceStack, PlayerTeam playerTeam, Component component) throws CommandSyntaxException {
		if (playerTeam.getDisplayName().equals(component)) {
			throw ERROR_TEAM_ALREADY_NAME.create();
		} else {
			playerTeam.setDisplayName(component);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.team.option.name.success", playerTeam.getFormattedDisplayName()), true);
			return 0;
		}
	}

	private static int setColor(CommandSourceStack commandSourceStack, PlayerTeam playerTeam, ChatFormatting chatFormatting) throws CommandSyntaxException {
		if (playerTeam.getColor() == chatFormatting) {
			throw ERROR_TEAM_ALREADY_COLOR.create();
		} else {
			playerTeam.setColor(chatFormatting);
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.team.option.color.success", playerTeam.getFormattedDisplayName(), chatFormatting.getName()), true
			);
			return 0;
		}
	}

	private static int emptyTeam(CommandSourceStack commandSourceStack, PlayerTeam playerTeam) throws CommandSyntaxException {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		Collection<String> collection = Lists.<String>newArrayList(playerTeam.getPlayers());
		if (collection.isEmpty()) {
			throw ERROR_TEAM_ALREADY_EMPTY.create();
		} else {
			for (String string : collection) {
				scoreboard.removePlayerFromTeam(string, playerTeam);
			}

			commandSourceStack.sendSuccess(new TranslatableComponent("commands.team.empty.success", collection.size(), playerTeam.getFormattedDisplayName()), true);
			return collection.size();
		}
	}

	private static int deleteTeam(CommandSourceStack commandSourceStack, PlayerTeam playerTeam) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		scoreboard.removePlayerTeam(playerTeam);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.team.remove.success", playerTeam.getFormattedDisplayName()), true);
		return scoreboard.getPlayerTeams().size();
	}

	private static int createTeam(CommandSourceStack commandSourceStack, String string) throws CommandSyntaxException {
		return createTeam(commandSourceStack, string, new TextComponent(string));
	}

	private static int createTeam(CommandSourceStack commandSourceStack, String string, Component component) throws CommandSyntaxException {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		if (scoreboard.getPlayerTeam(string) != null) {
			throw ERROR_TEAM_ALREADY_EXISTS.create();
		} else if (string.length() > 16) {
			throw ERROR_TEAM_NAME_TOO_LONG.create(16);
		} else {
			PlayerTeam playerTeam = scoreboard.addPlayerTeam(string);
			playerTeam.setDisplayName(component);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.team.add.success", playerTeam.getFormattedDisplayName()), true);
			return scoreboard.getPlayerTeams().size();
		}
	}

	private static int listMembers(CommandSourceStack commandSourceStack, PlayerTeam playerTeam) {
		Collection<String> collection = playerTeam.getPlayers();
		if (collection.isEmpty()) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.team.list.members.empty", playerTeam.getFormattedDisplayName()), false);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent(
					"commands.team.list.members.success", playerTeam.getFormattedDisplayName(), collection.size(), ComponentUtils.formatList(collection)
				),
				false
			);
		}

		return collection.size();
	}

	private static int listTeams(CommandSourceStack commandSourceStack) {
		Collection<PlayerTeam> collection = commandSourceStack.getServer().getScoreboard().getPlayerTeams();
		if (collection.isEmpty()) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.team.list.teams.empty"), false);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.team.list.teams.success", collection.size(), ComponentUtils.formatList(collection, PlayerTeam::getFormattedDisplayName)),
				false
			);
		}

		return collection.size();
	}

	private static int setPrefix(CommandSourceStack commandSourceStack, PlayerTeam playerTeam, Component component) {
		playerTeam.setPlayerPrefix(component);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.team.option.prefix.success", component), false);
		return 1;
	}

	private static int setSuffix(CommandSourceStack commandSourceStack, PlayerTeam playerTeam, Component component) {
		playerTeam.setPlayerSuffix(component);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.team.option.suffix.success", component), false);
		return 1;
	}
}
