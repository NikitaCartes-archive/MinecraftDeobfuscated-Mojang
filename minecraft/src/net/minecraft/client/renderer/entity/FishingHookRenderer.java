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
			poseStack.mulPose(Vector3f.YP.rotation(180.0F - this.entityRenderDispatcher.playerRotY, true));
			poseStack.mulPose(
				Vector3f.XP.rotation((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, true)
			);
			Matrix4f matrix4f = poseStack.getPose();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(TEXTURE_LOCATION));
			OverlayTexture.setDefault(vertexConsumer);
			int l = fishingHook.getLightColor();
			vertexConsumer.vertex(matrix4f, -0.5F, -0.5F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(l).normal(0.0F, 1.0F, 0.0F).endVertex();
			vertexConsumer.vertex(matrix4f, 0.5F, -0.5F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(l).normal(0.0F, 1.0F, 0.0F).endVertex();
			vertexConsumer.vertex(matrix4f, 0.5F, 0.5F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(l).normal(0.0F, 1.0F, 0.0F).endVertex();
			vertexConsumer.vertex(matrix4f, -0.5F, 0.5F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(l).normal(0.0F, 1.0F, 0.0F).endVertex();
			poseStack.popPose();
			vertexConsumer.unsetDefaultOverlayCoords();
			int m = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
			ItemStack itemStack = player.getMainHandItem();
			if (itemStack.getItem() != Items.FISHING_ROD) {
				m = -m;
			}

			float n = player.getAttackAnim(h);
			float o = Mth.sin(Mth.sqrt(n) * (float) Math.PI);
			float p = Mth.lerp(h, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
			double q = (double)Mth.sin(p);
			double r = (double)Mth.cos(p);
			double s = (double)m * 0.35;
			double t = 0.8;
			double u;
			double v;
			double w;
			float x;
			if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.thirdPersonView <= 0) && player == Minecraft.getInstance().player) {
				double y = this.entityRenderDispatcher.options.fov;
				y /= 100.0;
				Vec3 vec3 = new Vec3((double)m * -0.36 * y, -0.045 * y, 0.4);
				vec3 = vec3.xRot(-Mth.lerp(h, player.xRotO, player.xRot) * (float) (Math.PI / 180.0));
				vec3 = vec3.yRot(-Mth.lerp(h, player.yRotO, player.yRot) * (float) (Math.PI / 180.0));
				vec3 = vec3.yRot(o * 0.5F);
				vec3 = vec3.xRot(-o * 0.7F);
				u = Mth.lerp((double)h, player.xo, player.x) + vec3.x;
				v = Mth.lerp((double)h, player.yo, player.y) + vec3.y;
				w = Mth.lerp((double)h, player.zo, player.z) + vec3.z;
				x = player.getEyeHeight();
			} else {
				u = Mth.lerp((double)h, player.xo, player.x) - r * s - q * 0.8;
				v = player.yo + (double)player.getEyeHeight() + (player.y - player.yo) * (double)h - 0.45;
				w = Mth.lerp((double)h, player.zo, player.z) - q * s + r * 0.8;
				x = player.isCrouching() ? -0.1875F : 0.0F;
			}

			double y = Mth.lerp((double)h, fishingHook.xo, fishingHook.x);
			double z = Mth.lerp((double)h, fishingHook.yo, fishingHook.y) + 0.25;
			double aa = Mth.lerp((double)h, fishingHook.zo, fishingHook.z);
			float ab = (float)(u - y);
			float ac = (float)(v - z) + x;
			float ad = (float)(w - aa);
			VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.LINES);
			Matrix4f matrix4f2 = poseStack.getPose();
			int ae = 16;

			for (int af = 0; af < 16; af++) {
				stringVertex(ab, ac, ad, vertexConsumer2, matrix4f2, (float)(af / 16));
				stringVertex(ab, ac, ad, vertexConsumer2, matrix4f2, (float)((af + 1) / 16));
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
