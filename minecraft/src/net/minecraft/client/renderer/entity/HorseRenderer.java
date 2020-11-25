package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;

@Environment(EnvType.CLIENT)
public final class HorseRenderer extends AbstractHorseRenderer<Horse, HorseModel<Horse>> {
	private static final Map<Variant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(Variant.class), enumMap -> {
		enumMap.put(Variant.WHITE, new ResourceLocation("textures/entity/horse/horse_white.png"));
		enumMap.put(Variant.CREAMY, new ResourceLocation("textures/entity/horse/horse_creamy.png"));
		enumMap.put(Variant.CHESTNUT, new ResourceLocation("textures/entity/horse/horse_chestnut.png"));
		enumMap.put(Variant.BROWN, new ResourceLocation("textures/entity/horse/horse_brown.png"));
		enumMap.put(Variant.BLACK, new ResourceLocation("textures/entity/horse/horse_black.png"));
		enumMap.put(Variant.GRAY, new ResourceLocation("textures/entity/horse/horse_gray.png"));
		enumMap.put(Variant.DARKBROWN, new ResourceLocation("textures/entity/horse/horse_darkbrown.png"));
	});

	public HorseRenderer(EntityRendererProvider.Context context) {
		super(context, new HorseModel<>(context.bakeLayer(ModelLayers.HORSE)), 1.1F);
		this.addLayer(new HorseMarkingLayer(this));
		this.addLayer(new HorseArmorLayer(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(Horse horse) {
		return (ResourceLocation)LOCATION_BY_VARIANT.get(horse.getVariant());
	}
}
