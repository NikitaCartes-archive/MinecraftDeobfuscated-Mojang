package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;
import net.minecraft.world.level.timers.TimerQueue;

public class ScheduleCommand {
	private static final SimpleCommandExceptionType ERROR_SAME_TICK = new SimpleCommandExceptionType(new TranslatableComponent("commands.schedule.same_tick"));
	private static final DynamicCommandExceptionType ERROR_CANT_REMOVE = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.schedule.cleared.failure", object)
	);
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_SCHEDULE = (commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
			commandContext.getSource().getServer().getWorldData().overworldData().getScheduledEvents().getEventsIds(), suggestionsBuilder
		);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("schedule")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("function")
						.then(
							Commands.argument("function", FunctionArgument.functions())
								.suggests(FunctionCommand.SUGGEST_FUNCTION)
								.then(
									Commands.argument("time", TimeArgument.time())
										.executes(
											commandContext -> schedule(
													commandContext.getSource(),
													FunctionArgument.getFunctionOrTag(commandContext, "function"),
													IntegerArgumentType.getInteger(commandContext, "time"),
													true
												)
										)
										.then(
											Commands.literal("append")
												.executes(
													commandContext -> schedule(
															commandContext.getSource(),
															FunctionArgument.getFunctionOrTag(commandContext, "function"),
															IntegerArgumentType.getInteger(commandContext, "time"),
															false
														)
												)
										)
										.then(
											Commands.literal("replace")
												.executes(
													commandContext -> schedule(
															commandContext.getSource(),
															FunctionArgument.getFunctionOrTag(commandContext, "function"),
															IntegerArgumentType.getInteger(commandContext, "time"),
															true
														)
												)
										)
								)
						)
				)
				.then(
					Commands.literal("clear")
						.then(
							Commands.argument("function", StringArgumentType.greedyString())
								.suggests(SUGGEST_SCHEDULE)
								.executes(commandContext -> remove(commandContext.getSource(), StringArgumentType.getString(commandContext, "function")))
						)
				)
		);
	}

	private static int schedule(
		CommandSourceStack commandSourceStack, Pair<ResourceLocation, Either<CommandFunction, Tag<CommandFunction>>> pair, int i, boolean bl
	) throws CommandSyntaxException {
		if (i == 0) {
			throw ERROR_SAME_TICK.create();
		} else {
			long l = commandSourceStack.getLevel().getGameTime() + (long)i;
			ResourceLocation resourceLocation = pair.getFirst();
			TimerQueue<MinecraftServer> timerQueue = commandSourceStack.getServer().getWorldData().overworldData().getScheduledEvents();
			pair.getSecond().ifLeft(commandFunction -> {
				String string = resourceLocation.toString();
				if (bl) {
					timerQueue.remove(string);
				}

				timerQueue.schedule(string, l, new FunctionCallback(resourceLocation));
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.schedule.created.function", resourceLocation, i, l), true);
			}).ifRight(tag -> {
				String string = "#" + resourceLocation;
				if (bl) {
					timerQueue.remove(string);
				}

				timerQueue.schedule(string, l, new FunctionTagCallback(resourceLocation));
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.schedule.created.tag", resourceLocation, i, l), true);
			});
			return Math.floorMod(l, Integer.MAX_VALUE);
		}
	}

	private static int remove(CommandSourceStack commandSourceStack, String string) throws CommandSyntaxException {
		int i = commandSourceStack.getServer().getWorldData().overworldData().getScheduledEvents().remove(string);
		if (i == 0) {
			throw ERROR_CANT_REMOVE.create(string);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.schedule.cleared.success", i, string), true);
			return i;
		}
	}
}
