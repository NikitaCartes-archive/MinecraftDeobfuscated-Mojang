package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public interface TextureObject {
	void pushFilter(boolean bl, boolean bl2);

	void popFilter();

	void load(ResourceManager resourceManager) throws IOException;

	int getId();

	default void bind() {
		RenderSystem.bindTexture(this.getId());
	}

	default void reset(TextureManager textureManager, ResourceManager resourceManager, ResourceLocation resourceLocation, Executor executor) {
		textureManager.register(resourceLocation, this);
	}
}
