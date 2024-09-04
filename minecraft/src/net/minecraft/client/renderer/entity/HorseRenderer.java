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
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;

@Environment(EnvType.CLIENT)
public final class HorseRenderer extends AbstractHorseRenderer<Horse, HorseRenderState, HorseModel> {
	private static final Map<Variant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(Variant.class), enumMap -> {
		enumMap.put(Variant.WHITE, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_white.png"));
		enumMap.put(Variant.CREAMY, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_creamy.png"));
		enumMap.put(Variant.CHESTNUT, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_chestnut.png"));
		enumMap.put(Variant.BROWN, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_brown.png"));
		enumMap.put(Variant.BLACK, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_black.png"));
		enumMap.put(Variant.GRAY, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_gray.png"));
		enumMap.put(Variant.DARK_BROWN, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_darkbrown.png"));
	});

	public HorseRenderer(EntityRendererProvider.Context context) {
		super(context, new HorseModel(context.bakeLayer(ModelLayers.HORSE)), new HorseModel(context.bakeLayer(ModelLayers.HORSE_BABY)), 1.1F);
		this.addLayer(new HorseMarkingLayer(this));
		this.addLayer(new HorseArmorLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
	}

	public ResourceLocation getTextureLocation(HorseRenderState horseRenderState) {
		return (ResourceLocation)LOCATION_BY_VARIANT.get(horseRenderState.variant);
	}

	public HorseRenderState createRenderState() {
		return new HorseRenderState();
	}

	public void extractRenderState(Horse horse, HorseRenderState horseRenderState, float f) {
		super.extractRenderState(horse, horseRenderState, f);
		horseRenderState.variant = horse.getVariant();
		horseRenderState.markings = horse.getMarkings();
		horseRenderState.bodyArmorItem = horse.getBodyArmorItem().copy();
	}
}
