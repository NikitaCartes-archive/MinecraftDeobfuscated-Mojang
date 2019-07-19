/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class WeatherCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("weather").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((LiteralArgumentBuilder)Commands.literal("clear").executes(commandContext -> WeatherCommand.setClear((CommandSourceStack)commandContext.getSource(), 6000))).then(Commands.argument("duration", IntegerArgumentType.integer(0, 1000000)).executes(commandContext -> WeatherCommand.setClear((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration") * 20))))).then(((LiteralArgumentBuilder)Commands.literal("rain").executes(commandContext -> WeatherCommand.setRain((CommandSourceStack)commandContext.getSource(), 6000))).then(Commands.argument("duration", IntegerArgumentType.integer(0, 1000000)).executes(commandContext -> WeatherCommand.setRain((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration") * 20))))).then(((LiteralArgumentBuilder)Commands.literal("thunder").executes(commandContext -> WeatherCommand.setThunder((CommandSourceStack)commandContext.getSource(), 6000))).then(Commands.argument("duration", IntegerArgumentType.integer(0, 1000000)).executes(commandContext -> WeatherCommand.setThunder((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration") * 20)))));
    }

    private static int setClear(CommandSourceStack commandSourceStack, int i) {
        commandSourceStack.getLevel().getLevelData().setClearWeatherTime(i);
        commandSourceStack.getLevel().getLevelData().setRainTime(0);
        commandSourceStack.getLevel().getLevelData().setThunderTime(0);
        commandSourceStack.getLevel().getLevelData().setRaining(false);
        commandSourceStack.getLevel().getLevelData().setThundering(false);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.weather.set.clear", new Object[0]), true);
        return i;
    }

    private static int setRain(CommandSourceStack commandSourceStack, int i) {
        commandSourceStack.getLevel().getLevelData().setClearWeatherTime(0);
        commandSourceStack.getLevel().getLevelData().setRainTime(i);
        commandSourceStack.getLevel().getLevelData().setThunderTime(i);
        commandSourceStack.getLevel().getLevelData().setRaining(true);
        commandSourceStack.getLevel().getLevelData().setThundering(false);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.weather.set.rain", new Object[0]), true);
        return i;
    }

    private static int setThunder(CommandSourceStack commandSourceStack, int i) {
        commandSourceStack.getLevel().getLevelData().setClearWeatherTime(0);
        commandSourceStack.getLevel().getLevelData().setRainTime(i);
        commandSourceStack.getLevel().getLevelData().setThunderTime(i);
        commandSourceStack.getLevel().getLevelData().setRaining(true);
        commandSourceStack.getLevel().getLevelData().setThundering(true);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.weather.set.thunder", new Object[0]), true);
        return i;
    }
}

