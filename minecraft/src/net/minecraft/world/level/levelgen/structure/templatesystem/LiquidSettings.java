package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum LiquidSettings implements StringRepresentable {
	IGNORE_WATERLOGGING("ignore_waterlogging"),
	APPLY_WATERLOGGING("apply_waterlogging");

	public static Codec<LiquidSettings> CODEC = StringRepresentable.fromValues(LiquidSettings::values);
	private final String name;

	private LiquidSettings(final String string2) {
		this.name = string2;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}
}
