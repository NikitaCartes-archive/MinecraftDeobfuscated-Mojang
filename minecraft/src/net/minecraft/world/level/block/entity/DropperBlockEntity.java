package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class DropperBlockEntity extends DispenserBlockEntity {
	private boolean isLunar = false;

	public DropperBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.DROPPER, blockPos, blockState);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.dropper");
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("Lunar", 1)) {
			this.isLunar = true;
		}
	}

	public void setLunar() {
		this.isLunar = true;
	}

	public boolean isLunar() {
		return this.isLunar;
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		super.saveAdditional(compoundTag);
		if (this.isLunar) {
			compoundTag.putBoolean("Lunar", true);
		}
	}
}
