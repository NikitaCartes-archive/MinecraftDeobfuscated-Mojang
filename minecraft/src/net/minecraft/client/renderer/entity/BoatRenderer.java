package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

@Environment(EnvType.CLIENT)
public class BoatRenderer extends EntityRenderer<Boat> {
	private final Map<Boat.Type, Pair<ResourceLocation, BoatModel>> boatResources;

	public BoatRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.8F;
		this.boatResources = (Map<Boat.Type, Pair<ResourceLocation, BoatModel>>)Stream.of(Boat.Type.values())
			.collect(
				ImmutableMap.toImmutableMap(
					type -> type,
					type -> Pair.of(
							new ResourceLocation("textures/entity/boat/" + type.getName() + ".png"), new BoatModel(context.bakeLayer(ModelLayers.createBoatModelName(type)))
						)
				)
			);
	}

	public void render(Boat boat, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.translate(0.0, 0.375, 0.0);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - f));
		float h = (float)boat.getHurtTime() - g;
		float j = boat.getDamage() - g;
		if (j < 0.0F) {
			j = 0.0F;
		}

		if (h > 0.0F) {
			poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(h) * h * j / 10.0F * (float)boat.getHurtDir()));
		}

		float k = boat.getBubbleAngle(g);
		if (!Mth.equal(k, 0.0F)) {
			poseStack.mulPose(new Quaternion(new Vector3f(1.0F, 0.0F, 1.0F), boat.getBubbleAngle(g), true));
		}

		Pair<ResourceLocation, BoatModel> pair = (Pair<ResourceLocation, BoatModel>)this.boatResources.get(boat.getBoatType());
		ResourceLocation resourceLocation = pair.getFirst();
		BoatModel boatModel = pair.getSecond();
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
		boatModel.setupAnim(boat, g, 0.0F, -0.1F, 0.0F, 0.0F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(boatModel.renderType(resourceLocation));
		boatModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		if (!boat.isUnderWater()) {
			VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.waterMask());
			boatModel.waterPatch().render(poseStack, vertexConsumer2, i, OverlayTexture.NO_OVERLAY);
		}

		poseStack.popPose();
		super.render(boat, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(Boat boat) {
		return (ResourceLocation)((Pair)this.boatResources.get(boat.getBoatType())).getFirst();
	}
}
