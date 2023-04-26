package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public interface GlyphProviderBuilder {
	Either<GlyphProviderBuilder.Loader, GlyphProviderBuilder.Reference> build();

	@Environment(EnvType.CLIENT)
	public interface Loader {
		GlyphProvider load(ResourceManager resourceManager) throws IOException;
	}

	@Environment(EnvType.CLIENT)
	public static record Reference(ResourceLocation id) {
	}
}
