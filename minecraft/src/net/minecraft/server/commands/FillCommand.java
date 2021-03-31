package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FillCommand {
	private static final int MAX_FILL_AREA = 32768;
	private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
		(object, object2) -> new TranslatableComponent("commands.fill.toobig", object, object2)
	);
	private static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), null);
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.fill.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("fill")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("from", BlockPosArgument.blockPos())
						.then(
							Commands.argument("to", BlockPosArgument.blockPos())
								.then(
									Commands.argument("block", BlockStateArgument.block())
										.executes(
											commandContext -> fillBlocks(
													commandContext.getSource(),
													BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")),
													BlockStateArgument.getBlock(commandContext, "block"),
													FillCommand.Mode.REPLACE,
													null
												)
										)
										.then(
											Commands.literal("replace")
												.executes(
													commandContext -> fillBlocks(
															commandContext.getSource(),
															BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")),
															BlockStateArgument.getBlock(commandContext, "block"),
															FillCommand.Mode.REPLACE,
															null
														)
												)
												.then(
													Commands.argument("filter", BlockPredicateArgument.blockPredicate())
														.executes(
															commandContext -> fillBlocks(
																	commandContext.getSource(),
																	BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")),
																	BlockStateArgument.getBlock(commandContext, "block"),
																	FillCommand.Mode.REPLACE,
																	BlockPredicateArgument.getBlockPredicate(commandContext, "filter")
																)
														)
												)
										)
										.then(
											Commands.literal("keep")
												.executes(
													commandContext -> fillBlocks(
															commandContext.getSource(),
															BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")),
															BlockStateArgument.getBlock(commandContext, "block"),
															FillCommand.Mode.REPLACE,
															blockInWorld -> blockInWorld.getLevel().isEmptyBlock(blockInWorld.getPos())
														)
												)
										)
										.then(
											Commands.literal("outline")
												.executes(
													commandContext -> fillBlocks(
															commandContext.getSource(),
															BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")),
															BlockStateArgument.getBlock(commandContext, "block"),
															FillCommand.Mode.OUTLINE,
															null
														)
												)
										)
										.then(
											Commands.literal("hollow")
												.executes(
													commandContext -> fillBlocks(
															commandContext.getSource(),
															BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")),
															BlockStateArgument.getBlock(commandContext, "block"),
															FillCommand.Mode.HOLLOW,
															null
														)
												)
										)
										.then(
											Commands.literal("destroy")
												.executes(
													commandContext -> fillBlocks(
															commandContext.getSource(),
															BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")),
															BlockStateArgument.getBlock(commandContext, "block"),
															FillCommand.Mode.DESTROY,
															null
														)
												)
										)
								)
						)
				)
		);
	}

	private static int fillBlocks(
		CommandSourceStack commandSourceStack, BoundingBox boundingBox, BlockInput blockInput, FillCommand.Mode mode, @Nullable Predicate<BlockInWorld> predicate
	) throws CommandSyntaxException {
		int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
		if (i > 32768) {
			throw ERROR_AREA_TOO_LARGE.create(32768, i);
		} else {
			List<BlockPos> list = Lists.<BlockPos>newArrayList();
			ServerLevel serverLevel = commandSourceStack.getLevel();
			int j = 0;

			for (BlockPos blockPos : BlockPos.betweenClosed(
				boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ()
			)) {
				if (predicate == null || predicate.test(new BlockInWorld(serverLevel, blockPos, true))) {
					BlockInput blockInput2 = mode.filter.filter(boundingBox, blockPos, blockInput, serverLevel);
					if (blockInput2 != null) {
						BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
						Clearable.tryClear(blockEntity);
						if (blockInput2.place(serverLevel, blockPos, 2)) {
							list.add(blockPos.immutable());
							j++;
						}
					}
				}
			}

			for (BlockPos blockPosx : list) {
				Block block = serverLevel.getBlockState(blockPosx).getBlock();
				serverLevel.blockUpdated(blockPosx, block);
			}

			if (j == 0) {
				throw ERROR_FAILED.create();
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.fill.success", j), true);
				return j;
			}
		}
	}

	static enum Mode {
		REPLACE((boundingBox, blockPos, blockInput, serverLevel) -> blockInput),
		OUTLINE(
			(boundingBox, blockPos, blockInput, serverLevel) -> blockPos.getX() != boundingBox.minX()
						&& blockPos.getX() != boundingBox.maxX()
						&& blockPos.getY() != boundingBox.minY()
						&& blockPos.getY() != boundingBox.maxY()
						&& blockPos.getZ() != boundingBox.minZ()
						&& blockPos.getZ() != boundingBox.maxZ()
					? null
					: blockInput
		),
		HOLLOW(
			(boundingBox, blockPos, blockInput, serverLevel) -> blockPos.getX() != boundingBox.minX()
						&& blockPos.getX() != boundingBox.maxX()
						&& blockPos.getY() != boundingBox.minY()
						&& blockPos.getY() != boundingBox.maxY()
						&& blockPos.getZ() != boundingBox.minZ()
						&& blockPos.getZ() != boundingBox.maxZ()
					? FillCommand.HOLLOW_CORE
					: blockInput
		),
		DESTROY((boundingBox, blockPos, blockInput, serverLevel) -> {
			serverLevel.destroyBlock(blockPos, true);
			return blockInput;
		});

		public final SetBlockCommand.Filter filter;

		private Mode(SetBlockCommand.Filter filter) {
			this.filter = filter;
		}
	}
}
