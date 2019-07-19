package net.minecraft.world.level.block.entity;

public class TrappedChestBlockEntity extends ChestBlockEntity {
	public TrappedChestBlockEntity() {
		super(BlockEntityType.TRAPPED_CHEST);
	}

	@Override
	protected void signalOpenCount() {
		super.signalOpenCount();
		this.level.updateNeighborsAt(this.worldPosition.below(), this.getBlockState().getBlock());
	}
}
