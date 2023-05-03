package net.minecraft.client.gui.font.providers;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public record ProviderReferenceDefinition(ResourceLocation id) implements GlyphProviderDefinition {
	public static final MapCodec<ProviderReferenceDefinition> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(ProviderReferenceDefinition::id)).apply(instance, ProviderReferenceDefinition::new)
	);

	@Override
	public GlyphProviderType type() {
		return GlyphProviderType.REFERENCE;
	}

	@Override
	public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
		return Either.right(new GlyphProviderDefinition.Reference(this.id));
	}
}
