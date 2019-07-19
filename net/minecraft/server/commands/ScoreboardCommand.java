/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ScoreboardCommand {
    private static final SimpleCommandExceptionType ERROR_OBJECTIVE_ALREADY_EXISTS = new SimpleCommandExceptionType(new TranslatableComponent("commands.scoreboard.objectives.add.duplicate", new Object[0]));
    private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_EMPTY = new SimpleCommandExceptionType(new TranslatableComponent("commands.scoreboard.objectives.display.alreadyEmpty", new Object[0]));
    private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_SET = new SimpleCommandExceptionType(new TranslatableComponent("commands.scoreboard.objectives.display.alreadySet", new Object[0]));
    private static final SimpleCommandExceptionType ERROR_TRIGGER_ALREADY_ENABLED = new SimpleCommandExceptionType(new TranslatableComponent("commands.scoreboard.players.enable.failed", new Object[0]));
    private static final SimpleCommandExceptionType ERROR_NOT_TRIGGER = new SimpleCommandExceptionType(new TranslatableComponent("commands.scoreboard.players.enable.invalid", new Object[0]));
    private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableComponent("commands.scoreboard.players.get.null", object, object2));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("scoreboard").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("objectives").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("list").executes(commandContext -> ScoreboardCommand.listObjectives((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("add").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", StringArgumentType.word()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("criteria", ObjectiveCriteriaArgument.criteria()).executes(commandContext -> ScoreboardCommand.addObjective((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "objective"), ObjectiveCriteriaArgument.getCriteria(commandContext, "criteria"), new TextComponent(StringArgumentType.getString(commandContext, "objective"))))).then(Commands.argument("displayName", ComponentArgument.textComponent()).executes(commandContext -> ScoreboardCommand.addObjective((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString(commandContext, "objective"), ObjectiveCriteriaArgument.getCriteria(commandContext, "criteria"), ComponentArgument.getComponent(commandContext, "displayName")))))))).then(Commands.literal("modify").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("displayname").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("displayName", ComponentArgument.textComponent()).executes(commandContext -> ScoreboardCommand.setDisplayName((CommandSourceStack)commandContext.getSource(), ObjectiveArgument.getObjective(commandContext, "objective"), ComponentArgument.getComponent(commandContext, "displayName")))))).then(ScoreboardCommand.createRenderTypeModify())))).then(Commands.literal("remove").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).executes(commandContext -> ScoreboardCommand.removeObjective((CommandSourceStack)commandContext.getSource(), ObjectiveArgument.getObjective(commandContext, "objective")))))).then(Commands.literal("setdisplay").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("slot", ScoreboardSlotArgument.displaySlot()).executes(commandContext -> ScoreboardCommand.clearDisplaySlot((CommandSourceStack)commandContext.getSource(), ScoreboardSlotArgument.getDisplaySlot(commandContext, "slot")))).then(Commands.argument("objective", ObjectiveArgument.objective()).executes(commandContext -> ScoreboardCommand.setDisplaySlot((CommandSourceStack)commandContext.getSource(), ScoreboardSlotArgument.getDisplaySlot(commandContext, "slot"), ObjectiveArgument.getObjective(commandContext, "objective")))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("players").then((ArgumentBuilder<CommandSourceStack, ?>)((LiteralArgumentBuilder)Commands.literal("list").executes(commandContext -> ScoreboardCommand.listTrackedPlayers((CommandSourceStack)commandContext.getSource()))).then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes(commandContext -> ScoreboardCommand.listTrackedPlayerScores((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getName(commandContext, "target")))))).then(Commands.literal("set").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("score", IntegerArgumentType.integer()).executes(commandContext -> ScoreboardCommand.setScore((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"), ObjectiveArgument.getWritableObjective(commandContext, "objective"), IntegerArgumentType.getInteger(commandContext, "score")))))))).then(Commands.literal("get").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).executes(commandContext -> ScoreboardCommand.getScore((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getName(commandContext, "target"), ObjectiveArgument.getObjective(commandContext, "objective"))))))).then(Commands.literal("add").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("score", IntegerArgumentType.integer(0)).executes(commandContext -> ScoreboardCommand.addScore((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"), ObjectiveArgument.getWritableObjective(commandContext, "objective"), IntegerArgumentType.getInteger(commandContext, "score")))))))).then(Commands.literal("remove").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("score", IntegerArgumentType.integer(0)).executes(commandContext -> ScoreboardCommand.removeScore((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"), ObjectiveArgument.getWritableObjective(commandContext, "objective"), IntegerArgumentType.getInteger(commandContext, "score")))))))).then(Commands.literal("reset").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes(commandContext -> ScoreboardCommand.resetScores((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets")))).then(Commands.argument("objective", ObjectiveArgument.objective()).executes(commandContext -> ScoreboardCommand.resetScore((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"), ObjectiveArgument.getObjective(commandContext, "objective"))))))).then(Commands.literal("enable").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).suggests((commandContext, suggestionsBuilder) -> ScoreboardCommand.suggestTriggers((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"), suggestionsBuilder)).executes(commandContext -> ScoreboardCommand.enableTrigger((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"), ObjectiveArgument.getObjective(commandContext, "objective"))))))).then(Commands.literal("operation").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targetObjective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("operation", OperationArgument.operation()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("sourceObjective", ObjectiveArgument.objective()).executes(commandContext -> ScoreboardCommand.performOperation((CommandSourceStack)commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"), ObjectiveArgument.getWritableObjective(commandContext, "targetObjective"), OperationArgument.getOperation(commandContext, "operation"), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "source"), ObjectiveArgument.getObjective(commandContext, "sourceObjective")))))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRenderTypeModify() {
        LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("rendertype");
        for (ObjectiveCriteria.RenderType renderType : ObjectiveCriteria.RenderType.values()) {
            literalArgumentBuilder.then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal(renderType.getId()).executes(commandContext -> ScoreboardCommand.setRenderType((CommandSourceStack)commandContext.getSource(), ObjectiveArgument.getObjective(commandContext, "objective"), renderType)));
        }
        return literalArgumentBuilder;
    }

    private static CompletableFuture<Suggestions> suggestTriggers(CommandSourceStack commandSourceStack, Collection<String> collection, SuggestionsBuilder suggestionsBuilder) {
        ArrayList<String> list = Lists.newArrayList();
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        for (Objective objective : scoreboard.getObjectives()) {
            if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) continue;
            boolean bl = false;
            for (String string : collection) {
                if (scoreboard.hasPlayerScore(string, objective) && !scoreboard.getOrCreatePlayerScore(string, objective).isLocked()) continue;
                bl = true;
                break;
            }
            if (!bl) continue;
            list.add(objective.getName());
        }
        return SharedSuggestionProvider.suggest(list, suggestionsBuilder);
    }

    private static int getScore(CommandSourceStack commandSourceStack, String string, Objective objective) throws CommandSyntaxException {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        if (!scoreboard.hasPlayerScore(string, objective)) {
            throw ERROR_NO_VALUE.create(objective.getName(), string);
        }
        Score score = scoreboard.getOrCreatePlayerScore(string, objective);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.get.success", string, score.getScore(), objective.getFormattedDisplayName()), false);
        return score.getScore();
    }

    private static int performOperation(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective, OperationArgument.Operation operation, Collection<String> collection2, Objective objective2) throws CommandSyntaxException {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        int i = 0;
        for (String string : collection) {
            Score score = scoreboard.getOrCreatePlayerScore(string, objective);
            for (String string2 : collection2) {
                Score score2 = scoreboard.getOrCreatePlayerScore(string2, objective2);
                operation.apply(score, score2);
            }
            i += score.getScore();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.operation.success.single", objective.getFormattedDisplayName(), collection.iterator().next(), i), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.operation.success.multiple", objective.getFormattedDisplayName(), collection.size()), true);
        }
        return i;
    }

    private static int enableTrigger(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective) throws CommandSyntaxException {
        if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
            throw ERROR_NOT_TRIGGER.create();
        }
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        int i = 0;
        for (String string : collection) {
            Score score = scoreboard.getOrCreatePlayerScore(string, objective);
            if (!score.isLocked()) continue;
            score.setLocked(false);
            ++i;
        }
        if (i == 0) {
            throw ERROR_TRIGGER_ALREADY_ENABLED.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.enable.success.single", objective.getFormattedDisplayName(), collection.iterator().next()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.enable.success.multiple", objective.getFormattedDisplayName(), collection.size()), true);
        }
        return i;
    }

    private static int resetScores(CommandSourceStack commandSourceStack, Collection<String> collection) {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        for (String string : collection) {
            scoreboard.resetPlayerScore(string, null);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.reset.all.single", collection.iterator().next()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.reset.all.multiple", collection.size()), true);
        }
        return collection.size();
    }

    private static int resetScore(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective) {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        for (String string : collection) {
            scoreboard.resetPlayerScore(string, objective);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.reset.specific.single", objective.getFormattedDisplayName(), collection.iterator().next()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.reset.specific.multiple", objective.getFormattedDisplayName(), collection.size()), true);
        }
        return collection.size();
    }

    private static int setScore(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective, int i) {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        for (String string : collection) {
            Score score = scoreboard.getOrCreatePlayerScore(string, objective);
            score.setScore(i);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.set.success.single", objective.getFormattedDisplayName(), collection.iterator().next(), i), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.set.success.multiple", objective.getFormattedDisplayName(), collection.size(), i), true);
        }
        return i * collection.size();
    }

    private static int addScore(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective, int i) {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        int j = 0;
        for (String string : collection) {
            Score score = scoreboard.getOrCreatePlayerScore(string, objective);
            score.setScore(score.getScore() + i);
            j += score.getScore();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.add.success.single", i, objective.getFormattedDisplayName(), collection.iterator().next(), j), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.add.success.multiple", i, objective.getFormattedDisplayName(), collection.size()), true);
        }
        return j;
    }

    private static int removeScore(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective, int i) {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        int j = 0;
        for (String string : collection) {
            Score score = scoreboard.getOrCreatePlayerScore(string, objective);
            score.setScore(score.getScore() - i);
            j += score.getScore();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.remove.success.single", i, objective.getFormattedDisplayName(), collection.iterator().next(), j), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.remove.success.multiple", i, objective.getFormattedDisplayName(), collection.size()), true);
        }
        return j;
    }

    private static int listTrackedPlayers(CommandSourceStack commandSourceStack) {
        Collection<String> collection = commandSourceStack.getServer().getScoreboard().getTrackedPlayers();
        if (collection.isEmpty()) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.list.empty", new Object[0]), false);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.list.success", collection.size(), ComponentUtils.formatList(collection)), false);
        }
        return collection.size();
    }

    private static int listTrackedPlayerScores(CommandSourceStack commandSourceStack, String string) {
        Map<Objective, Score> map = commandSourceStack.getServer().getScoreboard().getPlayerScores(string);
        if (map.isEmpty()) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.list.entity.empty", string), false);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.list.entity.success", string, map.size()), false);
            for (Map.Entry<Objective, Score> entry : map.entrySet()) {
                commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.players.list.entity.entry", entry.getKey().getFormattedDisplayName(), entry.getValue().getScore()), false);
            }
        }
        return map.size();
    }

    private static int clearDisplaySlot(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        if (scoreboard.getDisplayObjective(i) == null) {
            throw ERROR_DISPLAY_SLOT_ALREADY_EMPTY.create();
        }
        ((Scoreboard)scoreboard).setDisplayObjective(i, null);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.objectives.display.cleared", Scoreboard.getDisplaySlotNames()[i]), true);
        return 0;
    }

    private static int setDisplaySlot(CommandSourceStack commandSourceStack, int i, Objective objective) throws CommandSyntaxException {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        if (scoreboard.getDisplayObjective(i) == objective) {
            throw ERROR_DISPLAY_SLOT_ALREADY_SET.create();
        }
        ((Scoreboard)scoreboard).setDisplayObjective(i, objective);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.objectives.display.set", Scoreboard.getDisplaySlotNames()[i], objective.getDisplayName()), true);
        return 0;
    }

    private static int setDisplayName(CommandSourceStack commandSourceStack, Objective objective, Component component) {
        if (!objective.getDisplayName().equals(component)) {
            objective.setDisplayName(component);
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.objectives.modify.displayname", objective.getName(), objective.getFormattedDisplayName()), true);
        }
        return 0;
    }

    private static int setRenderType(CommandSourceStack commandSourceStack, Objective objective, ObjectiveCriteria.RenderType renderType) {
        if (objective.getRenderType() != renderType) {
            objective.setRenderType(renderType);
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.objectives.modify.rendertype", objective.getFormattedDisplayName()), true);
        }
        return 0;
    }

    private static int removeObjective(CommandSourceStack commandSourceStack, Objective objective) {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        scoreboard.removeObjective(objective);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.objectives.remove.success", objective.getFormattedDisplayName()), true);
        return scoreboard.getObjectives().size();
    }

    private static int addObjective(CommandSourceStack commandSourceStack, String string, ObjectiveCriteria objectiveCriteria, Component component) throws CommandSyntaxException {
        ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
        if (scoreboard.getObjective(string) != null) {
            throw ERROR_OBJECTIVE_ALREADY_EXISTS.create();
        }
        if (string.length() > 16) {
            throw ObjectiveArgument.ERROR_OBJECTIVE_NAME_TOO_LONG.create(16);
        }
        scoreboard.addObjective(string, objectiveCriteria, component, objectiveCriteria.getDefaultRenderType());
        Objective objective = scoreboard.getObjective(string);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.objectives.add.success", objective.getFormattedDisplayName()), true);
        return scoreboard.getObjectives().size();
    }

    private static int listObjectives(CommandSourceStack commandSourceStack) {
        Collection<Objective> collection = commandSourceStack.getServer().getScoreboard().getObjectives();
        if (collection.isEmpty()) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.objectives.list.empty", new Object[0]), false);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.scoreboard.objectives.list.success", collection.size(), ComponentUtils.formatList(collection, Objective::getFormattedDisplayName)), false);
        }
        return collection.size();
    }
}

