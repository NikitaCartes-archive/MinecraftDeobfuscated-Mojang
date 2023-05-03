package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public interface GlyphProviderDefinition {
	Codec<GlyphProviderDefinition> CODEC = GlyphProviderType.CODEC
		.dispatch(GlyphProviderDefinition::type, glyphProviderType -> glyphProviderType.mapCodec().codec());

	GlyphProviderType type();

	Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack();

	@Environment(EnvType.CLIENT)
	public interface Loader {
		GlyphProvider load(ResourceManager resourceManager) throws IOException;
	}

	@Environment(EnvType.CLIENT)
	public static record Reference(ResourceLocation id) {
	}
}
