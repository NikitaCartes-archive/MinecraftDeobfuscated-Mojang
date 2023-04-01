package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum VehicleCollisionRule implements StringRepresentable {
	NONE("none"),
	BREAK("break"),
	EXPLODE("explode");

	public static final Codec<VehicleCollisionRule> CODEC = StringRepresentable.fromEnum(VehicleCollisionRule::values);
	private final String id;

	private VehicleCollisionRule(String string2) {
		this.id = string2;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}
}
