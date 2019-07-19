package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;

public class ScheduleCommand {
	private static final SimpleCommandExceptionType ERROR_SAME_TICK = new SimpleCommandExceptionType(new TranslatableComponent("commands.schedule.same_tick"));

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
													commandContext.getSource(), FunctionArgument.getFunctionOrTag(commandContext, "function"), IntegerArgumentType.getInteger(commandContext, "time")
												)
										)
								)
						)
				)
		);
	}

	private static int schedule(CommandSourceStack commandSourceStack, Either<CommandFunction, Tag<CommandFunction>> either, int i) throws CommandSyntaxException {
		if (i == 0) {
			throw ERROR_SAME_TICK.create();
		} else {
			long l = commandSourceStack.getLevel().getGameTime() + (long)i;
			either.ifLeft(commandFunction -> {
					ResourceLocation resourceLocation = commandFunction.getId();
					commandSourceStack.getLevel().getLevelData().getScheduledEvents().reschedule(resourceLocation.toString(), l, new FunctionCallback(resourceLocation));
					commandSourceStack.sendSuccess(new TranslatableComponent("commands.schedule.created.function", resourceLocation, i, l), true);
				})
				.ifRight(
					tag -> {
						ResourceLocation resourceLocation = tag.getId();
						commandSourceStack.getLevel()
							.getLevelData()
							.getScheduledEvents()
							.reschedule("#" + resourceLocation.toString(), l, new FunctionTagCallback(resourceLocation));
						commandSourceStack.sendSuccess(new TranslatableComponent("commands.schedule.created.tag", resourceLocation, i, l), true);
					}
				);
			return (int)Math.floorMod(l, 2147483647L);
		}
	}
}
