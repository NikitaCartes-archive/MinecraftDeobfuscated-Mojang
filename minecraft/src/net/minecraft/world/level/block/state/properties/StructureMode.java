package net.minecraft.world.level.block.state.properties;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;

public enum StructureMode implements StringRepresentable {
	SAVE("save"),
	LOAD("load"),
	CORNER("corner"),
	DATA("data");

	private final String name;
	private final Component displayName;

	private StructureMode(String string2) {
		this.name = string2;
		this.displayName = new TranslatableComponent("structure_block.mode_info." + string2);
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	@Environment(EnvType.CLIENT)
	public Component getDisplayName() {
		return this.displayName;
	}
}
