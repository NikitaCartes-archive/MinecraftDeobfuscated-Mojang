package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class GuardianRenderer extends MobRenderer<Guardian, GuardianRenderState, GuardianModel> {
	private static final ResourceLocation GUARDIAN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/guardian.png");
	private static final ResourceLocation GUARDIAN_BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/guardian_beam.png");
	private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(GUARDIAN_BEAM_LOCATION);

	public GuardianRenderer(EntityRendererProvider.Context context) {
		this(context, 0.5F, ModelLayers.GUARDIAN);
	}

	protected GuardianRenderer(EntityRendererProvider.Context context, float f, ModelLayerLocation modelLayerLocation) {
		super(context, new GuardianModel(context.bakeLayer(modelLayerLocation)), f);
	}

	public boolean shouldRender(Guardian guardian, Frustum frustum, double d, double e, double f) {
		if (super.shouldRender(guardian, frustum, d, e, f)) {
			return true;
		} else {
			if (guardian.hasActiveAttackTarget()) {
				LivingEntity livingEntity = guardian.getActiveAttackTarget();
				if (livingEntity != null) {
					Vec3 vec3 = this.getPosition(livingEntity, (double)livingEntity.getBbHeight() * 0.5, 1.0F);
					Vec3 vec32 = this.getPosition(guardian, (double)guardian.getEyeHeight(), 1.0F);
					return frustum.isVisible(new AABB(vec32.x, vec32.y, vec32.z, vec3.x, vec3.y, vec3.z));
				}
			}

			return false;
		}
	}

	private Vec3 getPosition(LivingEntity livingEntity, double d, float f) {
		double e = Mth.lerp((double)f, livingEntity.xOld, livingEntity.getX());
		double g = Mth.lerp((double)f, livingEntity.yOld, livingEntity.getY()) + d;
		double h = Mth.lerp((double)f, livingEntity.zOld, livingEntity.getZ());
		return new Vec3(e, g, h);
	}

	public void render(GuardianRenderState guardianRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		super.render(guardianRenderState, poseStack, multiBufferSource, i);
		Vec3 vec3 = guardianRenderState.attackTargetPosition;
		if (vec3 != null) {
			float f = guardianRenderState.attackTime * 0.5F % 1.0F;
			poseStack.pushPose();
			poseStack.translate(0.0F, guardianRenderState.eyeHeight, 0.0F);
			renderBeam(
				poseStack,
				multiBufferSource.getBuffer(BEAM_RENDER_TYPE),
				vec3.subtract(guardianRenderState.eyePosition),
				guardianRenderState.attackTime,
				guardianRenderState.scale,
				f
			);
			poseStack.popPose();
		}
	}

	private static void renderBeam(PoseStack poseStack, VertexConsumer vertexConsumer, Vec3 vec3, float f, float g, float h) {
		float i = (float)(vec3.length() + 1.0);
		vec3 = vec3.normalize();
		float j = (float)Math.acos(vec3.y);
		float k = (float) (Math.PI / 2) - (float)Math.atan2(vec3.z, vec3.x);
		poseStack.mulPose(Axis.YP.rotationDegrees(k * (180.0F / (float)Math.PI)));
		poseStack.mulPose(Axis.XP.rotationDegrees(j * (180.0F / (float)Math.PI)));
		float l = f * 0.05F * -1.5F;
		float m = g * g;
		int n = 64 + (int)(m * 191.0F);
		int o = 32 + (int)(m * 191.0F);
		int p = 128 - (int)(m * 64.0F);
		float q = 0.2F;
		float r = 0.282F;
		float s = Mth.cos(l + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
		float t = Mth.sin(l + (float) (Math.PI * 3.0 / 4.0)) * 0.282F;
		float u = Mth.cos(l + (float) (Math.PI / 4)) * 0.282F;
		float v = Mth.sin(l + (float) (Math.PI / 4)) * 0.282F;
		float w = Mth.cos(l + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
		float x = Mth.sin(l + ((float) Math.PI * 5.0F / 4.0F)) * 0.282F;
		float y = Mth.cos(l + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
		float z = Mth.sin(l + ((float) Math.PI * 7.0F / 4.0F)) * 0.282F;
		float aa = Mth.cos(l + (float) Math.PI) * 0.2F;
		float ab = Mth.sin(l + (float) Math.PI) * 0.2F;
		float ac = Mth.cos(l + 0.0F) * 0.2F;
		float ad = Mth.sin(l + 0.0F) * 0.2F;
		float ae = Mth.cos(l + (float) (Math.PI / 2)) * 0.2F;
		float af = Mth.sin(l + (float) (Math.PI / 2)) * 0.2F;
		float ag = Mth.cos(l + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
		float ah = Mth.sin(l + (float) (Math.PI * 3.0 / 2.0)) * 0.2F;
		float aj = 0.0F;
		float ak = 0.4999F;
		float al = -1.0F + h;
		float am = al + i * 2.5F;
		PoseStack.Pose pose = poseStack.last();
		vertex(vertexConsumer, pose, aa, i, ab, n, o, p, 0.4999F, am);
		vertex(vertexConsumer, pose, aa, 0.0F, ab, n, o, p, 0.4999F, al);
		vertex(vertexConsumer, pose, ac, 0.0F, ad, n, o, p, 0.0F, al);
		vertex(vertexConsumer, pose, ac, i, ad, n, o, p, 0.0F, am);
		vertex(vertexConsumer, pose, ae, i, af, n, o, p, 0.4999F, am);
		vertex(vertexConsumer, pose, ae, 0.0F, af, n, o, p, 0.4999F, al);
		vertex(vertexConsumer, pose, ag, 0.0F, ah, n, o, p, 0.0F, al);
		vertex(vertexConsumer, pose, ag, i, ah, n, o, p, 0.0F, am);
		float an = Mth.floor(f) % 2 == 0 ? 0.5F : 0.0F;
		vertex(vertexConsumer, pose, s, i, t, n, o, p, 0.5F, an + 0.5F);
		vertex(vertexConsumer, pose, u, i, v, n, o, p, 1.0F, an + 0.5F);
		vertex(vertexConsumer, pose, y, i, z, n, o, p, 1.0F, an);
		vertex(vertexConsumer, pose, w, i, x, n, o, p, 0.5F, an);
	}

	private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f, float g, float h, int i, int j, int k, float l, float m) {
		vertexConsumer.addVertex(pose, f, g, h)
			.setColor(i, j, k, 255)
			.setUv(l, m)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(15728880)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	public ResourceLocation getTextureLocation(GuardianRenderState guardianRenderState) {
		return GUARDIAN_LOCATION;
	}

	public GuardianRenderState createRenderState() {
		return new GuardianRenderState();
	}

	public void extractRenderState(Guardian guardian, GuardianRenderState guardianRenderState, float f) {
		super.extractRenderState(guardian, guardianRenderState, f);
		guardianRenderState.spikesAnimation = guardian.getSpikesAnimation(f);
		guardianRenderState.tailAnimation = guardian.getTailAnimation(f);
		guardianRenderState.eyePosition = guardian.getEyePosition(f);
		Entity entity = getEntityToLookAt(guardian);
		if (entity != null) {
			guardianRenderState.lookDirection = guardian.getViewVector(f);
			guardianRenderState.lookAtPosition = entity.getEyePosition(f);
		} else {
			guardianRenderState.lookDirection = null;
			guardianRenderState.lookAtPosition = null;
		}

		LivingEntity livingEntity = guardian.getActiveAttackTarget();
		if (livingEntity != null) {
			guardianRenderState.attackScale = guardian.getAttackAnimationScale(f);
			guardianRenderState.attackTime = guardian.getClientSideAttackTime() + f;
			guardianRenderState.attackTargetPosition = this.getPosition(livingEntity, (double)livingEntity.getBbHeight() * 0.5, f);
		} else {
			guardianRenderState.attackTargetPosition = null;
		}
	}

	@Nullable
	private static Entity getEntityToLookAt(Guardian guardian) {
		Entity entity = Minecraft.getInstance().getCameraEntity();
		return (Entity)(guardian.hasActiveAttackTarget() ? guardian.getActiveAttackTarget() : entity);
	}
}
