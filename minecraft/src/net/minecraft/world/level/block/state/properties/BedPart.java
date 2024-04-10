package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum BedPart implements StringRepresentable {
	HEAD("head"),
	FOOT("foot");

	private final String name;

	private BedPart(final String string2) {
		this.name = string2;
	}

	public String toString() {
		return this.name;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
