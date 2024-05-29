package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
	protected static final float NAMETAG_SCALE = 0.025F;
	public static final int LEASH_RENDER_STEPS = 24;
	protected final EntityRenderDispatcher entityRenderDispatcher;
	private final Font font;
	protected float shadowRadius;
	protected float shadowStrength = 1.0F;

	protected EntityRenderer(EntityRendererProvider.Context context) {
		this.entityRenderDispatcher = context.getEntityRenderDispatcher();
		this.font = context.getFont();
	}

	public final int getPackedLightCoords(T entity, float f) {
		BlockPos blockPos = BlockPos.containing(entity.getLightProbePosition(f));
		return LightTexture.pack(this.getBlockLightLevel(entity, blockPos), this.getSkyLightLevel(entity, blockPos));
	}

	protected int getSkyLightLevel(T entity, BlockPos blockPos) {
		return entity.level().getBrightness(LightLayer.SKY, blockPos);
	}

	protected int getBlockLightLevel(T entity, BlockPos blockPos) {
		return entity.isOnFire() ? 15 : entity.level().getBrightness(LightLayer.BLOCK, blockPos);
	}

	public boolean shouldRender(T entity, Frustum frustum, double d, double e, double f) {
		if (!entity.shouldRender(d, e, f)) {
			return false;
		} else if (entity.noCulling) {
			return true;
		} else {
			AABB aABB = entity.getBoundingBoxForCulling().inflate(0.5);
			if (aABB.hasNaN() || aABB.getSize() == 0.0) {
				aABB = new AABB(entity.getX() - 2.0, entity.getY() - 2.0, entity.getZ() - 2.0, entity.getX() + 2.0, entity.getY() + 2.0, entity.getZ() + 2.0);
			}

			if (frustum.isVisible(aABB)) {
				return true;
			} else {
				if (entity instanceof Leashable leashable) {
					Entity entity2 = leashable.getLeashHolder();
					if (entity2 != null) {
						return frustum.isVisible(entity2.getBoundingBoxForCulling());
					}
				}

				return false;
			}
		}
	}

	public Vec3 getRenderOffset(T entity, float f) {
		return Vec3.ZERO;
	}

	public void render(T entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (entity instanceof Leashable leashable) {
			Entity entity2 = leashable.getLeashHolder();
			if (entity2 != null) {
				this.renderLeash(entity, g, poseStack, multiBufferSource, entity2);
			}
		}

		if (this.shouldShowName(entity)) {
			this.renderNameTag(entity, entity.getDisplayName(), poseStack, multiBufferSource, i, g);
		}
	}

	private <E extends Entity> void renderLeash(T entity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, E entity2) {
		poseStack.pushPose();
		Vec3 vec3 = entity2.getRopeHoldPosition(f);
		double d = (double)(entity.getPreciseBodyRotation(f) * (float) (Math.PI / 180.0)) + (Math.PI / 2);
		Vec3 vec32 = entity.getLeashOffset(f);
		double e = Math.cos(d) * vec32.z + Math.sin(d) * vec32.x;
		double g = Math.sin(d) * vec32.z - Math.cos(d) * vec32.x;
		double h = Mth.lerp((double)f, entity.xo, entity.getX()) + e;
		double i = Mth.lerp((double)f, entity.yo, entity.getY()) + vec32.y;
		double j = Mth.lerp((double)f, entity.zo, entity.getZ()) + g;
		poseStack.translate(e, vec32.y, g);
		float k = (float)(vec3.x - h);
		float l = (float)(vec3.y - i);
		float m = (float)(vec3.z - j);
		float n = 0.025F;
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.leash());
		Matrix4f matrix4f = poseStack.last().pose();
		float o = Mth.invSqrt(k * k + m * m) * 0.025F / 2.0F;
		float p = m * o;
		float q = k * o;
		BlockPos blockPos = BlockPos.containing(entity.getEyePosition(f));
		BlockPos blockPos2 = BlockPos.containing(entity2.getEyePosition(f));
		int r = this.getBlockLightLevel(entity, blockPos);
		int s = this.entityRenderDispatcher.getRenderer(entity2).getBlockLightLevel(entity2, blockPos2);
		int t = entity.level().getBrightness(LightLayer.SKY, blockPos);
		int u = entity.level().getBrightness(LightLayer.SKY, blockPos2);

		for (int v = 0; v <= 24; v++) {
			addVertexPair(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.025F, p, q, v, false);
		}

		for (int v = 24; v >= 0; v--) {
			addVertexPair(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.0F, p, q, v, true);
		}

		poseStack.popPose();
	}

	private static void addVertexPair(
		VertexConsumer vertexConsumer,
		Matrix4f matrix4f,
		float f,
		float g,
		float h,
		int i,
		int j,
		int k,
		int l,
		float m,
		float n,
		float o,
		float p,
		int q,
		boolean bl
	) {
		float r = (float)q / 24.0F;
		int s = (int)Mth.lerp(r, (float)i, (float)j);
		int t = (int)Mth.lerp(r, (float)k, (float)l);
		int u = LightTexture.pack(s, t);
		float v = q % 2 == (bl ? 1 : 0) ? 0.7F : 1.0F;
		float w = 0.5F * v;
		float x = 0.4F * v;
		float y = 0.3F * v;
		float z = f * r;
		float aa = g > 0.0F ? g * r * r : g - g * (1.0F - r) * (1.0F - r);
		float ab = h * r;
		vertexConsumer.addVertex(matrix4f, z - o, aa + n, ab + p).setColor(w, x, y, 1.0F).setLight(u);
		vertexConsumer.addVertex(matrix4f, z + o, aa + m - n, ab - p).setColor(w, x, y, 1.0F).setLight(u);
	}

	protected boolean shouldShowName(T entity) {
		return entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity;
	}

	public abstract ResourceLocation getTextureLocation(T entity);

	public Font getFont() {
		return this.font;
	}

	protected void renderNameTag(T entity, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f) {
		double d = this.entityRenderDispatcher.distanceToSqr(entity);
		if (!(d > 4096.0)) {
			Vec3 vec3 = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(f));
			if (vec3 != null) {
				boolean bl = !entity.isDiscrete();
				int j = "deadmau5".equals(component.getString()) ? -10 : 0;
				poseStack.pushPose();
				poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
				poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
				poseStack.scale(0.025F, -0.025F, 0.025F);
				Matrix4f matrix4f = poseStack.last().pose();
				float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
				int k = (int)(g * 255.0F) << 24;
				Font font = this.getFont();
				float h = (float)(-font.width(component) / 2);
				font.drawInBatch(component, h, (float)j, 553648127, false, matrix4f, multiBufferSource, bl ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, k, i);
				if (bl) {
					font.drawInBatch(component, h, (float)j, -1, false, matrix4f, multiBufferSource, Font.DisplayMode.NORMAL, 0, i);
				}

				poseStack.popPose();
			}
		}
	}

	protected float getShadowRadius(T entity) {
		return this.shadowRadius;
	}
}
