package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum BellAttachType implements StringRepresentable {
	FLOOR("floor"),
	CEILING("ceiling"),
	SINGLE_WALL("single_wall"),
	DOUBLE_WALL("double_wall");

	private final String name;

	private BellAttachType(final String string2) {
		this.name = string2;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
