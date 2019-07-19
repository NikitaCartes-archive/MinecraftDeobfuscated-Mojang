package net.minecraft.client.resources.model;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public interface UnbakedModel {
	Collection<ResourceLocation> getDependencies();

	Collection<ResourceLocation> getTextures(Function<ResourceLocation, UnbakedModel> function, Set<String> set);

	@Nullable
	BakedModel bake(ModelBakery modelBakery, Function<ResourceLocation, TextureAtlasSprite> function, ModelState modelState);
}
