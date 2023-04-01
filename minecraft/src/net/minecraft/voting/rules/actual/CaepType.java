package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum CaepType implements StringRepresentable {
	NONE("blonk"),
	AWESOM("awesom"),
	SQUID("squid"),
	VETERINARIAN("veterinarian"),
	NO_CIRCLE("no_circle"),
	NYAN("nyan");

	public static final Codec<CaepType> CODEC = StringRepresentable.fromEnum(CaepType::values);
	private final String id;
	private final Component displayName;
	private final ResourceLocation textureId;

	private CaepType(String string2) {
		this.id = string2;
		this.displayName = Component.translatable("rule.caep." + string2);
		this.textureId = new ResourceLocation("textures/entity/player/caeps/" + string2 + ".png");
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}

	public Component getDisplayName() {
		return this.displayName;
	}

	public ResourceLocation getTextureId() {
		return this.textureId;
	}
}
