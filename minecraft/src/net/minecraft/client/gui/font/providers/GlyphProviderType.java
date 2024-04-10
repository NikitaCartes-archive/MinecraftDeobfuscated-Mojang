package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.SpaceProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringRepresentable;

@Environment(EnvType.CLIENT)
public enum GlyphProviderType implements StringRepresentable {
	BITMAP("bitmap", BitmapProvider.Definition.CODEC),
	TTF("ttf", TrueTypeGlyphProviderDefinition.CODEC),
	SPACE("space", SpaceProvider.Definition.CODEC),
	UNIHEX("unihex", UnihexProvider.Definition.CODEC),
	REFERENCE("reference", ProviderReferenceDefinition.CODEC);

	public static final Codec<GlyphProviderType> CODEC = StringRepresentable.fromEnum(GlyphProviderType::values);
	private final String name;
	private final MapCodec<? extends GlyphProviderDefinition> codec;

	private GlyphProviderType(final String string2, final MapCodec<? extends GlyphProviderDefinition> mapCodec) {
		this.name = string2;
		this.codec = mapCodec;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public MapCodec<? extends GlyphProviderDefinition> mapCodec() {
		return this.codec;
	}
}
