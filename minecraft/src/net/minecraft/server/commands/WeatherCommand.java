package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;

public class WeatherCommand {
	private static final int DEFAULT_TIME = -1;

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("weather")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("clear")
						.executes(commandContext -> setClear(commandContext.getSource(), -1))
						.then(
							Commands.argument("duration", TimeArgument.time(1))
								.executes(commandContext -> setClear(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration")))
						)
				)
				.then(
					Commands.literal("rain")
						.executes(commandContext -> setRain(commandContext.getSource(), -1))
						.then(
							Commands.argument("duration", TimeArgument.time(1))
								.executes(commandContext -> setRain(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration")))
						)
				)
				.then(
					Commands.literal("thunder")
						.executes(commandContext -> setThunder(commandContext.getSource(), -1))
						.then(
							Commands.argument("duration", TimeArgument.time(1))
								.executes(commandContext -> setThunder(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "duration")))
						)
				)
		);
	}

	private static int getDuration(CommandSourceStack commandSourceStack, int i, IntProvider intProvider) {
		return i == -1 ? intProvider.sample(commandSourceStack.getLevel().getRandom()) : i;
	}

	private static int setClear(CommandSourceStack commandSourceStack, int i) {
		commandSourceStack.getLevel().setWeatherParameters(getDuration(commandSourceStack, i, ServerLevel.RAIN_DELAY), 0, false, false);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.weather.set.clear"), true);
		return i;
	}

	private static int setRain(CommandSourceStack commandSourceStack, int i) {
		commandSourceStack.getLevel().setWeatherParameters(0, getDuration(commandSourceStack, i, ServerLevel.RAIN_DURATION), true, false);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.weather.set.rain"), true);
		return i;
	}

	private static int setThunder(CommandSourceStack commandSourceStack, int i) {
		commandSourceStack.getLevel().setWeatherParameters(0, getDuration(commandSourceStack, i, ServerLevel.THUNDER_DURATION), true, true);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.weather.set.thunder"), true);
		return i;
	}
}
