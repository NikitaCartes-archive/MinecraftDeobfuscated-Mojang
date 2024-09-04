package net.minecraft.client.resources.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public interface ModelBaker {
	BakedModel bake(ResourceLocation resourceLocation, ModelState modelState);
}
