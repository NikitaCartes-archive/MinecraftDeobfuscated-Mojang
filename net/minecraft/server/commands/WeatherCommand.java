/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;

public class WeatherCommand {
    private static final int DEFAULT_TIME = -1;

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("weather").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((LiteralArgumentBuilder)Commands.literal("clear").executes(commandContext -> WeatherCommand.setClear((CommandSourceStack)commandContext.getSource(), -1))).then(Commands.argument("duration", TimeArgument.time(1)).executes(commandContext -> WeatherCommand.setClear((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration")))))).then(((LiteralArgumentBuilder)Commands.literal("rain").executes(commandContext -> WeatherCommand.setRain((CommandSourceStack)commandContext.getSource(), -1))).then(Commands.argument("duration", TimeArgument.time(1)).executes(commandContext -> WeatherCommand.setRain((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration")))))).then(((LiteralArgumentBuilder)Commands.literal("thunder").executes(commandContext -> WeatherCommand.setThunder((CommandSourceStack)commandContext.getSource(), -1))).then(Commands.argument("duration", TimeArgument.time(1)).executes(commandContext -> WeatherCommand.setThunder((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration"))))));
    }

    private static int getDuration(CommandSourceStack commandSourceStack, int i, IntProvider intProvider) {
        if (i == -1) {
            return intProvider.sample(commandSourceStack.getLevel().getRandom());
        }
        return i;
    }

    private static int setClear(CommandSourceStack commandSourceStack, int i) {
        commandSourceStack.getLevel().setWeatherParameters(WeatherCommand.getDuration(commandSourceStack, i, ServerLevel.RAIN_DELAY), 0, false, false);
        commandSourceStack.sendSuccess(Component.translatable("commands.weather.set.clear"), true);
        return i;
    }

    private static int setRain(CommandSourceStack commandSourceStack, int i) {
        commandSourceStack.getLevel().setWeatherParameters(0, WeatherCommand.getDuration(commandSourceStack, i, ServerLevel.RAIN_DURATION), true, false);
        commandSourceStack.sendSuccess(Component.translatable("commands.weather.set.rain"), true);
        return i;
    }

    private static int setThunder(CommandSourceStack commandSourceStack, int i) {
        commandSourceStack.getLevel().setWeatherParameters(0, WeatherCommand.getDuration(commandSourceStack, i, ServerLevel.THUNDER_DURATION), true, true);
        commandSourceStack.sendSuccess(Component.translatable("commands.weather.set.thunder"), true);
        return i;
    }
}

