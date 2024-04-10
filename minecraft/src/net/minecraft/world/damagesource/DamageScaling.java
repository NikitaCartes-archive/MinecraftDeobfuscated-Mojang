package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DamageScaling implements StringRepresentable {
	NEVER("never"),
	WHEN_CAUSED_BY_LIVING_NON_PLAYER("when_caused_by_living_non_player"),
	ALWAYS("always");

	public static final Codec<DamageScaling> CODEC = StringRepresentable.fromEnum(DamageScaling::values);
	private final String id;

	private DamageScaling(final String string2) {
		this.id = string2;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}
}
