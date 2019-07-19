package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum RailShape implements StringRepresentable {
	NORTH_SOUTH(0, "north_south"),
	EAST_WEST(1, "east_west"),
	ASCENDING_EAST(2, "ascending_east"),
	ASCENDING_WEST(3, "ascending_west"),
	ASCENDING_NORTH(4, "ascending_north"),
	ASCENDING_SOUTH(5, "ascending_south"),
	SOUTH_EAST(6, "south_east"),
	SOUTH_WEST(7, "south_west"),
	NORTH_WEST(8, "north_west"),
	NORTH_EAST(9, "north_east");

	private final int data;
	private final String name;

	private RailShape(int j, String string2) {
		this.data = j;
		this.name = string2;
	}

	public int getData() {
		return this.data;
	}

	public String toString() {
		return this.name;
	}

	public boolean isAscending() {
		return this == ASCENDING_NORTH || this == ASCENDING_EAST || this == ASCENDING_SOUTH || this == ASCENDING_WEST;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
