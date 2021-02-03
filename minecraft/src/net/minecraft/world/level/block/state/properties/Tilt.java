package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum Tilt implements StringRepresentable {
	NONE("none", true),
	UNSTABLE("unstable", false),
	PARTIAL("partial", true),
	FULL("full", true);

	private final String name;
	private final boolean causesVibration;

	private Tilt(String string2, boolean bl) {
		this.name = string2;
		this.causesVibration = bl;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public boolean causesVibration() {
		return this.causesVibration;
	}
}
