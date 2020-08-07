package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;

@Environment(EnvType.CLIENT)
public class HorseMarkingLayer extends RenderLayer<Horse, HorseModel<Horse>> {
	private static final Map<Markings, ResourceLocation> LOCATION_BY_MARKINGS = Util.make(Maps.newEnumMap(Markings.class), enumMap -> {
		enumMap.put(Markings.NONE, null);
		enumMap.put(Markings.WHITE, new ResourceLocation("textures/entity/horse/horse_markings_white.png"));
		enumMap.put(Markings.WHITE_FIELD, new ResourceLocation("textures/entity/horse/horse_markings_whitefield.png"));
		enumMap.put(Markings.WHITE_DOTS, new ResourceLocation("textures/entity/horse/horse_markings_whitedots.png"));
		enumMap.put(Markings.BLACK_DOTS, new ResourceLocation("textures/entity/horse/horse_markings_blackdots.png"));
	});

	public HorseMarkingLayer(RenderLayerParent<Horse, HorseModel<Horse>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Horse horse, float f, float g, float h, float j, float k, float l) {
		ResourceLocation resourceLocation = (ResourceLocation)LOCATION_BY_MARKINGS.get(horse.getMarkings());
		if (resourceLocation != null && !horse.isInvisible()) {
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityTranslucent(resourceLocation));
			this.getParentModel().renderToBuffer(poseStack, vertexConsumer, i, LivingEntityRenderer.getOverlayCoords(horse, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
