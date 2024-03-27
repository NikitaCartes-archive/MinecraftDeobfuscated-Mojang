package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public interface GlyphProviderDefinition {
	MapCodec<GlyphProviderDefinition> MAP_CODEC = GlyphProviderType.CODEC.dispatchMap(GlyphProviderDefinition::type, GlyphProviderType::mapCodec);

	GlyphProviderType type();

	Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack();

	@Environment(EnvType.CLIENT)
	public static record Conditional(GlyphProviderDefinition definition, FontOption.Filter filter) {
		public static final Codec<GlyphProviderDefinition.Conditional> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						GlyphProviderDefinition.MAP_CODEC.forGetter(GlyphProviderDefinition.Conditional::definition),
						FontOption.Filter.CODEC.optionalFieldOf("filter", FontOption.Filter.ALWAYS_PASS).forGetter(GlyphProviderDefinition.Conditional::filter)
					)
					.apply(instance, GlyphProviderDefinition.Conditional::new)
		);
	}

	@Environment(EnvType.CLIENT)
	public interface Loader {
		GlyphProvider load(ResourceManager resourceManager) throws IOException;
	}

	@Environment(EnvType.CLIENT)
	public static record Reference(ResourceLocation id) {
	}
}
