package net.minecraft.world.level.block.entity.vault;

import net.minecraft.util.Mth;

public class VaultClientData {
	public static final float ROTATION_SPEED = 10.0F;
	private float currentSpin;
	private float previousSpin;

	VaultClientData() {
	}

	public float currentSpin() {
		return this.currentSpin;
	}

	public float previousSpin() {
		return this.previousSpin;
	}

	void updateDisplayItemSpin() {
		this.previousSpin = this.currentSpin;
		this.currentSpin = Mth.wrapDegrees(this.currentSpin + 10.0F);
	}
}
