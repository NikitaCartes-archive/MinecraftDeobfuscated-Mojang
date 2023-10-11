package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandResultConsumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.HeightmapTypeArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class ExecuteCommand {
	private static final int MAX_TEST_AREA = 32768;
	private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("commands.execute.blocks.toobig", object, object2)
	);
	private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(
		Component.translatable("commands.execute.conditional.fail")
	);
	private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.execute.conditional.fail_count", object)
	);
	private static final Dynamic2CommandExceptionType ERROR_FUNCTION_CONDITION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("commands.execute.function.instantiationFailure", object, object2)
	);
	private static final BinaryOperator<CommandResultConsumer<CommandSourceStack>> CALLBACK_CHAINER = (commandResultConsumer, commandResultConsumer2) -> (commandSourceStack, bl, i) -> {
			commandResultConsumer.storeResult(commandSourceStack, bl, i);
			commandResultConsumer2.storeResult(commandSourceStack, bl, i);
		};
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_PREDICATE = (commandContext, suggestionsBuilder) -> {
		LootDataManager lootDataManager = commandContext.getSource().getServer().getLootData();
		return SharedSuggestionProvider.suggestResource(lootDataManager.getKeys(LootDataType.PREDICATE), suggestionsBuilder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
			Commands.literal("execute").requires(commandSourceStack -> commandSourceStack.hasPermission(2))
		);
		commandDispatcher.register(
			Commands.literal("execute")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(Commands.literal("run").redirect(commandDispatcher.getRoot()))
				.then(addConditionals(literalCommandNode, Commands.literal("if"), true, commandBuildContext))
				.then(addConditionals(literalCommandNode, Commands.literal("unless"), false, commandBuildContext))
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
						list.add(commandContext.getSource().withLevel((ServerLevel)entity.level()).withPosition(entity.position()).withRotation(entity.getRotationVector()));
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
						.then(Commands.literal("over").then(Commands.argument("heightmap", HeightmapTypeArgument.heightmap()).redirect(literalCommandNode, commandContext -> {
							Vec3 vec3 = commandContext.getSource().getPosition();
							ServerLevel serverLevel = commandContext.getSource().getLevel();
							double d = vec3.x();
							double e = vec3.z();
							if (!serverLevel.hasChunk(SectionPos.blockToSectionCoord(d), SectionPos.blockToSectionCoord(e))) {
								throw BlockPosArgument.ERROR_NOT_LOADED.create();
							} else {
								int i = serverLevel.getHeight(HeightmapTypeArgument.getHeightmap(commandContext, "heightmap"), Mth.floor(d), Mth.floor(e));
								return commandContext.getSource().withPosition(new Vec3(d, (double)i, e));
							}
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
				.then(
					Commands.literal("summon")
						.then(
							Commands.argument("entity", ResourceArgument.resource(commandBuildContext, Registries.ENTITY_TYPE))
								.suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
								.redirect(
									literalCommandNode,
									commandContext -> spawnEntityAndRedirect(commandContext.getSource(), ResourceArgument.getSummonableEntityType(commandContext, "entity"))
								)
						)
				)
				.then(createRelationOperations(literalCommandNode, Commands.literal("on")))
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
		return commandSourceStack.withCallback((commandSourceStackx, bl2, i) -> {
			for (String string : collection) {
				Score score = scoreboard.getOrCreatePlayerScore(string, objective);
				int j = bl ? i : (bl2 ? 1 : 0);
				score.setScore(j);
			}
		}, CALLBACK_CHAINER);
	}

	private static CommandSourceStack storeValue(CommandSourceStack commandSourceStack, CustomBossEvent customBossEvent, boolean bl, boolean bl2) {
		return commandSourceStack.withCallback((commandSourceStackx, bl3, i) -> {
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
		return commandSourceStack.withCallback((commandSourceStackx, bl2, i) -> {
			try {
				CompoundTag compoundTag = dataAccessor.getData();
				int j = bl ? i : (bl2 ? 1 : 0);
				nbtPath.set(compoundTag, (Tag)intFunction.apply(j));
				dataAccessor.setData(compoundTag);
			} catch (CommandSyntaxException var9) {
			}
		}, CALLBACK_CHAINER);
	}

	private static boolean isChunkLoaded(ServerLevel serverLevel, BlockPos blockPos) {
		ChunkPos chunkPos = new ChunkPos(blockPos);
		LevelChunk levelChunk = serverLevel.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
		return levelChunk == null ? false : levelChunk.getFullStatus() == FullChunkStatus.ENTITY_TICKING && serverLevel.areEntitiesLoaded(chunkPos.toLong());
	}

	private static ArgumentBuilder<CommandSourceStack, ?> addConditionals(
		CommandNode<CommandSourceStack> commandNode,
		LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder,
		boolean bl,
		CommandBuildContext commandBuildContext
	) {
		literalArgumentBuilder.then(
				Commands.literal("block")
					.then(
						Commands.argument("pos", BlockPosArgument.blockPos())
							.then(
								addConditional(
									commandNode,
									Commands.argument("block", BlockPredicateArgument.blockPredicate(commandBuildContext)),
									bl,
									commandContext -> BlockPredicateArgument.getBlockPredicate(commandContext, "block")
											.test(new BlockInWorld(commandContext.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), true))
								)
							)
					)
			)
			.then(
				Commands.literal("biome")
					.then(
						Commands.argument("pos", BlockPosArgument.blockPos())
							.then(
								addConditional(
									commandNode,
									Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(commandBuildContext, Registries.BIOME)),
									bl,
									commandContext -> ResourceOrTagArgument.getResourceOrTag(commandContext, "biome", Registries.BIOME)
											.test(commandContext.getSource().getLevel().getBiome(BlockPosArgument.getLoadedBlockPos(commandContext, "pos")))
								)
							)
					)
			)
			.then(
				Commands.literal("loaded")
					.then(
						addConditional(
							commandNode,
							Commands.argument("pos", BlockPosArgument.blockPos()),
							bl,
							commandContext -> isChunkLoaded(commandContext.getSource().getLevel(), BlockPosArgument.getBlockPos(commandContext, "pos"))
						)
					)
			)
			.then(
				Commands.literal("dimension")
					.then(
						addConditional(
							commandNode,
							Commands.argument("dimension", DimensionArgument.dimension()),
							bl,
							commandContext -> DimensionArgument.getDimension(commandContext, "dimension") == commandContext.getSource().getLevel()
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
			)
			.then(
				Commands.literal("function")
					.then(
						Commands.argument("name", FunctionArgument.functions())
							.suggests(FunctionCommand.SUGGEST_FUNCTION)
							.fork(commandNode, new ExecuteCommand.ExecuteIfFunctionCustomModifier(bl))
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
				commandContext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", i), false);
				return i;
			} else {
				throw ERROR_CONDITIONAL_FAILED.create();
			}
		} : commandContext -> {
			int i = commandNumericPredicate.test(commandContext);
			if (i == 0) {
				commandContext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
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
		LootParams lootParams = new LootParams.Builder(serverLevel)
			.withParameter(LootContextParams.ORIGIN, commandSourceStack.getPosition())
			.withOptionalParameter(LootContextParams.THIS_ENTITY, commandSourceStack.getEntity())
			.create(LootContextParamSets.COMMAND);
		LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
		lootContext.pushVisitedElement(LootContext.createVisitedEntry(lootItemCondition));
		return lootItemCondition.test(lootContext);
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
				commandContext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
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
			commandContext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", optionalInt.getAsInt()), false);
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
			commandContext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
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
		BoundingBox boundingBox = BoundingBox.fromCorners(blockPos, blockPos2);
		BoundingBox boundingBox2 = BoundingBox.fromCorners(blockPos3, blockPos3.offset(boundingBox.getLength()));
		BlockPos blockPos4 = new BlockPos(
			boundingBox2.minX() - boundingBox.minX(), boundingBox2.minY() - boundingBox.minY(), boundingBox2.minZ() - boundingBox.minZ()
		);
		int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
		if (i > 32768) {
			throw ERROR_AREA_TOO_LARGE.create(32768, i);
		} else {
			int j = 0;

			for (int k = boundingBox.minZ(); k <= boundingBox.maxZ(); k++) {
				for (int l = boundingBox.minY(); l <= boundingBox.maxY(); l++) {
					for (int m = boundingBox.minX(); m <= boundingBox.maxX(); m++) {
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

								if (blockEntity2.getType() != blockEntity.getType()) {
									return OptionalInt.empty();
								}

								CompoundTag compoundTag = blockEntity.saveWithoutMetadata();
								CompoundTag compoundTag2 = blockEntity2.saveWithoutMetadata();
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

	private static RedirectModifier<CommandSourceStack> expandOneToOneEntityRelation(Function<Entity, Optional<Entity>> function) {
		return commandContext -> {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			Entity entity = commandSourceStack.getEntity();
			return (Collection<CommandSourceStack>)(entity == null
				? List.of()
				: (Collection)((Optional)function.apply(entity))
					.filter(entityx -> !entityx.isRemoved())
					.map(entityx -> List.of(commandSourceStack.withEntity(entityx)))
					.orElse(List.of()));
		};
	}

	private static RedirectModifier<CommandSourceStack> expandOneToManyEntityRelation(Function<Entity, Stream<Entity>> function) {
		return commandContext -> {
			CommandSourceStack commandSourceStack = commandContext.getSource();
			Entity entity = commandSourceStack.getEntity();
			return entity == null ? List.of() : ((Stream)function.apply(entity)).filter(entityx -> !entityx.isRemoved()).map(commandSourceStack::withEntity).toList();
		};
	}

	private static LiteralArgumentBuilder<CommandSourceStack> createRelationOperations(
		CommandNode<CommandSourceStack> commandNode, LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder
	) {
		return literalArgumentBuilder.then(
				Commands.literal("owner")
					.fork(
						commandNode,
						expandOneToOneEntityRelation(entity -> entity instanceof OwnableEntity ownableEntity ? Optional.ofNullable(ownableEntity.getOwner()) : Optional.empty())
					)
			)
			.then(
				Commands.literal("leasher")
					.fork(commandNode, expandOneToOneEntityRelation(entity -> entity instanceof Mob mob ? Optional.ofNullable(mob.getLeashHolder()) : Optional.empty()))
			)
			.then(
				Commands.literal("target")
					.fork(
						commandNode,
						expandOneToOneEntityRelation(entity -> entity instanceof Targeting targeting ? Optional.ofNullable(targeting.getTarget()) : Optional.empty())
					)
			)
			.then(
				Commands.literal("attacker")
					.fork(
						commandNode,
						expandOneToOneEntityRelation(entity -> entity instanceof Attackable attackable ? Optional.ofNullable(attackable.getLastAttacker()) : Optional.empty())
					)
			)
			.then(Commands.literal("vehicle").fork(commandNode, expandOneToOneEntityRelation(entity -> Optional.ofNullable(entity.getVehicle()))))
			.then(Commands.literal("controller").fork(commandNode, expandOneToOneEntityRelation(entity -> Optional.ofNullable(entity.getControllingPassenger()))))
			.then(
				Commands.literal("origin")
					.fork(
						commandNode,
						expandOneToOneEntityRelation(
							entity -> entity instanceof TraceableEntity traceableEntity ? Optional.ofNullable(traceableEntity.getOwner()) : Optional.empty()
						)
					)
			)
			.then(Commands.literal("passengers").fork(commandNode, expandOneToManyEntityRelation(entity -> entity.getPassengers().stream())));
	}

	private static CommandSourceStack spawnEntityAndRedirect(CommandSourceStack commandSourceStack, Holder.Reference<EntityType<?>> reference) throws CommandSyntaxException {
		Entity entity = SummonCommand.createEntity(commandSourceStack, reference, commandSourceStack.getPosition(), new CompoundTag(), true);
		return commandSourceStack.withEntity(entity);
	}

	public static <T extends ExecutionCommandSource<T>> void scheduleFunctionConditionsAndTest(
		List<T> list,
		Function<T, T> function,
		IntPredicate intPredicate,
		ContextChain<T> contextChain,
		@Nullable CompoundTag compoundTag,
		ExecutionControl<T> executionControl,
		ExecuteCommand.CommandGetter<T, Collection<CommandFunction<T>>> commandGetter,
		boolean bl
	) throws CommandSyntaxException {
		List<T> list2 = new ArrayList(list.size());
		CommandContext<T> commandContext = contextChain.getTopContext();

		for (T executionCommandSource : list) {
			Collection<CommandFunction<T>> collection = commandGetter.get(commandContext.copyFor(executionCommandSource));
			int i = collection.size();
			if (i != 0) {
				T executionCommandSource2 = prepareCallback(function, intPredicate, list2, executionCommandSource, i == 1);

				for (CommandFunction<T> commandFunction : collection) {
					InstantiatedFunction<T> instantiatedFunction;
					try {
						instantiatedFunction = commandFunction.instantiate(compoundTag, executionCommandSource2.dispatcher(), executionCommandSource2);
					} catch (FunctionInstantiationException var19) {
						throw ERROR_FUNCTION_CONDITION_INSTANTATION_FAILURE.create(commandFunction.id(), var19.messageComponent());
					}

					executionControl.queueNext(new CallFunction<>(instantiatedFunction).bind(executionCommandSource2));
				}
			}
		}

		ContextChain<T> contextChain2 = contextChain.nextStage();
		String string = commandContext.getInput();
		executionControl.queueNext(new BuildContexts.Continuation<>(string, contextChain2, bl, list2));
	}

	private static <T extends ExecutionCommandSource<T>> T prepareCallback(
		Function<T, T> function, IntPredicate intPredicate, List<T> list, T executionCommandSource, boolean bl
	) {
		T executionCommandSource2 = (T)((ExecutionCommandSource)function.apply(executionCommandSource)).clearCallbacks();
		if (bl) {
			return executionCommandSource2.withReturnValueConsumer(i -> {
				if (intPredicate.test(i)) {
					list.add(executionCommandSource);
				}
			});
		} else {
			MutableBoolean mutableBoolean = new MutableBoolean();
			return executionCommandSource2.withReturnValueConsumer(i -> {
				if (mutableBoolean.isFalse() && intPredicate.test(i)) {
					list.add(executionCommandSource);
					mutableBoolean.setTrue();
				}
			});
		}
	}

	@FunctionalInterface
	public interface CommandGetter<T, R> {
		R get(CommandContext<T> commandContext) throws CommandSyntaxException;
	}

	@FunctionalInterface
	interface CommandNumericPredicate {
		int test(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;
	}

	@FunctionalInterface
	interface CommandPredicate {
		boolean test(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;
	}

	static class ExecuteIfFunctionCustomModifier implements CustomModifierExecutor.ModifierAdapter<CommandSourceStack> {
		private final IntPredicate check;

		ExecuteIfFunctionCustomModifier(boolean bl) {
			this.check = bl ? i -> i != 0 : i -> i == 0;
		}

		@Override
		public void apply(
			List<CommandSourceStack> list, ContextChain<CommandSourceStack> contextChain, boolean bl, ExecutionControl<CommandSourceStack> executionControl
		) throws CommandSyntaxException {
			ExecuteCommand.scheduleFunctionConditionsAndTest(
				list,
				FunctionCommand::modifySenderForExecution,
				this.check,
				contextChain,
				null,
				executionControl,
				commandContext -> FunctionArgument.getFunctions(commandContext, "name"),
				bl
			);
		}
	}
}
