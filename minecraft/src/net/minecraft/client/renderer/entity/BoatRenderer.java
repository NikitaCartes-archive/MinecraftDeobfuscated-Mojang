package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class BoatRenderer extends EntityRenderer<Boat, BoatRenderState> {
	private final Map<Boat.Type, Pair<ResourceLocation, EntityModel<BoatRenderState>>> boatResources;
	private final Model waterPatchModel;

	public BoatRenderer(EntityRendererProvider.Context context, boolean bl) {
		super(context);
		this.shadowRadius = 0.8F;
		this.boatResources = (Map<Boat.Type, Pair<ResourceLocation, EntityModel<BoatRenderState>>>)Stream.of(Boat.Type.values())
			.collect(ImmutableMap.toImmutableMap(type -> type, type -> Pair.of(getTextureLocation(type, bl), this.createBoatModel(context, type, bl))));
		this.waterPatchModel = new Model.Simple(context.bakeLayer(ModelLayers.BOAT_WATER_PATCH), resourceLocation -> RenderType.waterMask());
	}

	private EntityModel<BoatRenderState> createBoatModel(EntityRendererProvider.Context context, Boat.Type type, boolean bl) {
		ModelLayerLocation modelLayerLocation = bl ? ModelLayers.createChestBoatModelName(type) : ModelLayers.createBoatModelName(type);
		ModelPart modelPart = context.bakeLayer(modelLayerLocation);

		return (EntityModel<BoatRenderState>)(switch (type) {
			case BAMBOO -> new RaftModel(modelPart);
			default -> new BoatModel(modelPart);
		});
	}

	private static ResourceLocation getTextureLocation(Boat.Type type, boolean bl) {
		return bl
			? ResourceLocation.withDefaultNamespace("textures/entity/chest_boat/" + type.getName() + ".png")
			: ResourceLocation.withDefaultNamespace("textures/entity/boat/" + type.getName() + ".png");
	}

	public void render(BoatRenderState boatRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.375F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - boatRenderState.yRot));
		float f = boatRenderState.hurtTime;
		if (f > 0.0F) {
			poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(f) * f * boatRenderState.damageTime / 10.0F * (float)boatRenderState.hurtDir));
		}

		if (!Mth.equal(boatRenderState.bubbleAngle, 0.0F)) {
			poseStack.mulPose(new Quaternionf().setAngleAxis(boatRenderState.bubbleAngle * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
		}

		Pair<ResourceLocation, EntityModel<BoatRenderState>> pair = (Pair<ResourceLocation, EntityModel<BoatRenderState>>)this.boatResources
			.get(boatRenderState.variant);
		ResourceLocation resourceLocation = pair.getFirst();
		EntityModel<BoatRenderState> entityModel = pair.getSecond();
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
		entityModel.setupAnim(boatRenderState);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(entityModel.renderType(resourceLocation));
		entityModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
		if (!boatRenderState.isUnderWater && boatRenderState.variant != Boat.Type.BAMBOO) {
			this.waterPatchModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(this.waterPatchModel.renderType(resourceLocation)), i, OverlayTexture.NO_OVERLAY);
		}

		poseStack.popPose();
		super.render(boatRenderState, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(BoatRenderState boatRenderState) {
		return (ResourceLocation)((Pair)this.boatResources.get(boatRenderState.variant)).getFirst();
	}

	public BoatRenderState createRenderState() {
		return new BoatRenderState();
	}

	public void extractRenderState(Boat boat, BoatRenderState boatRenderState, float f) {
		super.extractRenderState(boat, boatRenderState, f);
		boatRenderState.yRot = boat.getYRot(f);
		boatRenderState.hurtTime = (float)boat.getHurtTime() - f;
		boatRenderState.hurtDir = boat.getHurtDir();
		boatRenderState.damageTime = Math.max(boat.getDamage() - f, 0.0F);
		boatRenderState.bubbleAngle = boat.getBubbleAngle(f);
		boatRenderState.isUnderWater = boat.isUnderWater();
		boatRenderState.variant = boat.getVariant();
		boatRenderState.rowingTimeLeft = boat.getRowingTime(0, f);
		boatRenderState.rowingTimeRight = boat.getRowingTime(1, f);
	}
}
