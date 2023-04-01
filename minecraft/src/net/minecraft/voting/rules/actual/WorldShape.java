package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum WorldShape implements StringRepresentable {
	ONE("1", "earth_1.png"),
	A("a", "earth_a.png"),
	PRIME("prime", "earth_prime.png"),
	NONE("none", "earth.png");

	public static final Codec<WorldShape> CODEC = StringRepresentable.fromEnum(WorldShape::values);
	private final String id;
	private final ResourceLocation texture;

	private WorldShape(String string2, String string3) {
		this.id = string2;
		this.texture = new ResourceLocation("textures/environment/" + string3);
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}

	public ResourceLocation getTexture() {
		return this.texture;
	}
}
