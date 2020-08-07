package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class FishingHookRenderer extends EntityRenderer<FishingHook> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);

	public FishingHookRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(FishingHook fishingHook, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		Player player = fishingHook.getPlayerOwner();
		if (player != null) {
			poseStack.pushPose();
			poseStack.pushPose();
			poseStack.scale(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
			poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
			PoseStack.Pose pose = poseStack.last();
			Matrix4f matrix4f = pose.pose();
			Matrix3f matrix3f = pose.normal();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
			vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 0, 0, 1);
			vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 0, 1, 1);
			vertex(vertexConsumer, matrix4f, matrix3f, i, 1.0F, 1, 1, 0);
			vertex(vertexConsumer, matrix4f, matrix3f, i, 0.0F, 1, 0, 0);
			poseStack.popPose();
			int j = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
			ItemStack itemStack = player.getMainHandItem();
			if (itemStack.getItem() != Items.FISHING_ROD) {
				j = -j;
			}

			float h = player.getAttackAnim(g);
			float k = Mth.sin(Mth.sqrt(h) * (float) Math.PI);
			float l = Mth.lerp(g, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
			double d = (double)Mth.sin(l);
			double e = (double)Mth.cos(l);
			double m = (double)j * 0.35;
			double n = 0.8;
			double o;
			double p;
			double q;
			float r;
			if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson())
				&& player == Minecraft.getInstance().player) {
				double s = this.entityRenderDispatcher.options.fov;
				s /= 100.0;
				Vec3 vec3 = new Vec3((double)j * -0.36 * s, -0.045 * s, 0.4);
				vec3 = vec3.xRot(-Mth.lerp(g, player.xRotO, player.xRot) * (float) (Math.PI / 180.0));
				vec3 = vec3.yRot(-Mth.lerp(g, player.yRotO, player.yRot) * (float) (Math.PI / 180.0));
				vec3 = vec3.yRot(k * 0.5F);
				vec3 = vec3.xRot(-k * 0.7F);
				o = Mth.lerp((double)g, player.xo, player.getX()) + vec3.x;
				p = Mth.lerp((double)g, player.yo, player.getY()) + vec3.y;
				q = Mth.lerp((double)g, player.zo, player.getZ()) + vec3.z;
				r = player.getEyeHeight();
			} else {
				o = Mth.lerp((double)g, player.xo, player.getX()) - e * m - d * 0.8;
				p = player.yo + (double)player.getEyeHeight() + (player.getY() - player.yo) * (double)g - 0.45;
				q = Mth.lerp((double)g, player.zo, player.getZ()) - d * m + e * 0.8;
				r = player.isCrouching() ? -0.1875F : 0.0F;
			}

			double s = Mth.lerp((double)g, fishingHook.xo, fishingHook.getX());
			double t = Mth.lerp((double)g, fishingHook.yo, fishingHook.getY()) + 0.25;
			double u = Mth.lerp((double)g, fishingHook.zo, fishingHook.getZ());
			float v = (float)(o - s);
			float w = (float)(p - t) + r;
			float x = (float)(q - u);
			VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.lines());
			Matrix4f matrix4f2 = poseStack.last().pose();
			int y = 16;

			for (int z = 0; z < 16; z++) {
				stringVertex(v, w, x, vertexConsumer2, matrix4f2, fraction(z, 16));
				stringVertex(v, w, x, vertexConsumer2, matrix4f2, fraction(z + 1, 16));
			}

			poseStack.popPose();
			super.render(fishingHook, f, g, poseStack, multiBufferSource, i);
		}
	}

	private static float fraction(int i, int j) {
		return (float)i / (float)j;
	}

	private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, int i, float f, int j, int k, int l) {
		vertexConsumer.vertex(matrix4f, f - 0.5F, (float)j - 0.5F, 0.0F)
			.color(255, 255, 255, 255)
			.uv((float)k, (float)l)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(i)
			.normal(matrix3f, 0.0F, 1.0F, 0.0F)
			.endVertex();
	}

	private static void stringVertex(float f, float g, float h, VertexConsumer vertexConsumer, Matrix4f matrix4f, float i) {
		vertexConsumer.vertex(matrix4f, f * i, g * (i * i + i) * 0.5F + 0.25F, h * i).color(0, 0, 0, 255).endVertex();
	}

	public ResourceLocation getTextureLocation(FishingHook fishingHook) {
		return TEXTURE_LOCATION;
	}
}
