package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum StructureMode implements StringRepresentable {
	SAVE("save"),
	LOAD("load"),
	CORNER("corner"),
	DATA("data");

	private final String name;

	private StructureMode(String string2) {
		this.name = string2;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
