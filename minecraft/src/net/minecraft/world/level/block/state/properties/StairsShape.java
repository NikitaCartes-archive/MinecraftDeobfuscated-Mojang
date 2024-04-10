package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum StairsShape implements StringRepresentable {
	STRAIGHT("straight"),
	INNER_LEFT("inner_left"),
	INNER_RIGHT("inner_right"),
	OUTER_LEFT("outer_left"),
	OUTER_RIGHT("outer_right");

	private final String name;

	private StairsShape(final String string2) {
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
