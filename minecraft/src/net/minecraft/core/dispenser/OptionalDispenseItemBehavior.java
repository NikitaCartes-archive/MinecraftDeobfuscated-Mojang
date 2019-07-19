package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;

public abstract class OptionalDispenseItemBehavior extends DefaultDispenseItemBehavior {
	protected boolean success = true;

	@Override
	protected void playSound(BlockSource blockSource) {
		blockSource.getLevel().levelEvent(this.success ? 1000 : 1001, blockSource.getPos(), 0);
	}
}
