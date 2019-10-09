package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class FishingHookRenderer extends EntityRenderer<FishingHook> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");

	public FishingHookRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(FishingHook fishingHook, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		Player player = fishingHook.getOwner();
		if (player != null) {
			poseStack.pushPose();
			poseStack.pushPose();
			poseStack.scale(0.5F, 0.5F, 0.5F);
			float i = 1.0F;
			float j = 0.5F;
			float k = 0.5F;
			poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - this.entityRenderDispatcher.playerRotY));
			float l = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX;
			poseStack.mulPose(Vector3f.XP.rotationDegrees(l));
			Matrix4f matrix4f = poseStack.getPose();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(TEXTURE_LOCATION));
			int m = fishingHook.getLightColor();
			vertexConsumer.vertex(matrix4f, -0.5F, -0.5F, 0.0F)
				.color(255, 255, 255, 255)
				.uv(0.0F, 1.0F)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(m)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
			vertexConsumer.vertex(matrix4f, 0.5F, -0.5F, 0.0F)
				.color(255, 255, 255, 255)
				.uv(1.0F, 1.0F)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(m)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
			vertexConsumer.vertex(matrix4f, 0.5F, 0.5F, 0.0F)
				.color(255, 255, 255, 255)
				.uv(1.0F, 0.0F)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(m)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
			vertexConsumer.vertex(matrix4f, -0.5F, 0.5F, 0.0F)
				.color(255, 255, 255, 255)
				.uv(0.0F, 0.0F)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(m)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
			poseStack.popPose();
			int n = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
			ItemStack itemStack = player.getMainHandItem();
			if (itemStack.getItem() != Items.FISHING_ROD) {
				n = -n;
			}

			float o = player.getAttackAnim(h);
			float p = Mth.sin(Mth.sqrt(o) * (float) Math.PI);
			float q = Mth.lerp(h, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
			double r = (double)Mth.sin(q);
			double s = (double)Mth.cos(q);
			double t = (double)n * 0.35;
			double u = 0.8;
			double v;
			double w;
			double x;
			float y;
			if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.thirdPersonView <= 0) && player == Minecraft.getInstance().player) {
				double z = this.entityRenderDispatcher.options.fov;
				z /= 100.0;
				Vec3 vec3 = new Vec3((double)n * -0.36 * z, -0.045 * z, 0.4);
				vec3 = vec3.xRot(-Mth.lerp(h, player.xRotO, player.xRot) * (float) (Math.PI / 180.0));
				vec3 = vec3.yRot(-Mth.lerp(h, player.yRotO, player.yRot) * (float) (Math.PI / 180.0));
				vec3 = vec3.yRot(p * 0.5F);
				vec3 = vec3.xRot(-p * 0.7F);
				v = Mth.lerp((double)h, player.xo, player.getX()) + vec3.x;
				w = Mth.lerp((double)h, player.yo, player.getY()) + vec3.y;
				x = Mth.lerp((double)h, player.zo, player.getZ()) + vec3.z;
				y = player.getEyeHeight();
			} else {
				v = Mth.lerp((double)h, player.xo, player.getX()) - s * t - r * 0.8;
				w = player.yo + (double)player.getEyeHeight() + (player.getY() - player.yo) * (double)h - 0.45;
				x = Mth.lerp((double)h, player.zo, player.getZ()) - r * t + s * 0.8;
				y = player.isCrouching() ? -0.1875F : 0.0F;
			}

			double z = Mth.lerp((double)h, fishingHook.xo, fishingHook.getX());
			double aa = Mth.lerp((double)h, fishingHook.yo, fishingHook.getY()) + 0.25;
			double ab = Mth.lerp((double)h, fishingHook.zo, fishingHook.getZ());
			float ac = (float)(v - z);
			float ad = (float)(w - aa) + y;
			float ae = (float)(x - ab);
			VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.lines());
			Matrix4f matrix4f2 = poseStack.getPose();
			int af = 16;

			for (int ag = 0; ag < 16; ag++) {
				stringVertex(ac, ad, ae, vertexConsumer2, matrix4f2, (float)(ag / 16));
				stringVertex(ac, ad, ae, vertexConsumer2, matrix4f2, (float)((ag + 1) / 16));
			}

			poseStack.popPose();
			super.render(fishingHook, d, e, f, g, h, poseStack, multiBufferSource);
		}
	}

	private static void stringVertex(float f, float g, float h, VertexConsumer vertexConsumer, Matrix4f matrix4f, float i) {
		vertexConsumer.vertex(matrix4f, f * i, g * (i * i + i) * 0.5F + 0.25F, h * i).color(0, 0, 0, 255).endVertex();
	}

	public ResourceLocation getTextureLocation(FishingHook fishingHook) {
		return TEXTURE_LOCATION;
	}
}
