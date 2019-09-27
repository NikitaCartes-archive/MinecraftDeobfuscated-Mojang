package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

@Environment(EnvType.CLIENT)
public class EndCrystalRenderer extends EntityRenderer<EndCrystal> {
	private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
	public static final float SIN_45 = (float)Math.sin(Math.PI / 4);
	private final ModelPart cube;
	private final ModelPart glass;
	private final ModelPart base;

	public EndCrystalRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.5F;
		this.glass = new ModelPart(64, 32, 0, 0);
		this.glass.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
		this.cube = new ModelPart(64, 32, 32, 0);
		this.cube.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
		this.base = new ModelPart(64, 32, 0, 16);
		this.base.addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F);
	}

	public void render(EndCrystal endCrystal, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		float i = getY(endCrystal, h);
		float j = 0.0625F;
		float k = ((float)endCrystal.time + h) * 3.0F;
		int l = endCrystal.getLightColor();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(this.getTextureLocation(endCrystal)));
		OverlayTexture.setDefault(vertexConsumer);
		poseStack.pushPose();
		poseStack.scale(2.0F, 2.0F, 2.0F);
		poseStack.translate(0.0, -0.5, 0.0);
		if (endCrystal.showsBottom()) {
			this.base.render(poseStack, vertexConsumer, 0.0625F, l, null);
		}

		poseStack.mulPose(Vector3f.YP.rotation(k, true));
		poseStack.translate(0.0, (double)(1.5F + i / 2.0F), 0.0);
		poseStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
		this.glass.render(poseStack, vertexConsumer, 0.0625F, l, null);
		float m = 0.875F;
		poseStack.scale(0.875F, 0.875F, 0.875F);
		poseStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
		poseStack.mulPose(Vector3f.YP.rotation(k, true));
		this.glass.render(poseStack, vertexConsumer, 0.0625F, l, null);
		poseStack.scale(0.875F, 0.875F, 0.875F);
		poseStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
		poseStack.mulPose(Vector3f.YP.rotation(k, true));
		this.cube.render(poseStack, vertexConsumer, 0.0625F, l, null);
		poseStack.popPose();
		poseStack.popPose();
		vertexConsumer.unsetDefaultOverlayCoords();
		BlockPos blockPos = endCrystal.getBeamTarget();
		if (blockPos != null) {
			float n = (float)blockPos.getX() + 0.5F;
			float o = (float)blockPos.getY() + 0.5F;
			float p = (float)blockPos.getZ() + 0.5F;
			float q = (float)((double)n - endCrystal.x);
			float r = (float)((double)o - endCrystal.y);
			float s = (float)((double)p - endCrystal.z);
			poseStack.translate((double)q, (double)r, (double)s);
			EnderDragonRenderer.renderCrystalBeams(-q, -r + i, -s, h, endCrystal.time, poseStack, multiBufferSource, l);
		}

		super.render(endCrystal, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public static float getY(EndCrystal endCrystal, float f) {
		float g = (float)endCrystal.time + f;
		float h = Mth.sin(g * 0.2F) / 2.0F + 0.5F;
		h = (h * h + h) * 0.4F;
		return h - 1.4F;
	}

	public ResourceLocation getTextureLocation(EndCrystal endCrystal) {
		return END_CRYSTAL_LOCATION;
	}

	public boolean shouldRender(EndCrystal endCrystal, Frustum frustum, double d, double e, double f) {
		return super.shouldRender(endCrystal, frustum, d, e, f) || endCrystal.getBeamTarget() != null;
	}
}
