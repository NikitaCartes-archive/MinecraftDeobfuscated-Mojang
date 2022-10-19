package net.minecraft.client.resources.model;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public interface ModelBaker {
	UnbakedModel getModel(ResourceLocation resourceLocation);

	@Nullable
	BakedModel bake(ResourceLocation resourceLocation, ModelState modelState);
}
