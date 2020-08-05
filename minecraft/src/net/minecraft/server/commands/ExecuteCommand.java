package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class ExecuteCommand {
	private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("commands.execute.blocks.toobig", object, object2)
	);
	private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.execute.conditional.fail")
	);
	private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.execute.conditional.fail_count", object)
	);
	private static final BinaryOperator<ResultConsumer<CommandSourceStack>> CALLBACK_CHAINER = (resultConsumer, resultConsumer2) -> (commandContext, bl, i) -> {
			resultConsumer.onCommandComplete(commandContext, bl, i);
			resultConsumer2.onCommandComplete(commandContext, bl, i);
		};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_PREDICATE = (commandContext, suggestionsBuilder) -> {
		PredicateManager predicateManager = commandContext.getSource().getServer().getPredicateManager();
		return SharedSuggestionProvider.suggestResource(predicateManager.getKeys(), suggestionsBuilder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
			Commands.literal("execute").requires(commandSourceStack -> commandSourceStack.hasPermission(2))
		);
		commandDispatcher.register(
			Commands.literal("execute")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(Commands.literal("run").redirect(commandDispatcher.getRoot()))
				.then(addConditionals(literalCommandNode, Commands.literal("if"), true))
				.then(addConditionals(literalCommandNode, Commands.literal("unless"), false))
				.then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(literalCommandNode, commandContext -> {
					List<CommandSourceStack> list = Lists.<CommandSourceStack>newArrayList();

					for (Entity entity : EntityArgument.getOptionalEntities(commandContext, "targets")) {
						list.add(commandContext.getSource().withEntity(entity));
					}

					return list;
				})))
				.then(Commands.literal("at").then(Commands.argument("targets", EntityArgument.entities()).fork(literalCommandNode, commandContext -> {
					List<CommandSourceStack> list = Lists.<CommandSourceStack>newArrayList();

					for (Entity entity : EntityArgument.getOptionalEntities(commandContext, "targets")) {
						list.add(commandContext.getSource().withLevel((ServerLevel)entity.level).withPosition(entity.position()).withRotation(entity.getRotationVector()));
					}

					return list;
				})))
				.then(
					Commands.literal("store")
						.then(wrapStores(literalCommandNode, Commands.literal("result"), true))
						.then(wrapStores(literalCommandNode, Commands.literal("success"), false))
				)
				.then(
					Commands.literal("positioned")
						.then(
							Commands.argument("pos", Vec3Argument.vec3())
								.redirect(
									literalCommandNode,
									commandContext -> commandContext.getSource().withPosition(Vec3Argument.getVec3(commandContext, "pos")).withAnchor(EntityAnchorArgument.Anchor.FEET)
								)
						)
						.then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(literalCommandNode, commandContext -> {
							List<CommandSourceStack> list = Lists.<CommandSourceStack>newArrayList();

							for (Entity entity : EntityArgument.getOptionalEntities(commandContext, "targets")) {
								list.add(commandContext.getSource().withPosition(entity.position()));
							}

							return list;
						})))
				)
				.then(
					Commands.literal("rotated")
						.then(
							Commands.argument("rot", RotationArgument.rotation())
								.redirect(
									literalCommandNode,
									commandContext -> commandContext.getSource().withRotation(RotationArgument.getRotation(commandContext, "rot").getRotation(commandContext.getSource()))
								)
						)
						.then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(literalCommandNode, commandContext -> {
							List<CommandSourceStack> list = Lists.<CommandSourceStack>newArrayList();

							for (Entity entity : EntityArgument.getOptionalEntities(commandContext, "targets")) {
								list.add(commandContext.getSource().withRotation(entity.getRotationVector()));
							}

							return list;
						})))
				)
				.then(
					Commands.literal("facing")
						.then(
							Commands.literal("entity")
								.then(
									Commands.argument("targets", EntityArgument.entities())
										.then(Commands.argument("anchor", EntityAnchorArgument.anchor()).fork(literalCommandNode, commandContext -> {
											List<CommandSourceStack> list = Lists.<CommandSourceStack>newArrayList();
											EntityAnchorArgument.Anchor anchor = EntityAnchorArgument.getAnchor(commandContext, "anchor");

											for (Entity entity : EntityArgument.getOptionalEntities(commandContext, "targets")) {
												list.add(commandContext.getSource().facing(entity, anchor));
											}

											return list;
										}))
								)
						)
						.then(
							Commands.argument("pos", Vec3Argument.vec3())
								.redirect(literalCommandNode, commandContext -> commandContext.getSource().facing(Vec3Argument.getVec3(commandContext, "pos")))
						)
				)
				.then(
					Commands.literal("align")
						.then(
							Commands.argument("axes", SwizzleArgument.swizzle())
								.redirect(
									literalCommandNode,
									commandContext -> commandContext.getSource()
											.withPosition(commandContext.getSource().getPosition().align(SwizzleArgument.getSwizzle(commandContext, "axes")))
								)
						)
				)
				.then(
					Commands.literal("anchored")
						.then(
							Commands.argument("anchor", EntityAnchorArgument.anchor())
								.redirect(literalCommandNode, commandContext -> commandContext.getSource().withAnchor(EntityAnchorArgument.getAnchor(commandContext, "anchor")))
						)
				)
				.then(
					Commands.literal("in")
						.then(
							Commands.argument("dimension", DimensionArgument.dimension())
								.redirect(literalCommandNode, commandContext -> commandContext.getSource().withLevel(DimensionArgument.getDimension(commandContext, "dimension")))
						)
				)
		);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> wrapStores(
		LiteralCommandNode<CommandSourceStack> literalCommandNode, LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, boolean bl
	) {
		literalArgumentBuilder.then(
			Commands.literal("score")
				.then(
					Commands.argument("targets", ScoreHolderArgument.scoreHolders())
						.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
						.then(
							Commands.argument("objective", ObjectiveArgument.objective())
								.redirect(
									literalCommandNode,
									commandContext -> storeValue(
											commandContext.getSource(),
											ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"),
											ObjectiveArgument.getObjective(commandContext, "objective"),
											bl
										)
								)
						)
				)
		);
		literalArgumentBuilder.then(
			Commands.literal("bossbar")
				.then(
					Commands.argument("id", ResourceLocationArgument.id())
						.suggests(BossBarCommands.SUGGEST_BOSS_BAR)
						.then(
							Commands.literal("value")
								.redirect(literalCommandNode, commandContext -> storeValue(commandContext.getSource(), BossBarCommands.getBossBar(commandContext), true, bl))
						)
						.then(
							Commands.literal("max")
								.redirect(literalCommandNode, commandContext -> storeValue(commandContext.getSource(), BossBarCommands.getBossBar(commandContext), false, bl))
						)
				)
		);

		for (DataCommands.DataProvider dataProvider : DataCommands.TARGET_PROVIDERS) {
			dataProvider.wrap(
				literalArgumentBuilder,
				argumentBuilder -> argumentBuilder.then(
						Commands.argument("path", NbtPathArgument.nbtPath())
							.then(
								Commands.literal("int")
									.then(
										Commands.argument("scale", DoubleArgumentType.doubleArg())
											.redirect(
												literalCommandNode,
												commandContext -> storeData(
														commandContext.getSource(),
														dataProvider.access(commandContext),
														NbtPathArgument.getPath(commandContext, "path"),
														i -> IntTag.valueOf((int)((double)i * DoubleArgumentType.getDouble(commandContext, "scale"))),
														bl
													)
											)
									)
							)
							.then(
								Commands.literal("float")
									.then(
										Commands.argument("scale", DoubleArgumentType.doubleArg())
											.redirect(
												literalCommandNode,
												commandContext -> storeData(
														commandContext.getSource(),
														dataProvider.access(commandContext),
														NbtPathArgument.getPath(commandContext, "path"),
														i -> FloatTag.valueOf((float)((double)i * DoubleArgumentType.getDouble(commandContext, "scale"))),
														bl
													)
											)
									)
							)
							.then(
								Commands.literal("short")
									.then(
										Commands.argument("scale", DoubleArgumentType.doubleArg())
											.redirect(
												literalCommandNode,
												commandContext -> storeData(
														commandContext.getSource(),
														dataProvider.access(commandContext),
														NbtPathArgument.getPath(commandContext, "path"),
														i -> ShortTag.valueOf((short)((int)((double)i * DoubleArgumentType.getDouble(commandContext, "scale")))),
														bl
													)
											)
									)
							)
							.then(
								Commands.literal("long")
									.then(
										Commands.argument("scale", DoubleArgumentType.doubleArg())
											.redirect(
												literalCommandNode,
												commandContext -> storeData(
														commandContext.getSource(),
														dataProvider.access(commandContext),
														NbtPathArgument.getPath(commandContext, "path"),
														i -> LongTag.valueOf((long)((double)i * DoubleArgumentType.getDouble(commandContext, "scale"))),
														bl
													)
											)
									)
							)
							.then(
								Commands.literal("double")
									.then(
										Commands.argument("scale", DoubleArgumentType.doubleArg())
											.redirect(
												literalCommandNode,
												commandContext -> storeData(
														commandContext.getSource(),
														dataProvider.access(commandContext),
														NbtPathArgument.getPath(commandContext, "path"),
														i -> DoubleTag.valueOf((double)i * DoubleArgumentType.getDouble(commandContext, "scale")),
														bl
													)
											)
									)
							)
							.then(
								Commands.literal("byte")
									.then(
										Commands.argument("scale", DoubleArgumentType.doubleArg())
											.redirect(
												literalCommandNode,
												commandContext -> storeData(
														commandContext.getSource(),
														dataProvider.access(commandContext),
														NbtPathArgument.getPath(commandContext, "path"),
														i -> ByteTag.valueOf((byte)((int)((double)i * DoubleArgumentType.getDouble(commandContext, "scale")))),
														bl
													)
											)
									)
							)
					)
			);
		}

		return literalArgumentBuilder;
	}

	private static CommandSourceStack storeValue(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective, boolean bl) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		return commandSourceStack.withCallback((commandContext, bl2, i) -> {
			for (String string : collection) {
				Score score = scoreboard.getOrCreatePlayerScore(string, objective);
				int j = bl ? i : (bl2 ? 1 : 0);
				score.setScore(j);
			}
		}, CALLBACK_CHAINER);
	}

	private static CommandSourceStack storeValue(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, boolean bl, boolean bl2) {
		return commandSourceStack.withCallback((commandContext, bl3, i) -> {
			int j = bl2 ? i : (bl3 ? 1 : 0);
			if (bl) {
				customBossEvent.setValue(j);
			} else {
				customBossEvent.setMax(j);
			}
		}, CALLBACK_CHAINER);
	}

	private static CommandSourceStack storeData(
		CommandSourceStack commandSourceStack, DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath, IntFunction<Tag> intFunction, boolean bl
	) {
		return commandSourceStack.withCallback((commandContext, bl2, i) -> {
			try {
				CompoundTag compoundTag = dataAccessor.getData();
				int j = bl ? i : (bl2 ? 1 : 0);
				nbtPath.set(compoundTag, () -> (Tag)intFunction.apply(j));
				dataAccessor.setData(compoundTag);
			} catch (CommandSyntaxException var9) {
			}
		}, CALLBACK_CHAINER);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> addConditionals(
		CommandNode<CommandSourceStack> commandNode, LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, boolean bl
	) {
		literalArgumentBuilder.then(
				Commands.literal("block")
					.then(
						Commands.argument("pos", BlockPosArgument.blockPos())
							.then(
								addConditional(
									commandNode,
									Commands.argument("block", BlockPredicateArgument.blockPredicate()),
									bl,
									commandContext -> BlockPredicateArgument.getBlockPredicate(commandContext, "block")
											.test(new BlockInWorld(commandContext.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), true))
								)
							)
					)
			)
			.then(
				Commands.literal("score")
					.then(
						Commands.argument("target", ScoreHolderArgument.scoreHolder())
							.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
							.then(
								Commands.argument("targetObjective", ObjectiveArgument.objective())
									.then(
										Commands.literal("=")
											.then(
												Commands.argument("source", ScoreHolderArgument.scoreHolder())
													.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
													.then(
														addConditional(
															commandNode,
															Commands.argument("sourceObjective", ObjectiveArgument.objective()),
															bl,
															commandContext -> checkScore(commandContext, Integer::equals)
														)
													)
											)
									)
									.then(
										Commands.literal("<")
											.then(
												Commands.argument("source", ScoreHolderArgument.scoreHolder())
													.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
													.then(
														addConditional(
															commandNode,
															Commands.argument("sourceObjective", ObjectiveArgument.objective()),
															bl,
															commandContext -> checkScore(commandContext, (integer, integer2) -> integer < integer2)
														)
													)
											)
									)
									.then(
										Commands.literal("<=")
											.then(
												Commands.argument("source", ScoreHolderArgument.scoreHolder())
													.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
													.then(
														addConditional(
															commandNode,
															Commands.argument("sourceObjective", ObjectiveArgument.objective()),
															bl,
															commandContext -> checkScore(commandContext, (integer, integer2) -> integer <= integer2)
														)
													)
											)
									)
									.then(
										Commands.literal(">")
											.then(
												Commands.argument("source", ScoreHolderArgument.scoreHolder())
													.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
													.then(
														addConditional(
															commandNode,
															Commands.argument("sourceObjective", ObjectiveArgument.objective()),
															bl,
															commandContext -> checkScore(commandContext, (integer, integer2) -> integer > integer2)
														)
													)
											)
									)
									.then(
										Commands.literal(">=")
											.then(
												Commands.argument("source", ScoreHolderArgument.scoreHolder())
													.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
													.then(
														addConditional(
															commandNode,
															Commands.argument("sourceObjective", ObjectiveArgument.objective()),
															bl,
															commandContext -> checkScore(commandContext, (integer, integer2) -> integer >= integer2)
														)
													)
											)
									)
									.then(
										Commands.literal("matches")
											.then(
												addConditional(
													commandNode,
													Commands.argument("range", RangeArgument.intRange()),
													bl,
													commandContext -> checkScore(commandContext, RangeArgument.Ints.getRange(commandContext, "range"))
												)
											)
									)
							)
					)
			)
			.then(
				Commands.literal("blocks")
					.then(
						Commands.argument("start", BlockPosArgument.blockPos())
							.then(
								Commands.argument("end", BlockPosArgument.blockPos())
									.then(
										Commands.argument("destination", BlockPosArgument.blockPos())
											.then(addIfBlocksConditional(commandNode, Commands.literal("all"), bl, false))
											.then(addIfBlocksConditional(commandNode, Commands.literal("masked"), bl, true))
									)
							)
					)
			)
			.then(
				Commands.literal("entity")
					.then(
						Commands.argument("entities", EntityArgument.entities())
							.fork(commandNode, commandContext -> expect(commandContext, bl, !EntityArgument.getOptionalEntities(commandContext, "entities").isEmpty()))
							.executes(createNumericConditionalHandler(bl, commandContext -> EntityArgument.getOptionalEntities(commandContext, "entities").size()))
					)
			)
			.then(
				Commands.literal("predicate")
					.then(
						addConditional(
							commandNode,
							Commands.argument("predicate", ResourceLocationArgument.id()).suggests(SUGGEST_PREDICATE),
							bl,
							commandContext -> checkCustomPredicate(commandContext.getSource(), ResourceLocationArgument.getPredicate(commandContext, "predicate"))
						)
					)
			);

		for (DataCommands.DataProvider dataProvider : DataCommands.SOURCE_PROVIDERS) {
			literalArgumentBuilder.then(
				dataProvider.wrap(
					Commands.literal("data"),
					argumentBuilder -> argumentBuilder.then(
							Commands.argument("path", NbtPathArgument.nbtPath())
								.fork(
									commandNode,
									commandContext -> expect(
											commandContext, bl, checkMatchingData(dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path")) > 0
										)
								)
								.executes(
									createNumericConditionalHandler(
										bl, commandContext -> checkMatchingData(dataProvider.access(commandContext), NbtPathArgument.getPath(commandContext, "path"))
									)
								)
						)
				)
			);
		}

		return literalArgumentBuilder;
	}

	private static Command<CommandSourceStack> createNumericConditionalHandler(boolean bl, ExecuteCommand.CommandNumericPredicate commandNumericPredicate) {
		return bl ? commandContext -> {
			int i = commandNumericPredicate.test(commandContext);
			if (i > 0) {
				commandContext.getSource().sendSuccess(new TranslatableComponent("commands.execute.conditional.pass_count", i), false);
				return i;
			} else {
				throw ERROR_CONDITIONAL_FAILED.create();
			}
		} : commandContext -> {
			int i = commandNumericPredicate.test(commandContext);
			if (i == 0) {
				commandContext.getSource().sendSuccess(new TranslatableComponent("commands.execute.conditional.pass"), false);
				return 1;
			} else {
				throw ERROR_CONDITIONAL_FAILED_COUNT.create(i);
			}
		};
	}

	private static int checkMatchingData(DataAccessor dataAccessor, NbtPathArgument.NbtPath nbtPath) throws CommandSyntaxException {
		return nbtPath.countMatching(dataAccessor.getData());
	}

	private static boolean checkScore(CommandContext<CommandSourceStack> commandContext, BiPredicate<Integer, Integer> biPredicate) throws CommandSyntaxException {
		String string = ScoreHolderArgument.getName(commandContext, "target");
		Objective objective = ObjectiveArgument.getObjective(commandContext, "targetObjective");
		String string2 = ScoreHolderArgument.getName(commandContext, "source");
		Objective objective2 = ObjectiveArgument.getObjective(commandContext, "sourceObjective");
		Scoreboard scoreboard = commandContext.getSource().getServer().getScoreboard();
		if (scoreboard.hasPlayerScore(string, objective) && scoreboard.hasPlayerScore(string2, objective2)) {
			Score score = scoreboard.getOrCreatePlayerScore(string, objective);
			Score score2 = scoreboard.getOrCreatePlayerScore(string2, objective2);
			return biPredicate.test(score.getScore(), score2.getScore());
		} else {
			return false;
		}
	}

	private static boolean checkScore(CommandContext<CommandSourceStack> commandContext, MinMaxBounds.Ints ints) throws CommandSyntaxException {
		String string = ScoreHolderArgument.getName(commandContext, "target");
		Objective objective = ObjectiveArgument.getObjective(commandContext, "targetObjective");
		Scoreboard scoreboard = commandContext.getSource().getServer().getScoreboard();
		return !scoreboard.hasPlayerScore(string, objective) ? false : ints.matches(scoreboard.getOrCreatePlayerScore(string, objective).getScore());
	}

	private static boolean checkCustomPredicate(CommandSourceStack commandSourceStack, LootItemCondition lootItemCondition) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		LootContext.Builder builder = new LootContext.Builder(serverLevel)
			.withParameter(LootContextParams.ORIGIN, commandSourceStack.getPosition())
			.withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity());
		return lootItemCondition.test(builder.create(LootContextParamSets.COMMAND));
	}

	private static Collection<CommandSourceStack> expect(CommandContext<CommandSourceStack> commandContext, boolean bl, boolean bl2) {
		return (Collection<CommandSourceStack>)(bl2 == bl ? Collections.singleton(commandContext.getSource()) : Collections.emptyList());
	}

	private static ArgumentBuilder<CommandSourceStack, ?> addConditional(
		CommandNode<CommandSourceStack> commandNode,
		ArgumentBuilder<CommandSourceStack, ?> argumentBuilder,
		boolean bl,
		ExecuteCommand.CommandPredicate commandPredicate
	) {
		return argumentBuilder.fork(commandNode, commandContext -> expect(commandContext, bl, commandPredicate.test(commandContext))).executes(commandContext -> {
			if (bl == commandPredicate.test(commandContext)) {
				commandContext.getSource().sendSuccess(new TranslatableComponent("commands.execute.conditional.pass"), false);
				return 1;
			} else {
				throw ERROR_CONDITIONAL_FAILED.create();
			}
		});
	}

	private static ArgumentBuilder<CommandSourceStack, ?> addIfBlocksConditional(
		CommandNode<CommandSourceStack> commandNode, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, boolean bl, boolean bl2
	) {
		return argumentBuilder.fork(commandNode, commandContext -> expect(commandContext, bl, checkRegions(commandContext, bl2).isPresent()))
			.executes(bl ? commandContext -> checkIfRegions(commandContext, bl2) : commandContext -> checkUnlessRegions(commandContext, bl2));
	}

	private static int checkIfRegions(CommandContext<CommandSourceStack> commandContext, boolean bl) throws CommandSyntaxException {
		OptionalInt optionalInt = checkRegions(commandContext, bl);
		if (optionalInt.isPresent()) {
			commandContext.getSource().sendSuccess(new TranslatableComponent("commands.execute.conditional.pass_count", optionalInt.getAsInt()), false);
			return optionalInt.getAsInt();
		} else {
			throw ERROR_CONDITIONAL_FAILED.create();
		}
	}

	private static int checkUnlessRegions(CommandContext<CommandSourceStack> commandContext, boolean bl) throws CommandSyntaxException {
		OptionalInt optionalInt = checkRegions(commandContext, bl);
		if (optionalInt.isPresent()) {
			throw ERROR_CONDITIONAL_FAILED_COUNT.create(optionalInt.getAsInt());
		} else {
			commandContext.getSource().sendSuccess(new TranslatableComponent("commands.execute.conditional.pass"), false);
			return 1;
		}
	}

	private static OptionalInt checkRegions(CommandContext<CommandSourceStack> commandContext, boolean bl) throws CommandSyntaxException {
		return checkRegions(
			commandContext.getSource().getLevel(),
			BlockPosArgument.getLoadedBlockPos(commandContext, "start"),
			BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
			BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
			bl
		);
	}

	private static OptionalInt checkRegions(ServerLevel serverLevel, BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, boolean bl) throws CommandSyntaxException {
		BoundingBox boundingBox = new BoundingBox(blockPos, blockPos2);
		BoundingBox boundingBox2 = new BoundingBox(blockPos3, blockPos3.offset(boundingBox.getLength()));
		BlockPos blockPos4 = new BlockPos(boundingBox2.x0 - boundingBox.x0, boundingBox2.y0 - boundingBox.y0, boundingBox2.z0 - boundingBox.z0);
		int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
		if (i > 32768) {
			throw ERROR_AREA_TOO_LARGE.create(32768, i);
		} else {
			int j = 0;

			for (int k = boundingBox.z0; k <= boundingBox.z1; k++) {
				for (int l = boundingBox.y0; l <= boundingBox.y1; l++) {
					for (int m = boundingBox.x0; m <= boundingBox.x1; m++) {
						BlockPos blockPos5 = new BlockPos(m, l, k);
						BlockPos blockPos6 = blockPos5.offset(blockPos4);
						BlockState blockState = serverLevel.getBlockState(blockPos5);
						if (!bl || !blockState.is(Blocks.AIR)) {
							if (blockState != serverLevel.getBlockState(blockPos6)) {
								return OptionalInt.empty();
							}

							BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos5);
							BlockEntity blockEntity2 = serverLevel.getBlockEntity(blockPos6);
							if (blockEntity != null) {
								if (blockEntity2 == null) {
									return OptionalInt.empty();
								}

								CompoundTag compoundTag = blockEntity.save(new CompoundTag());
								compoundTag.remove("x");
								compoundTag.remove("y");
								compoundTag.remove("z");
								CompoundTag compoundTag2 = blockEntity2.save(new CompoundTag());
								compoundTag2.remove("x");
								compoundTag2.remove("y");
								compoundTag2.remove("z");
								if (!compoundTag.equals(compoundTag2)) {
									return OptionalInt.empty();
								}
							}

							j++;
						}
					}
				}
			}

			return OptionalInt.of(j);
		}
	}

	@FunctionalInterface
	interface CommandNumericPredicate {
		int test(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;
	}

	@FunctionalInterface
	interface CommandPredicate {
		boolean test(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;
	}
}
