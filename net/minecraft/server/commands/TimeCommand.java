/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;

public class TimeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("time").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("set").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("day").executes(commandContext -> TimeCommand.setTime((CommandSourceStack)commandContext.getSource(), 1000)))).then(Commands.literal("noon").executes(commandContext -> TimeCommand.setTime((CommandSourceStack)commandContext.getSource(), 6000)))).then(Commands.literal("night").executes(commandContext -> TimeCommand.setTime((CommandSourceStack)commandContext.getSource(), 13000)))).then(Commands.literal("midnight").executes(commandContext -> TimeCommand.setTime((CommandSourceStack)commandContext.getSource(), 18000)))).then(Commands.argument("time", TimeArgument.time()).executes(commandContext -> TimeCommand.setTime((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "time")))))).then(Commands.literal("add").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("time", TimeArgument.time()).executes(commandContext -> TimeCommand.addTime((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "time")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("query").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("daytime").executes(commandContext -> TimeCommand.queryTime((CommandSourceStack)commandContext.getSource(), TimeCommand.getDayTime(((CommandSourceStack)commandContext.getSource()).getLevel()))))).then(Commands.literal("gametime").executes(commandContext -> TimeCommand.queryTime((CommandSourceStack)commandContext.getSource(), (int)(((CommandSourceStack)commandContext.getSource()).getLevel().getGameTime() % Integer.MAX_VALUE))))).then(Commands.literal("day").executes(commandContext -> TimeCommand.queryTime((CommandSourceStack)commandContext.getSource(), (int)(((CommandSourceStack)commandContext.getSource()).getLevel().getDayTime() / 24000L % Integer.MAX_VALUE))))));
    }

    private static int getDayTime(ServerLevel serverLevel) {
        return (int)(serverLevel.getDayTime() % 24000L);
    }

    private static int queryTime(CommandSourceStack commandSourceStack, int i) {
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.time.query", i), false);
        return i;
    }

    public static int setTime(CommandSourceStack commandSourceStack, int i) {
        for (ServerLevel serverLevel : commandSourceStack.getServer().getAllLevels()) {
            serverLevel.setDayTime(i);
        }
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.time.set", i), true);
        return TimeCommand.getDayTime(commandSourceStack.getLevel());
    }

    public static int addTime(CommandSourceStack commandSourceStack, int i) {
        for (ServerLevel serverLevel : commandSourceStack.getServer().getAllLevels()) {
            serverLevel.setDayTime(serverLevel.getDayTime() + (long)i);
        }
        int j = TimeCommand.getDayTime(commandSourceStack.getLevel());
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.time.set", j), true);
        return j;
    }
}

