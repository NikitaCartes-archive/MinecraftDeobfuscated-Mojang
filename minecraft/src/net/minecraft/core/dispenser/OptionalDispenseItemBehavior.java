package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;

public abstract class OptionalDispenseItemBehavior extends DefaultDispenseItemBehavior {
	private boolean success = true;

	public boolean isSuccess() {
		return this.success;
	}

	public void setSuccess(boolean bl) {
		this.success = bl;
	}

	@Override
	protected void playSound(BlockSource blockSource) {
		blockSource.getLevel().levelEvent(this.isSuccess() ? 1000 : 1001, blockSource.getPos(), 0);
	}
}
