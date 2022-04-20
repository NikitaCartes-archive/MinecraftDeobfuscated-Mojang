/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class TriggerCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType(Component.translatable("commands.trigger.failed.unprimed"));
    private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType(Component.translatable("commands.trigger.failed.invalid"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)Commands.literal("trigger").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective()).suggests((commandContext, suggestionsBuilder) -> TriggerCommand.suggestObjectives((CommandSourceStack)commandContext.getSource(), suggestionsBuilder)).executes(commandContext -> TriggerCommand.simpleTrigger((CommandSourceStack)commandContext.getSource(), TriggerCommand.getScore(((CommandSourceStack)commandContext.getSource()).getPlayerOrException(), ObjectiveArgument.getObjective(commandContext, "objective"))))).then(Commands.literal("add").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("value", IntegerArgumentType.integer()).executes(commandContext -> TriggerCommand.addValue((CommandSourceStack)commandContext.getSource(), TriggerCommand.getScore(((CommandSourceStack)commandContext.getSource()).getPlayerOrException(), ObjectiveArgument.getObjective(commandContext, "objective")), IntegerArgumentType.getInteger(commandContext, "value")))))).then(Commands.literal("set").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("value", IntegerArgumentType.integer()).executes(commandContext -> TriggerCommand.setValue((CommandSourceStack)commandContext.getSource(), TriggerCommand.getScore(((CommandSourceStack)commandContext.getSource()).getPlayerOrException(), ObjectiveArgument.getObjective(commandContext, "objective")), IntegerArgumentType.getInteger(commandContext, "value")))))));
    }

    public static CompletableFuture<Suggestions> suggestObjectives(CommandSourceStack commandSourceStack, SuggestionsBuilder suggestionsBuilder) {
        Entity entity = commandSourceStack.getEntity();
        ArrayList<String> list = Lists.newArrayList();
        if (entity != null) {
            ServerScoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
            String string = entity.getScoreboardName();
            for (Objective objective : scoreboard.getObjectives()) {
                Score score;
                if (objective.getCriteria() != ObjectiveCriteria.TRIGGER || !scoreboard.hasPlayerScore(string, objective) || (score = scoreboard.getOrCreatePlayerScore(string, objective)).isLocked()) continue;
                list.add(objective.getName());
            }
        }
        return SharedSuggestionProvider.suggest(list, suggestionsBuilder);
    }

    private static int addValue(CommandSourceStack commandSourceStack, Score score, int i) {
        score.add(i);
        commandSourceStack.sendSuccess(Component.translatable("commands.trigger.add.success", score.getObjective().getFormattedDisplayName(), i), true);
        return score.getScore();
    }

    private static int setValue(CommandSourceStack commandSourceStack, Score score, int i) {
        score.setScore(i);
        commandSourceStack.sendSuccess(Component.translatable("commands.trigger.set.success", score.getObjective().getFormattedDisplayName(), i), true);
        return i;
    }

    private static int simpleTrigger(CommandSourceStack commandSourceStack, Score score) {
        score.add(1);
        commandSourceStack.sendSuccess(Component.translatable("commands.trigger.simple.success", score.getObjective().getFormattedDisplayName()), true);
        return score.getScore();
    }

    private static Score getScore(ServerPlayer serverPlayer, Objective objective) throws CommandSyntaxException {
        String string;
        if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
            throw ERROR_INVALID_OBJECTIVE.create();
        }
        Scoreboard scoreboard = serverPlayer.getScoreboard();
        if (!scoreboard.hasPlayerScore(string = serverPlayer.getScoreboardName(), objective)) {
            throw ERROR_NOT_PRIMED.create();
        }
        Score score = scoreboard.getOrCreatePlayerScore(string, objective);
        if (score.isLocked()) {
            throw ERROR_NOT_PRIMED.create();
        }
        score.setLocked(true);
        return score;
    }
}

