package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomSequences;

public class RandomCommand {
	private static final SimpleCommandExceptionType ERROR_RANGE_TOO_LARGE = new SimpleCommandExceptionType(
		Component.translatable("commands.random.error.range_too_large")
	);
	private static final SimpleCommandExceptionType ERROR_RANGE_TOO_SMALL = new SimpleCommandExceptionType(
		Component.translatable("commands.random.error.range_too_small")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("random")
				.then(drawRandomValueTree("value", false))
				.then(drawRandomValueTree("roll", true))
				.then(
					Commands.literal("reset")
						.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
						.then(
							Commands.literal("*")
								.executes(commandContext -> resetAllSequences(commandContext.getSource()))
								.then(
									Commands.argument("seed", IntegerArgumentType.integer())
										.executes(
											commandContext -> resetAllSequencesAndSetNewDefaults(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "seed"), true, true)
										)
										.then(
											Commands.argument("includeWorldSeed", BoolArgumentType.bool())
												.executes(
													commandContext -> resetAllSequencesAndSetNewDefaults(
															commandContext.getSource(),
															IntegerArgumentType.getInteger(commandContext, "seed"),
															BoolArgumentType.getBool(commandContext, "includeWorldSeed"),
															true
														)
												)
												.then(
													Commands.argument("includeSequenceId", BoolArgumentType.bool())
														.executes(
															commandContext -> resetAllSequencesAndSetNewDefaults(
																	commandContext.getSource(),
																	IntegerArgumentType.getInteger(commandContext, "seed"),
																	BoolArgumentType.getBool(commandContext, "includeWorldSeed"),
																	BoolArgumentType.getBool(commandContext, "includeSequenceId")
																)
														)
												)
										)
								)
						)
						.then(
							Commands.argument("sequence", ResourceLocationArgument.id())
								.suggests(RandomCommand::suggestRandomSequence)
								.executes(commandContext -> resetSequence(commandContext.getSource(), ResourceLocationArgument.getId(commandContext, "sequence")))
								.then(
									Commands.argument("seed", IntegerArgumentType.integer())
										.executes(
											commandContext -> resetSequence(
													commandContext.getSource(),
													ResourceLocationArgument.getId(commandContext, "sequence"),
													IntegerArgumentType.getInteger(commandContext, "seed"),
													true,
													true
												)
										)
										.then(
											Commands.argument("includeWorldSeed", BoolArgumentType.bool())
												.executes(
													commandContext -> resetSequence(
															commandContext.getSource(),
															ResourceLocationArgument.getId(commandContext, "sequence"),
															IntegerArgumentType.getInteger(commandContext, "seed"),
															BoolArgumentType.getBool(commandContext, "includeWorldSeed"),
															true
														)
												)
												.then(
													Commands.argument("includeSequenceId", BoolArgumentType.bool())
														.executes(
															commandContext -> resetSequence(
																	commandContext.getSource(),
																	ResourceLocationArgument.getId(commandContext, "sequence"),
																	IntegerArgumentType.getInteger(commandContext, "seed"),
																	BoolArgumentType.getBool(commandContext, "includeWorldSeed"),
																	BoolArgumentType.getBool(commandContext, "includeSequenceId")
																)
														)
												)
										)
								)
						)
				)
		);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> drawRandomValueTree(String string, boolean bl) {
		return Commands.literal(string)
			.then(
				Commands.argument("range", RangeArgument.intRange())
					.executes(commandContext -> randomSample(commandContext.getSource(), RangeArgument.Ints.getRange(commandContext, "range"), null, bl))
					.then(
						Commands.argument("sequence", ResourceLocationArgument.id())
							.suggests(RandomCommand::suggestRandomSequence)
							.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
							.executes(
								commandContext -> randomSample(
										commandContext.getSource(), RangeArgument.Ints.getRange(commandContext, "range"), ResourceLocationArgument.getId(commandContext, "sequence"), bl
									)
							)
					)
			);
	}

	private static CompletableFuture<Suggestions> suggestRandomSequence(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) {
		List<String> list = Lists.<String>newArrayList();
		commandContext.getSource().getLevel().getRandomSequences().forAllSequences((resourceLocation, randomSequence) -> list.add(resourceLocation.toString()));
		return SharedSuggestionProvider.suggest(list, suggestionsBuilder);
	}

	private static int randomSample(CommandSourceStack commandSourceStack, MinMaxBounds.Ints ints, @Nullable ResourceLocation resourceLocation, boolean bl) throws CommandSyntaxException {
		RandomSource randomSource;
		if (resourceLocation != null) {
			randomSource = commandSourceStack.getLevel().getRandomSequence(resourceLocation);
		} else {
			randomSource = commandSourceStack.getLevel().getRandom();
		}

		int i = (Integer)ints.min().orElse(Integer.MIN_VALUE);
		int j = (Integer)ints.max().orElse(Integer.MAX_VALUE);
		long l = (long)j - (long)i;
		if (l == 0L) {
			throw ERROR_RANGE_TOO_SMALL.create();
		} else if (l >= 2147483647L) {
			throw ERROR_RANGE_TOO_LARGE.create();
		} else {
			int k = Mth.randomBetweenInclusive(randomSource, i, j);
			if (bl) {
				commandSourceStack.getServer()
					.getPlayerList()
					.broadcastSystemMessage(Component.translatable("commands.random.roll", commandSourceStack.getDisplayName(), k, i, j), false);
			} else {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.random.sample.success", k), false);
			}

			return k;
		}
	}

	private static int resetSequence(CommandSourceStack commandSourceStack, ResourceLocation resourceLocation) throws CommandSyntaxException {
		commandSourceStack.getLevel().getRandomSequences().reset(resourceLocation);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.random.reset.success", Component.translationArg(resourceLocation)), false);
		return 1;
	}

	private static int resetSequence(CommandSourceStack commandSourceStack, ResourceLocation resourceLocation, int i, boolean bl, boolean bl2) throws CommandSyntaxException {
		commandSourceStack.getLevel().getRandomSequences().reset(resourceLocation, i, bl, bl2);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.random.reset.success", Component.translationArg(resourceLocation)), false);
		return 1;
	}

	private static int resetAllSequences(CommandSourceStack commandSourceStack) {
		int i = commandSourceStack.getLevel().getRandomSequences().clear();
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.random.reset.all.success", i), false);
		return i;
	}

	private static int resetAllSequencesAndSetNewDefaults(CommandSourceStack commandSourceStack, int i, boolean bl, boolean bl2) {
		RandomSequences randomSequences = commandSourceStack.getLevel().getRandomSequences();
		randomSequences.setSeedDefaults(i, bl, bl2);
		int j = randomSequences.clear();
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.random.reset.all.success", j), false);
		return j;
	}
}
