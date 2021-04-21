package net.minecraft.world;

public enum InteractionResult {
	SUCCESS,
	CONSUME,
	CONSUME_PARTIAL,
	PASS,
	FAIL;

	public boolean consumesAction() {
		return this == SUCCESS || this == CONSUME || this == CONSUME_PARTIAL;
	}

	public boolean shouldSwing() {
		return this == SUCCESS;
	}

	public boolean shouldAwardStats() {
		return this == SUCCESS || this == CONSUME;
	}

	public static InteractionResult sidedSuccess(boolean bl) {
		return bl ? SUCCESS : CONSUME;
	}
}
