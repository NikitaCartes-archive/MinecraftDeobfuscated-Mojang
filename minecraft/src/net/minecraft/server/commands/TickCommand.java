package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.util.TimeUtil;

public class TickCommand {
	private static final float MAX_TICKRATE = 10000.0F;
	private static final String DEFAULT_TICKRATE = String.valueOf(20);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("tick")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(Commands.literal("query").executes(commandContext -> tickQuery(commandContext.getSource())))
				.then(
					Commands.literal("rate")
						.then(
							Commands.argument("rate", FloatArgumentType.floatArg(1.0F, 10000.0F))
								.suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(new String[]{DEFAULT_TICKRATE}, suggestionsBuilder))
								.executes(commandContext -> setTickingRate(commandContext.getSource(), FloatArgumentType.getFloat(commandContext, "rate")))
						)
				)
				.then(
					Commands.literal("step")
						.then(Commands.literal("stop").executes(commandContext -> stopStepping(commandContext.getSource())))
						.then(
							Commands.argument("time", TimeArgument.time(1))
								.suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(new String[]{"1t", "1s"}, suggestionsBuilder))
								.executes(commandContext -> step(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "time")))
						)
				)
				.then(
					Commands.literal("sprint")
						.then(Commands.literal("stop").executes(commandContext -> stopSprinting(commandContext.getSource())))
						.then(
							Commands.argument("time", TimeArgument.time(1))
								.suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(new String[]{"60s", "1d", "3d"}, suggestionsBuilder))
								.executes(commandContext -> sprint(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "time")))
						)
				)
				.then(Commands.literal("unfreeze").executes(commandContext -> setFreeze(commandContext.getSource(), false)))
				.then(Commands.literal("freeze").executes(commandContext -> setFreeze(commandContext.getSource(), true)))
		);
	}

	private static String nanosToMilisString(long l) {
		return String.format("%.1f", (float)l / (float)TimeUtil.NANOSECONDS_PER_MILLISECOND);
	}

	private static int setTickingRate(CommandSourceStack commandSourceStack, float f) {
		ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
		serverTickRateManager.setTickRate(f);
		String string = String.format("%.1f", f);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.rate.success", string), true);
		return (int)f;
	}

	private static int tickQuery(CommandSourceStack commandSourceStack) {
		ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
		String string = nanosToMilisString(commandSourceStack.getServer().getAverageTickTimeNanos());
		float f = serverTickRateManager.tickrate();
		String string2 = String.format("%.1f", f);
		if (serverTickRateManager.isSprinting()) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.sprinting"), false);
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.query.rate.sprinting", string2, string), false);
		} else {
			if (serverTickRateManager.isFrozen()) {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.frozen"), false);
			} else {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.running"), false);
			}

			String string3 = nanosToMilisString(serverTickRateManager.nanosecondsPerTick());
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.query.rate.running", string2, string, string3), false);
		}

		long[] ls = Arrays.copyOf(commandSourceStack.getServer().getTickTimesNanos(), commandSourceStack.getServer().getTickTimesNanos().length);
		Arrays.sort(ls);
		String string4 = nanosToMilisString(ls[ls.length / 2]);
		String string5 = nanosToMilisString(ls[(int)((double)ls.length * 0.95)]);
		String string6 = nanosToMilisString(ls[(int)((double)ls.length * 0.99)]);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.query.percentiles", string4, string5, string6, ls.length), false);
		return (int)f;
	}

	private static int sprint(CommandSourceStack commandSourceStack, int i) {
		boolean bl = commandSourceStack.getServer().tickRateManager().requestGameToSprint(i);
		if (bl) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.sprint.stop.success"), true);
		}

		commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.sprinting"), true);
		return 1;
	}

	private static int setFreeze(CommandSourceStack commandSourceStack, boolean bl) {
		ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
		if (bl) {
			if (serverTickRateManager.isSprinting()) {
				serverTickRateManager.stopSprinting();
			}

			if (serverTickRateManager.isSteppingForward()) {
				serverTickRateManager.stopStepping();
			}
		}

		serverTickRateManager.setFrozen(bl);
		if (bl) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.frozen"), true);
		} else {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.status.running"), true);
		}

		return bl ? 1 : 0;
	}

	private static int step(CommandSourceStack commandSourceStack, int i) {
		ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
		boolean bl = serverTickRateManager.stepGameIfPaused(i);
		if (bl) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.step.success", i), true);
		} else {
			commandSourceStack.sendFailure(Component.translatable("commands.tick.step.fail"));
		}

		return 1;
	}

	private static int stopStepping(CommandSourceStack commandSourceStack) {
		ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
		boolean bl = serverTickRateManager.stopStepping();
		if (bl) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.step.stop.success"), true);
			return 1;
		} else {
			commandSourceStack.sendFailure(Component.translatable("commands.tick.step.stop.fail"));
			return 0;
		}
	}

	private static int stopSprinting(CommandSourceStack commandSourceStack) {
		ServerTickRateManager serverTickRateManager = commandSourceStack.getServer().tickRateManager();
		boolean bl = serverTickRateManager.stopSprinting();
		if (bl) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.tick.sprint.stop.success"), true);
			return 1;
		} else {
			commandSourceStack.sendFailure(Component.translatable("commands.tick.sprint.stop.fail"));
			return 0;
		}
	}
}
