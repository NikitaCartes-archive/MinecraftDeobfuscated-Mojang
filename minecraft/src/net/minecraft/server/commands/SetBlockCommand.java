package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SetBlockCommand {
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.setblock.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("setblock")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("pos", BlockPosArgument.blockPos())
						.then(
							Commands.argument("block", BlockStateArgument.block(commandBuildContext))
								.executes(
									commandContext -> setBlock(
											commandContext.getSource(),
											BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
											BlockStateArgument.getBlock(commandContext, "block"),
											SetBlockCommand.Mode.REPLACE,
											null
										)
								)
								.then(
									Commands.literal("destroy")
										.executes(
											commandContext -> setBlock(
													commandContext.getSource(),
													BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
													BlockStateArgument.getBlock(commandContext, "block"),
													SetBlockCommand.Mode.DESTROY,
													null
												)
										)
								)
								.then(
									Commands.literal("keep")
										.executes(
											commandContext -> setBlock(
													commandContext.getSource(),
													BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
													BlockStateArgument.getBlock(commandContext, "block"),
													SetBlockCommand.Mode.REPLACE,
													blockInWorld -> blockInWorld.getLevel().isEmptyBlock(blockInWorld.getPos())
												)
										)
								)
								.then(
									Commands.literal("replace")
										.executes(
											commandContext -> setBlock(
													commandContext.getSource(),
													BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
													BlockStateArgument.getBlock(commandContext, "block"),
													SetBlockCommand.Mode.REPLACE,
													null
												)
										)
								)
						)
				)
		);
	}

	private static int setBlock(
		CommandSourceStack commandSourceStack, BlockPos blockPos, BlockInput blockInput, SetBlockCommand.Mode mode, @Nullable Predicate<BlockInWorld> predicate
	) throws CommandSyntaxException {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		if (predicate != null && !predicate.test(new BlockInWorld(serverLevel, blockPos, true))) {
			throw ERROR_FAILED.create();
		} else {
			boolean bl;
			if (mode == SetBlockCommand.Mode.DESTROY) {
				serverLevel.destroyBlock(blockPos, true);
				bl = !blockInput.getState().isAir() || !serverLevel.getBlockState(blockPos).isAir();
			} else {
				BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
				Clearable.tryClear(blockEntity);
				bl = true;
			}

			if (bl && !blockInput.place(serverLevel, blockPos, 2)) {
				throw ERROR_FAILED.create();
			} else {
				serverLevel.blockUpdated(blockPos, blockInput.getState().getBlock());
				commandSourceStack.sendSuccess(Component.translatable("commands.setblock.success", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
				return 1;
			}
		}
	}

	public interface Filter {
		@Nullable
		BlockInput filter(BoundingBox boundingBox, BlockPos blockPos, BlockInput blockInput, ServerLevel serverLevel);
	}

	public static enum Mode {
		REPLACE,
		DESTROY;
	}
}
