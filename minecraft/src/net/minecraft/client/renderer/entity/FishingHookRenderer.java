package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
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
public class FishingHookRenderer extends EntityRenderer<FishingHook, FishingHookRenderState> {
	private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fishing_hook.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);
	private static final double VIEW_BOBBING_SCALE = 960.0;

	public FishingHookRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public boolean shouldRender(FishingHook fishingHook, Frustum frustum, double d, double e, double f) {
		return super.shouldRender(fishingHook, frustum, d, e, f) && fishingHook.getPlayerOwner() != null;
	}

	public void render(FishingHookRenderState fishingHookRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.pushPose();
		poseStack.scale(0.5F, 0.5F, 0.5F);
		poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		PoseStack.Pose pose = poseStack.last();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RENDER_TYPE);
		vertex(vertexConsumer, pose, i, 0.0F, 0, 0, 1);
		vertex(vertexConsumer, pose, i, 1.0F, 0, 1, 1);
		vertex(vertexConsumer, pose, i, 1.0F, 1, 1, 0);
		vertex(vertexConsumer, pose, i, 0.0F, 1, 0, 0);
		poseStack.popPose();
		float f = (float)fishingHookRenderState.lineOriginOffset.x;
		float g = (float)fishingHookRenderState.lineOriginOffset.y;
		float h = (float)fishingHookRenderState.lineOriginOffset.z;
		VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.lineStrip());
		PoseStack.Pose pose2 = poseStack.last();
		int j = 16;

		for (int k = 0; k <= 16; k++) {
			stringVertex(f, g, h, vertexConsumer2, pose2, fraction(k, 16), fraction(k + 1, 16));
		}

		poseStack.popPose();
		super.render(fishingHookRenderState, poseStack, multiBufferSource, i);
	}

	private Vec3 getPlayerHandPos(Player player, float f, float g) {
		int i = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
		ItemStack itemStack = player.getMainHandItem();
		if (!itemStack.is(Items.FISHING_ROD)) {
			i = -i;
		}

		if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
			double n = 960.0 / (double)this.entityRenderDispatcher.options.fov().get().intValue();
			Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float)i * 0.525F, -0.1F).scale(n).yRot(f * 0.5F).xRot(-f * 0.7F);
			return player.getEyePosition(g).add(vec3);
		} else {
			float h = Mth.lerp(g, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
			double d = (double)Mth.sin(h);
			double e = (double)Mth.cos(h);
			float j = player.getScale();
			double k = (double)i * 0.35 * (double)j;
			double l = 0.8 * (double)j;
			float m = player.isCrouching() ? -0.1875F : 0.0F;
			return player.getEyePosition(g).add(-e * k - d * l, (double)m - 0.45 * (double)j, -d * k + e * l);
		}
	}

	private static float fraction(int i, int j) {
		return (float)i / (float)j;
	}

	private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, int i, float f, int j, int k, int l) {
		vertexConsumer.addVertex(pose, f - 0.5F, (float)j - 0.5F, 0.0F)
			.setColor(-1)
			.setUv((float)k, (float)l)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(i)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
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
		vertexConsumer.addVertex(pose, k, l, m).setColor(-16777216).setNormal(pose, n, o, p);
	}

	public FishingHookRenderState createRenderState() {
		return new FishingHookRenderState();
	}

	public void extractRenderState(FishingHook fishingHook, FishingHookRenderState fishingHookRenderState, float f) {
		super.extractRenderState(fishingHook, fishingHookRenderState, f);
		Player player = fishingHook.getPlayerOwner();
		if (player == null) {
			fishingHookRenderState.lineOriginOffset = Vec3.ZERO;
		} else {
			float g = player.getAttackAnim(f);
			float h = Mth.sin(Mth.sqrt(g) * (float) Math.PI);
			Vec3 vec3 = this.getPlayerHandPos(player, h, f);
			Vec3 vec32 = fishingHook.getPosition(f).add(0.0, 0.25, 0.0);
			fishingHookRenderState.lineOriginOffset = vec3.subtract(vec32);
		}
	}

	protected boolean affectedByCulling(FishingHook fishingHook) {
		return false;
	}
}
