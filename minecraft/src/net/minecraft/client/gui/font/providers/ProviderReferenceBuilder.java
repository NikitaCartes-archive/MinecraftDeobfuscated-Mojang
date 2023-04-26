package net.minecraft.client.gui.font.providers;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ProviderReferenceBuilder implements GlyphProviderBuilder {
	public static final Codec<ProviderReferenceBuilder> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(providerReferenceBuilder -> providerReferenceBuilder.id))
				.apply(instance, ProviderReferenceBuilder::new)
	);
	private final ResourceLocation id;

	private ProviderReferenceBuilder(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	public static GlyphProviderBuilder fromJson(JsonObject jsonObject) {
		return CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(false, string -> {
		});
	}

	@Override
	public Either<GlyphProviderBuilder.Loader, GlyphProviderBuilder.Reference> build() {
		return Either.right(new GlyphProviderBuilder.Reference(this.id));
	}
}
