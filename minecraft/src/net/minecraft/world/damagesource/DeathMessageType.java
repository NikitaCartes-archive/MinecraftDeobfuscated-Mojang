package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DeathMessageType implements StringRepresentable {
	DEFAULT("default"),
	FALL_VARIANTS("fall_variants"),
	INTENTIONAL_GAME_DESIGN("intentional_game_design");

	public static final Codec<DeathMessageType> CODEC = StringRepresentable.fromEnum(DeathMessageType::values);
	private final String id;

	private DeathMessageType(final String string2) {
		this.id = string2;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}
}
