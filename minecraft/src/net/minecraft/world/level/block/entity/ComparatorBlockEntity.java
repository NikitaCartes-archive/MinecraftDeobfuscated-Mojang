package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class ComparatorBlockEntity extends BlockEntity {
	private int output;

	public ComparatorBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.COMPARATOR, blockPos, blockState);
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		compoundTag.putInt("OutputSignal", this.output);
		return compoundTag;
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		this.output = compoundTag.getInt("OutputSignal");
	}

	public int getOutputSignal() {
		return this.output;
	}

	public void setOutputSignal(int i) {
		this.output = i;
	}
}
