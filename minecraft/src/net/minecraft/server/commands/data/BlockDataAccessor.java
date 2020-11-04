package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDataAccessor implements DataAccessor {
	private static final SimpleCommandExceptionType ERROR_NOT_A_BLOCK_ENTITY = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.data.block.invalid")
	);
	public static final Function<String, DataCommands.DataProvider> PROVIDER = string -> new DataCommands.DataProvider() {
			@Override
			public DataAccessor access(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
				BlockPos blockPos = BlockPosArgument.getLoadedBlockPos(commandContext, string + "Pos");
				BlockEntity blockEntity = ((CommandSourceStack)commandContext.getSource()).getLevel().getBlockEntity(blockPos);
				if (blockEntity == null) {
					throw BlockDataAccessor.ERROR_NOT_A_BLOCK_ENTITY.create();
				} else {
					return new BlockDataAccessor(blockEntity, blockPos);
				}
			}

			@Override
			public ArgumentBuilder<CommandSourceStack, ?> wrap(
				ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function
			) {
				return argumentBuilder.then(
					Commands.literal("block").then((ArgumentBuilder<CommandSourceStack, ?>)function.apply(Commands.argument(string + "Pos", BlockPosArgument.blockPos())))
				);
			}
		};
	private final BlockEntity entity;
	private final BlockPos pos;

	public BlockDataAccessor(BlockEntity blockEntity, BlockPos blockPos) {
		this.entity = blockEntity;
		this.pos = blockPos;
	}

	@Override
	public void setData(CompoundTag compoundTag) {
		compoundTag.putInt("x", this.pos.getX());
		compoundTag.putInt("y", this.pos.getY());
		compoundTag.putInt("z", this.pos.getZ());
		BlockState blockState = this.entity.getLevel().getBlockState(this.pos);
		this.entity.load(compoundTag);
		this.entity.setChanged();
		this.entity.getLevel().sendBlockUpdated(this.pos, blockState, blockState, 3);
	}

	@Override
	public CompoundTag getData() {
		return this.entity.save(new CompoundTag());
	}

	@Override
	public Component getModifiedSuccess() {
		return new TranslatableComponent("commands.data.block.modified", this.pos.getX(), this.pos.getY(), this.pos.getZ());
	}

	@Override
	public Component getPrintSuccess(Tag tag) {
		return new TranslatableComponent("commands.data.block.query", this.pos.getX(), this.pos.getY(), this.pos.getZ(), NbtUtils.toPrettyComponent(tag));
	}

	@Override
	public Component getPrintSuccess(NbtPathArgument.NbtPath nbtPath, double d, int i) {
		return new TranslatableComponent(
			"commands.data.block.get", nbtPath, this.pos.getX(), this.pos.getY(), this.pos.getZ(), String.format(Locale.ROOT, "%.2f", d), i
		);
	}
}
