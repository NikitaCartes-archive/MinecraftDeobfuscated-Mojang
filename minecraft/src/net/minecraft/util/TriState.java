package net.minecraft.util;

public enum TriState {
	TRUE,
	FALSE,
	DEFAULT;

	public boolean toBoolean(boolean bl) {
		return switch (this) {
			case TRUE -> true;
			case FALSE -> false;
			default -> bl;
		};
	}
}
