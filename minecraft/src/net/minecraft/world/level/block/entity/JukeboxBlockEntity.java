package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class JukeboxBlockEntity extends BlockEntity implements Clearable {
	private ItemStack record = ItemStack.EMPTY;

	public JukeboxBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.JUKEBOX, blockPos, blockState);
	}

	@Override
	public void load(CompoundTag compoundTag) {
		super.load(compoundTag);
		if (compoundTag.contains("RecordItem", 10)) {
			this.setRecord(ItemStack.of(compoundTag.getCompound("RecordItem")));
		}
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		if (!this.getRecord().isEmpty()) {
			compoundTag.put("RecordItem", this.getRecord().save(new CompoundTag()));
		}

		return compoundTag;
	}

	public ItemStack getRecord() {
		return this.record;
	}

	public void setRecord(ItemStack itemStack) {
		this.record = itemStack;
		this.setChanged();
	}

	@Override
	public void clearContent() {
		this.setRecord(ItemStack.EMPTY);
	}
}
