package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.texture.LayeredTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;

@Environment(EnvType.CLIENT)
public final class HorseRenderer extends AbstractHorseRenderer<Horse, HorseModel<Horse>> {
	private static final Map<String, ResourceLocation> LAYERED_LOCATION_CACHE = Maps.<String, ResourceLocation>newHashMap();

	public HorseRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new HorseModel<>(0.0F), 1.1F);
		this.addLayer(new HorseArmorLayer(this));
	}

	public ResourceLocation getTextureLocation(Horse horse) {
		String string = horse.getLayeredTextureHashName();
		ResourceLocation resourceLocation = (ResourceLocation)LAYERED_LOCATION_CACHE.get(string);
		if (resourceLocation == null) {
			resourceLocation = new ResourceLocation(string);
			Minecraft.getInstance().getTextureManager().register(resourceLocation, new LayeredTexture(horse.getLayeredTextureLayers()));
			LAYERED_LOCATION_CACHE.put(string, resourceLocation);
		}

		return resourceLocation;
	}
}
