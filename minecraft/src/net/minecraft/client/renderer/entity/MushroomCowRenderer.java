package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.entity.layers.MushroomCowMushroomLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MushroomCow;

@Environment(EnvType.CLIENT)
public class MushroomCowRenderer extends MobRenderer<MushroomCow, CowModel<MushroomCow>> {
	private static final Map<MushroomCow.MushroomType, ResourceLocation> TEXTURES = Util.make(
		Maps.<MushroomCow.MushroomType, ResourceLocation>newHashMap(), hashMap -> {
			hashMap.put(MushroomCow.MushroomType.BROWN, new ResourceLocation("textures/entity/cow/brown_mooshroom.png"));
			hashMap.put(MushroomCow.MushroomType.RED, new ResourceLocation("textures/entity/cow/red_mooshroom.png"));
		}
	);

	public MushroomCowRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new CowModel<>(), 0.7F);
		this.addLayer(new MushroomCowMushroomLayer<>(this));
	}

	public ResourceLocation getTextureLocation(MushroomCow mushroomCow) {
		return (ResourceLocation)TEXTURES.get(mushroomCow.getMushroomType());
	}
}
