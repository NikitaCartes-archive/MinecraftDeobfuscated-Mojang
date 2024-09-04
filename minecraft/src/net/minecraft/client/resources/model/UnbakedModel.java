package net.minecraft.client.resources.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public interface UnbakedModel {
	void resolveDependencies(UnbakedModel.Resolver resolver);

	BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState);

	@Environment(EnvType.CLIENT)
	public interface Resolver {
		UnbakedModel resolve(ResourceLocation resourceLocation);
	}
}
