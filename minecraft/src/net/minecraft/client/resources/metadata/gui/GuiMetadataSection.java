package net.minecraft.client.resources.metadata.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ExtraCodecs;

@Environment(EnvType.CLIENT)
public record GuiMetadataSection(GuiSpriteScaling scaling) {
	public static final GuiMetadataSection DEFAULT = new GuiMetadataSection(GuiSpriteScaling.DEFAULT);
	public static final Codec<GuiMetadataSection> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(GuiSpriteScaling.CODEC, "scaling", GuiSpriteScaling.DEFAULT).forGetter(GuiMetadataSection::scaling)
				)
				.apply(instance, GuiMetadataSection::new)
	);
	public static final MetadataSectionType<GuiMetadataSection> TYPE = MetadataSectionType.fromCodec("gui", CODEC);
}
