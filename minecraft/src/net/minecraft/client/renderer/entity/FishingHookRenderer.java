package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class FishingHookRenderer extends EntityRenderer<FishingHook> {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
	private static final double VIEW_BOBBING_SCALE = 960.0;

	public FishingHookRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public void render(FishingHook fishingHook, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		Player player = fishingHook.getPlayerOwner();
		if (player != null) {
			poseStack.pushPose();
			poseStack.pushPose();
			poseStack.scale(0.5F, 0.5F, 0.5F);
			poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			PoseStack.Pose pose = poseStack.last();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
			vertex(vertexConsumer, pose, i, 0.0F, 0, 0, 1);
			vertex(vertexConsumer, pose, i, 1.0F, 0, 1, 1);
			vertex(vertexConsumer, pose, i, 1.0F, 1, 1, 0);
			vertex(vertexConsumer, pose, i, 0.0F, 1, 0, 0);
			poseStack.popPose();
			Vec3 vec3 = getPlayerHandPos(player, g, Items.FISHING_ROD, this.entityRenderDispatcher);
			double d = Mth.lerp((double)g, fishingHook.xo, fishingHook.getX());
			double e = Mth.lerp((double)g, fishingHook.yo, fishingHook.getY()) + 0.25;
			double h = Mth.lerp((double)g, fishingHook.zo, fishingHook.getZ());
			float j = (float)(vec3.x - d);
			float k = (float)(vec3.y - e);
			float l = (float)(vec3.z - h);
			VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.lineStrip());
			PoseStack.Pose pose2 = poseStack.last();
			int m = 16;

			for (int n = 0; n <= 16; n++) {
				stringVertex(j, k, l, vertexConsumer2, pose2, fraction(n, 16), fraction(n + 1, 16));
			}

			poseStack.popPose();
			super.render(fishingHook, f, g, poseStack, multiBufferSource, i);
		}
	}

	public static Vec3 getPlayerHandPos(Player player, float f, Item item, EntityRenderDispatcher entityRenderDispatcher) {
		int i = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
		ItemStack itemStack = player.getMainHandItem();
		if (!itemStack.is(item)) {
			i = -i;
		}

		float g = player.getAttackAnim(f);
		float h = Mth.sin(Mth.sqrt(g) * (float) Math.PI);
		float j = Mth.lerp(f, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
		double d = (double)Mth.sin(j);
		double e = (double)Mth.cos(j);
		double k = (double)i * 0.35;
		double l = 0.8;
		if ((entityRenderDispatcher.options == null || entityRenderDispatcher.options.getCameraType().isFirstPerson()) && player == Minecraft.getInstance().player) {
			double n = 960.0 / (double)entityRenderDispatcher.options.fov().get().intValue();
			Vec3 vec3 = entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float)i * 0.525F, -0.1F);
			vec3 = vec3.scale(n);
			vec3 = vec3.yRot(h * 0.5F);
			vec3 = vec3.xRot(-h * 0.7F);
			return new Vec3(
				Mth.lerp((double)f, player.xo, player.getX()) + vec3.x,
				Mth.lerp((double)f, player.yo, player.getY()) + vec3.y + (double)player.getEyeHeight(),
				Mth.lerp((double)f, player.zo, player.getZ()) + vec3.z
			);
		} else {
			float m = player.isCrouching() ? -0.1875F : 0.0F;
			return new Vec3(
				Mth.lerp((double)f, player.xo, player.getX()) - e * k - d * 0.8,
				player.yo + (double)player.getEyeHeight() + (player.getY() - player.yo) * (double)f - 0.45 + (double)m,
				Mth.lerp((double)f, player.zo, player.getZ()) - d * k + e * 0.8
			);
		}
	}

	private static float fraction(int i, int j) {
		return (float)i / (float)j;
	}

	private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, int i, float f, int j, int k, int l) {
		vertexConsumer.vertex(pose, f - 0.5F, (float)j - 0.5F, 0.0F)
			.color(255, 255, 255, 255)
			.uv((float)k, (float)l)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(i)
			.normal(pose, 0.0F, 1.0F, 0.0F)
			.endVertex();
	}

	private static void stringVertex(float f, float g, float h, VertexConsumer vertexConsumer, PoseStack.Pose pose, float i, float j) {
		float k = f * i;
		float l = g * (i * i + i) * 0.5F + 0.25F;
		float m = h * i;
		float n = f * j - k;
		float o = g * (j * j + j) * 0.5F + 0.25F - l;
		float p = h * j - m;
		float q = Mth.sqrt(n * n + o * o + p * p);
		n /= q;
		o /= q;
		p /= q;
		vertexConsumer.vertex(pose, k, l, m).color(0, 0, 0, 255).normal(pose, n, o, p).endVertex();
	}

	public ResourceLocation getTextureLocation(FishingHook fishingHook) {
		return TEXTURE_LOCATION;
	}
}
