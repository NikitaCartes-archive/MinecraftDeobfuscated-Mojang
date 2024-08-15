package net.minecraft.client.resources.model;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public interface UnbakedModel {
	void resolveDependencies(UnbakedModel.Resolver resolver, UnbakedModel.ResolutionContext resolutionContext);

	@Nullable
	BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState);

	@Environment(EnvType.CLIENT)
	public static enum ResolutionContext {
		TOP,
		OVERRIDE;
	}

	@Environment(EnvType.CLIENT)
	public interface Resolver {
		UnbakedModel resolve(ResourceLocation resourceLocation);

		UnbakedModel resolveForOverride(ResourceLocation resourceLocation);
	}
}
