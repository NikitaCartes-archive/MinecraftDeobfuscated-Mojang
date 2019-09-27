package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;

@Environment(EnvType.CLIENT)
public class LeashKnotRenderer extends EntityRenderer<LeashFenceKnotEntity> {
	private static final ResourceLocation KNOT_LOCATION = new ResourceLocation("textures/entity/lead_knot.png");
	private final LeashKnotModel<LeashFenceKnotEntity> model = new LeashKnotModel<>();

	public LeashKnotRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(
		LeashFenceKnotEntity leashFenceKnotEntity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource
	) {
		poseStack.pushPose();
		float i = 0.0625F;
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		int j = leashFenceKnotEntity.getLightColor();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(KNOT_LOCATION));
		OverlayTexture.setDefault(vertexConsumer);
		this.model.setupAnim(leashFenceKnotEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		this.model.renderToBuffer(poseStack, vertexConsumer, j);
		vertexConsumer.unsetDefaultOverlayCoords();
		poseStack.popPose();
		super.render(leashFenceKnotEntity, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public ResourceLocation getTextureLocation(LeashFenceKnotEntity leashFenceKnotEntity) {
		return KNOT_LOCATION;
	}
}
