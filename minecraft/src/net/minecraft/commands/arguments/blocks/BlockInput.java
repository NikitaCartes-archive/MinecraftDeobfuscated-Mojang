package net.minecraft.commands.arguments.blocks;

import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockInput implements Predicate<BlockInWorld> {
	private final BlockState state;
	private final Set<Property<?>> properties;
	@Nullable
	private final CompoundTag tag;

	public BlockInput(BlockState blockState, Set<Property<?>> set, @Nullable CompoundTag compoundTag) {
		this.state = blockState;
		this.properties = set;
		this.tag = compoundTag;
	}

	public BlockState getState() {
		return this.state;
	}

	public boolean test(BlockInWorld blockInWorld) {
		BlockState blockState = blockInWorld.getState();
		if (!blockState.is(this.state.getBlock())) {
			return false;
		} else {
			for (Property<?> property : this.properties) {
				if (blockState.getValue(property) != this.state.getValue(property)) {
					return false;
				}
			}

			if (this.tag == null) {
				return true;
			} else {
				BlockEntity blockEntity = blockInWorld.getEntity();
				return blockEntity != null && NbtUtils.compareNbt(this.tag, blockEntity.save(new CompoundTag()), true);
			}
		}
	}

	public boolean place(ServerLevel serverLevel, BlockPos blockPos, int i) {
		BlockState blockState = Block.updateFromNeighbourShapes(this.state, serverLevel, blockPos);
		if (blockState.isAir()) {
			blockState = this.state;
		}

		if (!serverLevel.setBlock(blockPos, blockState, i)) {
			return false;
		} else {
			if (this.tag != null) {
				BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
				if (blockEntity != null) {
					CompoundTag compoundTag = this.tag.copy();
					compoundTag.putInt("x", blockPos.getX());
					compoundTag.putInt("y", blockPos.getY());
					compoundTag.putInt("z", blockPos.getZ());
					blockEntity.load(compoundTag);
				}
			}

			return true;
		}
	}
}
