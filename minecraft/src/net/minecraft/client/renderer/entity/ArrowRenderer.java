package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;

@Environment(EnvType.CLIENT)
public abstract class ArrowRenderer<T extends AbstractArrow> extends EntityRenderer<T> {
	public ArrowRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(T abstractArrow, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		poseStack.mulPose(Vector3f.YP.rotation(Mth.lerp(h, abstractArrow.yRotO, abstractArrow.yRot) - 90.0F, true));
		poseStack.mulPose(Vector3f.ZP.rotation(Mth.lerp(h, abstractArrow.xRotO, abstractArrow.xRot), true));
		int i = 0;
		float j = 0.0F;
		float k = 0.5F;
		float l = 0.0F;
		float m = 0.15625F;
		float n = 0.0F;
		float o = 0.15625F;
		float p = 0.15625F;
		float q = 0.3125F;
		float r = 0.05625F;
		float s = (float)abstractArrow.shakeTime - h;
		if (s > 0.0F) {
			float t = -Mth.sin(s * 3.0F) * s;
			poseStack.mulPose(Vector3f.ZP.rotation(t, true));
		}

		poseStack.mulPose(Vector3f.XP.rotation(45.0F, true));
		poseStack.scale(0.05625F, 0.05625F, 0.05625F);
		poseStack.translate(-4.0, 0.0, 0.0);
		int u = abstractArrow.getLightColor();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(this.getTextureLocation(abstractArrow)));
		OverlayTexture.setDefault(vertexConsumer);
		Matrix4f matrix4f = poseStack.getPose();
		this.vertex(matrix4f, vertexConsumer, -7, -2, -2, 0.0F, 0.15625F, 1, 0, 0, u);
		this.vertex(matrix4f, vertexConsumer, -7, -2, 2, 0.15625F, 0.15625F, 1, 0, 0, u);
		this.vertex(matrix4f, vertexConsumer, -7, 2, 2, 0.15625F, 0.3125F, 1, 0, 0, u);
		this.vertex(matrix4f, vertexConsumer, -7, 2, -2, 0.0F, 0.3125F, 1, 0, 0, u);
		this.vertex(matrix4f, vertexConsumer, -7, 2, -2, 0.0F, 0.15625F, -1, 0, 0, u);
		this.vertex(matrix4f, vertexConsumer, -7, 2, 2, 0.15625F, 0.15625F, -1, 0, 0, u);
		this.vertex(matrix4f, vertexConsumer, -7, -2, 2, 0.15625F, 0.3125F, -1, 0, 0, u);
		this.vertex(matrix4f, vertexConsumer, -7, -2, -2, 0.0F, 0.3125F, -1, 0, 0, u);

		for (int v = 0; v < 4; v++) {
			poseStack.mulPose(Vector3f.XP.rotation(90.0F, true));
			this.vertex(matrix4f, vertexConsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, u);
			this.vertex(matrix4f, vertexConsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, u);
			this.vertex(matrix4f, vertexConsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, u);
			this.vertex(matrix4f, vertexConsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, u);
		}

		vertexConsumer.unsetDefaultOverlayCoords();
		poseStack.popPose();
		super.render(abstractArrow, d, e, f, g, h, poseStack, multiBufferSource);
	}

	public void vertex(Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, int j, int k, float f, float g, int l, int m, int n, int o) {
		vertexConsumer.vertex(matrix4f, (float)i, (float)j, (float)k).color(255, 255, 255, 255).uv(f, g).uv2(o).normal((float)l, (float)n, (float)m).endVertex();
	}
}
