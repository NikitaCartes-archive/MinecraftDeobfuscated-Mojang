package net.minecraft.client.renderer.texture.atlas;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public interface SpriteSource {
	FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");

	void run(ResourceManager resourceManager, SpriteSource.Output output);

	SpriteSourceType type();

	@Environment(EnvType.CLIENT)
	public interface Output {
		default void add(ResourceLocation resourceLocation, Resource resource) {
			this.add(resourceLocation, () -> SpriteLoader.loadSprite(resourceLocation, resource));
		}

		void add(ResourceLocation resourceLocation, SpriteSource.SpriteSupplier spriteSupplier);

		void removeAll(Predicate<ResourceLocation> predicate);
	}

	@Environment(EnvType.CLIENT)
	public interface SpriteSupplier extends Supplier<SpriteContents> {
		default void discard() {
		}
	}
}
