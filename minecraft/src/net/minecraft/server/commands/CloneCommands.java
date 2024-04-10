package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CloneCommands {
	private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(Component.translatable("commands.clone.overlap"));
	private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("commands.clone.toobig", object, object2)
	);
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.clone.failed"));
	public static final Predicate<BlockInWorld> FILTER_AIR = blockInWorld -> !blockInWorld.getState().isAir();

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("clone")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(beginEndDestinationAndModeSuffix(commandBuildContext, commandContext -> commandContext.getSource().getLevel()))
				.then(
					Commands.literal("from")
						.then(
							Commands.argument("sourceDimension", DimensionArgument.dimension())
								.then(beginEndDestinationAndModeSuffix(commandBuildContext, commandContext -> DimensionArgument.getDimension(commandContext, "sourceDimension")))
						)
				)
		);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> beginEndDestinationAndModeSuffix(
		CommandBuildContext commandBuildContext, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> commandFunction
	) {
		return Commands.argument("begin", BlockPosArgument.blockPos())
			.then(
				Commands.argument("end", BlockPosArgument.blockPos())
					.then(destinationAndModeSuffix(commandBuildContext, commandFunction, commandContext -> commandContext.getSource().getLevel()))
					.then(
						Commands.literal("to")
							.then(
								Commands.argument("targetDimension", DimensionArgument.dimension())
									.then(
										destinationAndModeSuffix(commandBuildContext, commandFunction, commandContext -> DimensionArgument.getDimension(commandContext, "targetDimension"))
									)
							)
					)
			);
	}

	private static CloneCommands.DimensionAndPosition getLoadedDimensionAndPosition(
		CommandContext<CommandSourceStack> commandContext, ServerLevel serverLevel, String string
	) throws CommandSyntaxException {
		BlockPos blockPos = BlockPosArgument.getLoadedBlockPos(commandContext, serverLevel, string);
		return new CloneCommands.DimensionAndPosition(serverLevel, blockPos);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> destinationAndModeSuffix(
		CommandBuildContext commandBuildContext,
		CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> commandFunction,
		CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> commandFunction2
	) {
		CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandFunction3 = commandContext -> getLoadedDimensionAndPosition(
				commandContext, commandFunction.apply(commandContext), "begin"
			);
		CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandFunction4 = commandContext -> getLoadedDimensionAndPosition(
				commandContext, commandFunction.apply(commandContext), "end"
			);
		CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandFunction5 = commandContext -> getLoadedDimensionAndPosition(
				commandContext, commandFunction2.apply(commandContext), "destination"
			);
		return Commands.argument("destination", BlockPosArgument.blockPos())
			.executes(
				commandContext -> clone(
						commandContext.getSource(),
						commandFunction3.apply(commandContext),
						commandFunction4.apply(commandContext),
						commandFunction5.apply(commandContext),
						blockInWorld -> true,
						CloneCommands.Mode.NORMAL
					)
			)
			.then(
				wrapWithCloneMode(
					commandFunction3,
					commandFunction4,
					commandFunction5,
					commandContext -> blockInWorld -> true,
					Commands.literal("replace")
						.executes(
							commandContext -> clone(
									commandContext.getSource(),
									commandFunction3.apply(commandContext),
									commandFunction4.apply(commandContext),
									commandFunction5.apply(commandContext),
									blockInWorld -> true,
									CloneCommands.Mode.NORMAL
								)
						)
				)
			)
			.then(
				wrapWithCloneMode(
					commandFunction3,
					commandFunction4,
					commandFunction5,
					commandContext -> FILTER_AIR,
					Commands.literal("masked")
						.executes(
							commandContext -> clone(
									commandContext.getSource(),
									commandFunction3.apply(commandContext),
									commandFunction4.apply(commandContext),
									commandFunction5.apply(commandContext),
									FILTER_AIR,
									CloneCommands.Mode.NORMAL
								)
						)
				)
			)
			.then(
				Commands.literal("filtered")
					.then(
						wrapWithCloneMode(
							commandFunction3,
							commandFunction4,
							commandFunction5,
							commandContext -> BlockPredicateArgument.getBlockPredicate(commandContext, "filter"),
							Commands.argument("filter", BlockPredicateArgument.blockPredicate(commandBuildContext))
								.executes(
									commandContext -> clone(
											commandContext.getSource(),
											commandFunction3.apply(commandContext),
											commandFunction4.apply(commandContext),
											commandFunction5.apply(commandContext),
											BlockPredicateArgument.getBlockPredicate(commandContext, "filter"),
											CloneCommands.Mode.NORMAL
										)
								)
						)
					)
			);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> wrapWithCloneMode(
		CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandFunction,
		CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandFunction2,
		CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandFunction3,
		CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> commandFunction4,
		ArgumentBuilder<CommandSourceStack, ?> argumentBuilder
	) {
		return argumentBuilder.then(
				Commands.literal("force")
					.executes(
						commandContext -> clone(
								commandContext.getSource(),
								commandFunction.apply(commandContext),
								commandFunction2.apply(commandContext),
								commandFunction3.apply(commandContext),
								commandFunction4.apply(commandContext),
								CloneCommands.Mode.FORCE
							)
					)
			)
			.then(
				Commands.literal("move")
					.executes(
						commandContext -> clone(
								commandContext.getSource(),
								commandFunction.apply(commandContext),
								commandFunction2.apply(commandContext),
								commandFunction3.apply(commandContext),
								commandFunction4.apply(commandContext),
								CloneCommands.Mode.MOVE
							)
					)
			)
			.then(
				Commands.literal("normal")
					.executes(
						commandContext -> clone(
								commandContext.getSource(),
								commandFunction.apply(commandContext),
								commandFunction2.apply(commandContext),
								commandFunction3.apply(commandContext),
								commandFunction4.apply(commandContext),
								CloneCommands.Mode.NORMAL
							)
					)
			);
	}

	private static int clone(
		CommandSourceStack commandSourceStack,
		CloneCommands.DimensionAndPosition dimensionAndPosition,
		CloneCommands.DimensionAndPosition dimensionAndPosition2,
		CloneCommands.DimensionAndPosition dimensionAndPosition3,
		Predicate<BlockInWorld> predicate,
		CloneCommands.Mode mode
	) throws CommandSyntaxException {
		BlockPos blockPos = dimensionAndPosition.position();
		BlockPos blockPos2 = dimensionAndPosition2.position();
		BoundingBox boundingBox = BoundingBox.fromCorners(blockPos, blockPos2);
		BlockPos blockPos3 = dimensionAndPosition3.position();
		BlockPos blockPos4 = blockPos3.offset(boundingBox.getLength());
		BoundingBox boundingBox2 = BoundingBox.fromCorners(blockPos3, blockPos4);
		ServerLevel serverLevel = dimensionAndPosition.dimension();
		ServerLevel serverLevel2 = dimensionAndPosition3.dimension();
		if (!mode.canOverlap() && serverLevel == serverLevel2 && boundingBox2.intersects(boundingBox)) {
			throw ERROR_OVERLAP.create();
		} else {
			int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
			int j = commandSourceStack.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
			if (i > j) {
				throw ERROR_AREA_TOO_LARGE.create(j, i);
			} else if (serverLevel.hasChunksAt(blockPos, blockPos2) && serverLevel2.hasChunksAt(blockPos3, blockPos4)) {
				List<CloneCommands.CloneBlockInfo> list = Lists.<CloneCommands.CloneBlockInfo>newArrayList();
				List<CloneCommands.CloneBlockInfo> list2 = Lists.<CloneCommands.CloneBlockInfo>newArrayList();
				List<CloneCommands.CloneBlockInfo> list3 = Lists.<CloneCommands.CloneBlockInfo>newArrayList();
				Deque<BlockPos> deque = Lists.<BlockPos>newLinkedList();
				BlockPos blockPos5 = new BlockPos(
					boundingBox2.minX() - boundingBox.minX(), boundingBox2.minY() - boundingBox.minY(), boundingBox2.minZ() - boundingBox.minZ()
				);

				for (int k = boundingBox.minZ(); k <= boundingBox.maxZ(); k++) {
					for (int l = boundingBox.minY(); l <= boundingBox.maxY(); l++) {
						for (int m = boundingBox.minX(); m <= boundingBox.maxX(); m++) {
							BlockPos blockPos6 = new BlockPos(m, l, k);
							BlockPos blockPos7 = blockPos6.offset(blockPos5);
							BlockInWorld blockInWorld = new BlockInWorld(serverLevel, blockPos6, false);
							BlockState blockState = blockInWorld.getState();
							if (predicate.test(blockInWorld)) {
								BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos6);
								if (blockEntity != null) {
									CloneCommands.CloneBlockEntityInfo cloneBlockEntityInfo = new CloneCommands.CloneBlockEntityInfo(
										blockEntity.saveCustomOnly(commandSourceStack.registryAccess()), blockEntity.components()
									);
									list2.add(new CloneCommands.CloneBlockInfo(blockPos7, blockState, cloneBlockEntityInfo));
									deque.addLast(blockPos6);
								} else if (!blockState.isSolidRender(serverLevel, blockPos6) && !blockState.isCollisionShapeFullBlock(serverLevel, blockPos6)) {
									list3.add(new CloneCommands.CloneBlockInfo(blockPos7, blockState, null));
									deque.addFirst(blockPos6);
								} else {
									list.add(new CloneCommands.CloneBlockInfo(blockPos7, blockState, null));
									deque.addLast(blockPos6);
								}
							}
						}
					}
				}

				if (mode == CloneCommands.Mode.MOVE) {
					for (BlockPos blockPos8 : deque) {
						BlockEntity blockEntity2 = serverLevel.getBlockEntity(blockPos8);
						Clearable.tryClear(blockEntity2);
						serverLevel.setBlock(blockPos8, Blocks.BARRIER.defaultBlockState(), 2);
					}

					for (BlockPos blockPos8 : deque) {
						serverLevel.setBlock(blockPos8, Blocks.AIR.defaultBlockState(), 3);
					}
				}

				List<CloneCommands.CloneBlockInfo> list4 = Lists.<CloneCommands.CloneBlockInfo>newArrayList();
				list4.addAll(list);
				list4.addAll(list2);
				list4.addAll(list3);
				List<CloneCommands.CloneBlockInfo> list5 = Lists.reverse(list4);

				for (CloneCommands.CloneBlockInfo cloneBlockInfo : list5) {
					BlockEntity blockEntity3 = serverLevel2.getBlockEntity(cloneBlockInfo.pos);
					Clearable.tryClear(blockEntity3);
					serverLevel2.setBlock(cloneBlockInfo.pos, Blocks.BARRIER.defaultBlockState(), 2);
				}

				int mx = 0;

				for (CloneCommands.CloneBlockInfo cloneBlockInfo2 : list4) {
					if (serverLevel2.setBlock(cloneBlockInfo2.pos, cloneBlockInfo2.state, 2)) {
						mx++;
					}
				}

				for (CloneCommands.CloneBlockInfo cloneBlockInfo2x : list2) {
					BlockEntity blockEntity4 = serverLevel2.getBlockEntity(cloneBlockInfo2x.pos);
					if (cloneBlockInfo2x.blockEntityInfo != null && blockEntity4 != null) {
						blockEntity4.loadCustomOnly(cloneBlockInfo2x.blockEntityInfo.tag, serverLevel2.registryAccess());
						blockEntity4.setComponents(cloneBlockInfo2x.blockEntityInfo.components);
						blockEntity4.setChanged();
					}

					serverLevel2.setBlock(cloneBlockInfo2x.pos, cloneBlockInfo2x.state, 2);
				}

				for (CloneCommands.CloneBlockInfo cloneBlockInfo2x : list5) {
					serverLevel2.blockUpdated(cloneBlockInfo2x.pos, cloneBlockInfo2x.state.getBlock());
				}

				serverLevel2.getBlockTicks().copyAreaFrom(serverLevel.getBlockTicks(), boundingBox, blockPos5);
				if (mx == 0) {
					throw ERROR_FAILED.create();
				} else {
					int n = mx;
					commandSourceStack.sendSuccess(() -> Component.translatable("commands.clone.success", n), true);
					return mx;
				}
			} else {
				throw BlockPosArgument.ERROR_NOT_LOADED.create();
			}
		}
	}

	static record CloneBlockEntityInfo(CompoundTag tag, DataComponentMap components) {
	}

	static record CloneBlockInfo(BlockPos pos, BlockState state, @Nullable CloneCommands.CloneBlockEntityInfo blockEntityInfo) {
	}

	@FunctionalInterface
	interface CommandFunction<T, R> {
		R apply(T object) throws CommandSyntaxException;
	}

	static record DimensionAndPosition(ServerLevel dimension, BlockPos position) {
	}

	static enum Mode {
		FORCE(true),
		MOVE(true),
		NORMAL(false);

		private final boolean canOverlap;

		private Mode(final boolean bl) {
			this.canOverlap = bl;
		}

		public boolean canOverlap() {
			return this.canOverlap;
		}
	}
}
