package net.minecraft.core.dispenser;

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
		blockSource.level().levelEvent(this.isSuccess() ? 1000 : 1001, blockSource.pos(), 0);
	}
}
