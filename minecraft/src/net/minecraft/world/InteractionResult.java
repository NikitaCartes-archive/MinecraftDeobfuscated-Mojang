package net.minecraft.world;

public enum InteractionResult {
	SUCCESS,
	CONSUME,
	PASS,
	FAIL;

	public boolean consumesAction() {
		return this == SUCCESS || this == CONSUME;
	}

	public boolean shouldSwing() {
		return this == SUCCESS;
	}
}
