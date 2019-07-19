/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ExperienceCommand {
    private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID = new SimpleCommandExceptionType(new TranslatableComponent("commands.experience.set.points.invalid", new Object[0]));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("experience").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("add").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.players()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer()).executes(commandContext -> ExperienceCommand.addExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), Type.POINTS))).then(Commands.literal("points").executes(commandContext -> ExperienceCommand.addExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), Type.POINTS)))).then(Commands.literal("levels").executes(commandContext -> ExperienceCommand.addExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), Type.LEVELS))))))).then(Commands.literal("set").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.players()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer(0)).executes(commandContext -> ExperienceCommand.setExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), Type.POINTS))).then(Commands.literal("points").executes(commandContext -> ExperienceCommand.setExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), Type.POINTS)))).then(Commands.literal("levels").executes(commandContext -> ExperienceCommand.setExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), IntegerArgumentType.getInteger(commandContext, "amount"), Type.LEVELS))))))).then(Commands.literal("query").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.player()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("points").executes(commandContext -> ExperienceCommand.queryExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayer(commandContext, "targets"), Type.POINTS)))).then(Commands.literal("levels").executes(commandContext -> ExperienceCommand.queryExperience((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayer(commandContext, "targets"), Type.LEVELS))))));
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("xp").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).redirect(literalCommandNode));
    }

    private static int queryExperience(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, Type type) {
        int i = type.query.applyAsInt(serverPlayer);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.experience.query." + type.name, serverPlayer.getDisplayName(), i), false);
        return i;
    }

    private static int addExperience(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> collection, int i, Type type) {
        for (ServerPlayer serverPlayer : collection) {
            type.add.accept(serverPlayer, i);
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.experience.add." + type.name + ".success.single", i, collection.iterator().next().getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.experience.add." + type.name + ".success.multiple", i, collection.size()), true);
        }
        return collection.size();
    }

    private static int setExperience(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> collection, int i, Type type) throws CommandSyntaxException {
        int j = 0;
        for (ServerPlayer serverPlayer : collection) {
            if (!type.set.test(serverPlayer, i)) continue;
            ++j;
        }
        if (j == 0) {
            throw ERROR_SET_POINTS_INVALID.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.experience.set." + type.name + ".success.single", i, collection.iterator().next().getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.experience.set." + type.name + ".success.multiple", i, collection.size()), true);
        }
        return collection.size();
    }

    static enum Type {
        POINTS("points", Player::giveExperiencePoints, (serverPlayer, integer) -> {
            if (integer >= serverPlayer.getXpNeededForNextLevel()) {
                return false;
            }
            serverPlayer.setExperiencePoints((int)integer);
            return true;
        }, serverPlayer -> Mth.floor(serverPlayer.experienceProgress * (float)serverPlayer.getXpNeededForNextLevel())),
        LEVELS("levels", ServerPlayer::giveExperienceLevels, (serverPlayer, integer) -> {
            serverPlayer.setExperienceLevels((int)integer);
            return true;
        }, serverPlayer -> serverPlayer.experienceLevel);

        public final BiConsumer<ServerPlayer, Integer> add;
        public final BiPredicate<ServerPlayer, Integer> set;
        public final String name;
        private final ToIntFunction<ServerPlayer> query;

        private Type(String string2, BiConsumer<ServerPlayer, Integer> biConsumer, BiPredicate<ServerPlayer, Integer> biPredicate, ToIntFunction<ServerPlayer> toIntFunction) {
            this.add = biConsumer;
            this.name = string2;
            this.set = biPredicate;
            this.query = toIntFunction;
        }
    }
}

