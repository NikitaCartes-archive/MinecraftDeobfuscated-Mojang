package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;

@Environment(EnvType.CLIENT)
public class PandaRenderer extends MobRenderer<Panda, PandaModel<Panda>> {
	private static final Map<Panda.Gene, ResourceLocation> TEXTURES = Util.make(Maps.newEnumMap(Panda.Gene.class), enumMap -> {
		enumMap.put(Panda.Gene.NORMAL, ResourceLocation.withDefaultNamespace("textures/entity/panda/panda.png"));
		enumMap.put(Panda.Gene.LAZY, ResourceLocation.withDefaultNamespace("textures/entity/panda/lazy_panda.png"));
		enumMap.put(Panda.Gene.WORRIED, ResourceLocation.withDefaultNamespace("textures/entity/panda/worried_panda.png"));
		enumMap.put(Panda.Gene.PLAYFUL, ResourceLocation.withDefaultNamespace("textures/entity/panda/playful_panda.png"));
		enumMap.put(Panda.Gene.BROWN, ResourceLocation.withDefaultNamespace("textures/entity/panda/brown_panda.png"));
		enumMap.put(Panda.Gene.WEAK, ResourceLocation.withDefaultNamespace("textures/entity/panda/weak_panda.png"));
		enumMap.put(Panda.Gene.AGGRESSIVE, ResourceLocation.withDefaultNamespace("textures/entity/panda/aggressive_panda.png"));
	});

	public PandaRenderer(EntityRendererProvider.Context context) {
		super(context, new PandaModel<>(context.bakeLayer(ModelLayers.PANDA)), 0.9F);
		this.addLayer(new PandaHoldsItemLayer(this, context.getItemInHandRenderer()));
	}

	public ResourceLocation getTextureLocation(Panda panda) {
		return (ResourceLocation)TEXTURES.getOrDefault(panda.getVariant(), (ResourceLocation)TEXTURES.get(Panda.Gene.NORMAL));
	}

	protected void setupRotations(Panda panda, PoseStack poseStack, float f, float g, float h, float i) {
		super.setupRotations(panda, poseStack, f, g, h, i);
		if (panda.rollCounter > 0) {
			int j = panda.rollCounter;
			int k = j + 1;
			float l = 7.0F;
			float m = panda.isBaby() ? 0.3F : 0.8F;
			if (j < 8) {
				float n = (float)(90 * j) / 7.0F;
				float o = (float)(90 * k) / 7.0F;
				float p = this.getAngle(n, o, k, h, 8.0F);
				poseStack.translate(0.0F, (m + 0.2F) * (p / 90.0F), 0.0F);
				poseStack.mulPose(Axis.XP.rotationDegrees(-p));
			} else if (j < 16) {
				float n = ((float)j - 8.0F) / 7.0F;
				float o = 90.0F + 90.0F * n;
				float q = 90.0F + 90.0F * ((float)k - 8.0F) / 7.0F;
				float p = this.getAngle(o, q, k, h, 16.0F);
				poseStack.translate(0.0F, m + 0.2F + (m - 0.2F) * (p - 90.0F) / 90.0F, 0.0F);
				poseStack.mulPose(Axis.XP.rotationDegrees(-p));
			} else if ((float)j < 24.0F) {
				float n = ((float)j - 16.0F) / 7.0F;
				float o = 180.0F + 90.0F * n;
				float q = 180.0F + 90.0F * ((float)k - 16.0F) / 7.0F;
				float p = this.getAngle(o, q, k, h, 24.0F);
				poseStack.translate(0.0F, m + m * (270.0F - p) / 90.0F, 0.0F);
				poseStack.mulPose(Axis.XP.rotationDegrees(-p));
			} else if (j < 32) {
				float n = ((float)j - 24.0F) / 7.0F;
				float o = 270.0F + 90.0F * n;
				float q = 270.0F + 90.0F * ((float)k - 24.0F) / 7.0F;
				float p = this.getAngle(o, q, k, h, 32.0F);
				poseStack.translate(0.0F, m * ((360.0F - p) / 90.0F), 0.0F);
				poseStack.mulPose(Axis.XP.rotationDegrees(-p));
			}
		}

		float r = panda.getSitAmount(h);
		if (r > 0.0F) {
			poseStack.translate(0.0F, 0.8F * r, 0.0F);
			poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(r, panda.getXRot(), panda.getXRot() + 90.0F)));
			poseStack.translate(0.0F, -1.0F * r, 0.0F);
			if (panda.isScared()) {
				float s = (float)(Math.cos((double)panda.tickCount * 1.25) * Math.PI * 0.05F);
				poseStack.mulPose(Axis.YP.rotationDegrees(s));
				if (panda.isBaby()) {
					poseStack.translate(0.0F, 0.8F, 0.55F);
				}
			}
		}

		float s = panda.getLieOnBackAmount(h);
		if (s > 0.0F) {
			float l = panda.isBaby() ? 0.5F : 1.3F;
			poseStack.translate(0.0F, l * s, 0.0F);
			poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(s, panda.getXRot(), panda.getXRot() + 180.0F)));
		}
	}

	private float getAngle(float f, float g, int i, float h, float j) {
		return (float)i < j ? Mth.lerp(h, f, g) : f;
	}
}
