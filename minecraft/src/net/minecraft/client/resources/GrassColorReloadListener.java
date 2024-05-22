package net.minecraft.client.resources;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GrassColor;

@Environment(EnvType.CLIENT)
public class GrassColorReloadListener extends SimplePreparableReloadListener<int[]> {
	private static final ResourceLocation LOCATION = ResourceLocation.withDefaultNamespace("textures/colormap/grass.png");

	protected int[] prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		try {
			return LegacyStuffWrapper.getPixels(resourceManager, LOCATION);
		} catch (IOException var4) {
			throw new IllegalStateException("Failed to load grass color texture", var4);
		}
	}

	protected void apply(int[] is, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		GrassColor.init(is);
	}
}
