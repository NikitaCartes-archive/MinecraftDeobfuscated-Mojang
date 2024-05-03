package net.minecraft.world.entity.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public record PaintingVariant(int width, int height, ResourceLocation assetId) {
	public static final Codec<PaintingVariant> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.intRange(1, 16).fieldOf("width").forGetter(PaintingVariant::width),
					ExtraCodecs.intRange(1, 16).fieldOf("height").forGetter(PaintingVariant::height),
					ResourceLocation.CODEC.fieldOf("asset_id").forGetter(PaintingVariant::assetId)
				)
				.apply(instance, PaintingVariant::new)
	);
	public static final Codec<Holder<PaintingVariant>> CODEC = RegistryFileCodec.create(Registries.PAINTING_VARIANT, DIRECT_CODEC);

	public int area() {
		return this.width() * this.height();
	}
}
