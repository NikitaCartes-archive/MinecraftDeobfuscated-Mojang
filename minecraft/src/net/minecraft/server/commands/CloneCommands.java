package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CloneCommands {
	private static final int MAX_CLONE_AREA = 32768;
	private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(new TranslatableComponent("commands.clone.overlap"));
	private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("commands.clone.toobig", object, object2)
	);
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.clone.failed"));
	public static final Predicate<BlockInWorld> FILTER_AIR = blockInWorld -> !blockInWorld.getState().isAir();

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("clone")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("begin", BlockPosArgument.blockPos())
						.then(
							Commands.argument("end", BlockPosArgument.blockPos())
								.then(
									Commands.argument("destination", BlockPosArgument.blockPos())
										.executes(
											commandContext -> clone(
													commandContext.getSource(),
													BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
													BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
													BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
													blockInWorld -> true,
													CloneCommands.Mode.NORMAL
												)
										)
										.then(
											Commands.literal("replace")
												.executes(
													commandContext -> clone(
															commandContext.getSource(),
															BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
															BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
															BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
															blockInWorld -> true,
															CloneCommands.Mode.NORMAL
														)
												)
												.then(
													Commands.literal("force")
														.executes(
															commandContext -> clone(
																	commandContext.getSource(),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
																	blockInWorld -> true,
																	CloneCommands.Mode.FORCE
																)
														)
												)
												.then(
													Commands.literal("move")
														.executes(
															commandContext -> clone(
																	commandContext.getSource(),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
																	blockInWorld -> true,
																	CloneCommands.Mode.MOVE
																)
														)
												)
												.then(
													Commands.literal("normal")
														.executes(
															commandContext -> clone(
																	commandContext.getSource(),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
																	blockInWorld -> true,
																	CloneCommands.Mode.NORMAL
																)
														)
												)
										)
										.then(
											Commands.literal("masked")
												.executes(
													commandContext -> clone(
															commandContext.getSource(),
															BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
															BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
															BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
															FILTER_AIR,
															CloneCommands.Mode.NORMAL
														)
												)
												.then(
													Commands.literal("force")
														.executes(
															commandContext -> clone(
																	commandContext.getSource(),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
																	FILTER_AIR,
																	CloneCommands.Mode.FORCE
																)
														)
												)
												.then(
													Commands.literal("move")
														.executes(
															commandContext -> clone(
																	commandContext.getSource(),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
																	FILTER_AIR,
																	CloneCommands.Mode.MOVE
																)
														)
												)
												.then(
													Commands.literal("normal")
														.executes(
															commandContext -> clone(
																	commandContext.getSource(),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
																	FILTER_AIR,
																	CloneCommands.Mode.NORMAL
																)
														)
												)
										)
										.then(
											Commands.literal("filtered")
												.then(
													Commands.argument("filter", BlockPredicateArgument.blockPredicate())
														.executes(
															commandContext -> clone(
																	commandContext.getSource(),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
																	BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
																	BlockPredicateArgument.getBlockPredicate(commandContext, "filter"),
																	CloneCommands.Mode.NORMAL
																)
														)
														.then(
															Commands.literal("force")
																.executes(
																	commandContext -> clone(
																			commandContext.getSource(),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
																			BlockPredicateArgument.getBlockPredicate(commandContext, "filter"),
																			CloneCommands.Mode.FORCE
																		)
																)
														)
														.then(
															Commands.literal("move")
																.executes(
																	commandContext -> clone(
																			commandContext.getSource(),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
																			BlockPredicateArgument.getBlockPredicate(commandContext, "filter"),
																			CloneCommands.Mode.MOVE
																		)
																)
														)
														.then(
															Commands.literal("normal")
																.executes(
																	commandContext -> clone(
																			commandContext.getSource(),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "begin"),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "end"),
																			BlockPosArgument.getLoadedBlockPos(commandContext, "destination"),
																			BlockPredicateArgument.getBlockPredicate(commandContext, "filter"),
																			CloneCommands.Mode.NORMAL
																		)
																)
														)
												)
										)
								)
						)
				)
		);
	}

	private static int clone(
		CommandSourceStack commandSourceStack, BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Predicate<BlockInWorld> predicate, CloneCommands.Mode mode
	) throws CommandSyntaxException {
		BoundingBox boundingBox = BoundingBox.fromCorners(blockPos, blockPos2);
		BlockPos blockPos4 = blockPos3.offset(boundingBox.getLength());
		BoundingBox boundingBox2 = BoundingBox.fromCorners(blockPos3, blockPos4);
		if (!mode.canOverlap() && boundingBox2.intersects(boundingBox)) {
			throw ERROR_OVERLAP.create();
		} else {
			int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
			if (i > 32768) {
				throw ERROR_AREA_TOO_LARGE.create(32768, i);
			} else {
				ServerLevel serverLevel = commandSourceStack.getLevel();
				if (serverLevel.hasChunksAt(blockPos, blockPos2) && serverLevel.hasChunksAt(blockPos3, blockPos4)) {
					List<CloneCommands.CloneBlockInfo> list = Lists.<CloneCommands.CloneBlockInfo>newArrayList();
					List<CloneCommands.CloneBlockInfo> list2 = Lists.<CloneCommands.CloneBlockInfo>newArrayList();
					List<CloneCommands.CloneBlockInfo> list3 = Lists.<CloneCommands.CloneBlockInfo>newArrayList();
					Deque<BlockPos> deque = Lists.<BlockPos>newLinkedList();
					BlockPos blockPos5 = new BlockPos(
						boundingBox2.minX() - boundingBox.minX(), boundingBox2.minY() - boundingBox.minY(), boundingBox2.minZ() - boundingBox.minZ()
					);

					for (int j = boundingBox.minZ(); j <= boundingBox.maxZ(); j++) {
						for (int k = boundingBox.minY(); k <= boundingBox.maxY(); k++) {
							for (int l = boundingBox.minX(); l <= boundingBox.maxX(); l++) {
								BlockPos blockPos6 = new BlockPos(l, k, j);
								BlockPos blockPos7 = blockPos6.offset(blockPos5);
								BlockInWorld blockInWorld = new BlockInWorld(serverLevel, blockPos6, false);
								BlockState blockState = blockInWorld.getState();
								if (predicate.test(blockInWorld)) {
									BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos6);
									if (blockEntity != null) {
										CompoundTag compoundTag = blockEntity.save(new CompoundTag());
										list2.add(new CloneCommands.CloneBlockInfo(blockPos7, blockState, compoundTag));
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
						BlockEntity blockEntity3 = serverLevel.getBlockEntity(cloneBlockInfo.pos);
						Clearable.tryClear(blockEntity3);
						serverLevel.setBlock(cloneBlockInfo.pos, Blocks.BARRIER.defaultBlockState(), 2);
					}

					int lx = 0;

					for (CloneCommands.CloneBlockInfo cloneBlockInfo2 : list4) {
						if (serverLevel.setBlock(cloneBlockInfo2.pos, cloneBlockInfo2.state, 2)) {
							lx++;
						}
					}

					for (CloneCommands.CloneBlockInfo cloneBlockInfo2x : list2) {
						BlockEntity blockEntity4 = serverLevel.getBlockEntity(cloneBlockInfo2x.pos);
						if (cloneBlockInfo2x.tag != null && blockEntity4 != null) {
							cloneBlockInfo2x.tag.putInt("x", cloneBlockInfo2x.pos.getX());
							cloneBlockInfo2x.tag.putInt("y", cloneBlockInfo2x.pos.getY());
							cloneBlockInfo2x.tag.putInt("z", cloneBlockInfo2x.pos.getZ());
							blockEntity4.load(cloneBlockInfo2x.tag);
							blockEntity4.setChanged();
						}

						serverLevel.setBlock(cloneBlockInfo2x.pos, cloneBlockInfo2x.state, 2);
					}

					for (CloneCommands.CloneBlockInfo cloneBlockInfo2x : list5) {
						serverLevel.blockUpdated(cloneBlockInfo2x.pos, cloneBlockInfo2x.state.getBlock());
					}

					serverLevel.getBlockTicks().copy(boundingBox, blockPos5);
					if (lx == 0) {
						throw ERROR_FAILED.create();
					} else {
						commandSourceStack.sendSuccess(new TranslatableComponent("commands.clone.success", lx), true);
						return lx;
					}
				} else {
					throw BlockPosArgument.ERROR_NOT_LOADED.create();
				}
			}
		}
	}

	static class CloneBlockInfo {
		public final BlockPos pos;
		public final BlockState state;
		@Nullable
		public final CompoundTag tag;

		public CloneBlockInfo(BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag) {
			this.pos = blockPos;
			this.state = blockState;
			this.tag = compoundTag;
		}
	}

	static enum Mode {
		FORCE(true),
		MOVE(true),
		NORMAL(false);

		private final boolean canOverlap;

		private Mode(boolean bl) {
			this.canOverlap = bl;
		}

		public boolean canOverlap() {
			return this.canOverlap;
		}
	}
}
