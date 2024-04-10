package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum WallSide implements StringRepresentable {
	NONE("none"),
	LOW("low"),
	TALL("tall");

	private final String name;

	private WallSide(final String string2) {
		this.name = string2;
	}

	public String toString() {
		return this.getSerializedName();
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
