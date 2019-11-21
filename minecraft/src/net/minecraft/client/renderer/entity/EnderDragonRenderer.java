package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Environment(EnvType.CLIENT)
public class EnderDragonRenderer extends EntityRenderer<EnderDragon> {
	public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
	private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
	private static final ResourceLocation DRAGON_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon.png");
	private static final ResourceLocation DRAGON_EYES_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");
	private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(DRAGON_LOCATION);
	private static final RenderType DECAL = RenderType.entityDecal(DRAGON_LOCATION);
	private static final RenderType EYES = RenderType.eyes(DRAGON_EYES_LOCATION);
	private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);
	private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
	private final EnderDragonRenderer.DragonModel model = new EnderDragonRenderer.DragonModel();

	public EnderDragonRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.5F;
	}

	public void render(EnderDragon enderDragon, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		float h = (float)enderDragon.getLatencyPos(7, g)[0];
		float j = (float)(enderDragon.getLatencyPos(5, g)[1] - enderDragon.getLatencyPos(10, g)[1]);
		poseStack.mulPose(Vector3f.YP.rotationDegrees(-h));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(j * 10.0F));
		poseStack.translate(0.0, 0.0, 1.0);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0, -1.501F, 0.0);
		boolean bl = enderDragon.hurtTime > 0;
		this.model.prepareMobModel(enderDragon, 0.0F, 0.0F, g);
		if (enderDragon.dragonDeathTime > 0) {
			float k = (float)enderDragon.dragonDeathTime / 200.0F;
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityAlpha(DRAGON_EXPLODING_LOCATION, k));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(DECAL);
			this.model.renderToBuffer(poseStack, vertexConsumer2, i, OverlayTexture.pack(0.0F, bl), 1.0F, 1.0F, 1.0F, 1.0F);
		} else {
			VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(RENDER_TYPE);
			this.model.renderToBuffer(poseStack, vertexConsumer3, i, OverlayTexture.pack(0.0F, bl), 1.0F, 1.0F, 1.0F, 1.0F);
		}

		VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(EYES);
		this.model.renderToBuffer(poseStack, vertexConsumer3, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		if (enderDragon.dragonDeathTime > 0) {
			float l = ((float)enderDragon.dragonDeathTime + g) / 200.0F;
			float m = 0.0F;
			if (l > 0.8F) {
				m = (l - 0.8F) / 0.2F;
			}

			Random random = new Random(432L);
			VertexConsumer vertexConsumer4 = multiBufferSource.getBuffer(RenderType.lightning());
			poseStack.pushPose();
			poseStack.translate(0.0, -1.0, -2.0);

			for (int n = 0; (float)n < (l + l * l) / 2.0F * 60.0F; n++) {
				poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
				poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F));
				poseStack.mulPose(Vector3f.XP.rotationDegrees(random.nextFloat() * 360.0F));
				poseStack.mulPose(Vector3f.YP.rotationDegrees(random.nextFloat() * 360.0F));
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(random.nextFloat() * 360.0F + l * 90.0F));
				float o = random.nextFloat() * 20.0F + 5.0F + m * 10.0F;
				float p = random.nextFloat() * 2.0F + 1.0F + m * 2.0F;
				Matrix4f matrix4f = poseStack.last().pose();
				int q = (int)(255.0F * (1.0F - m));
				vertex01(vertexConsumer4, matrix4f, q);
				vertex2(vertexConsumer4, matrix4f, o, p);
				vertex3(vertexConsumer4, matrix4f, o, p);
				vertex01(vertexConsumer4, matrix4f, q);
				vertex3(vertexConsumer4, matrix4f, o, p);
				vertex4(vertexConsumer4, matrix4f, o, p);
				vertex01(vertexConsumer4, matrix4f, q);
				vertex4(vertexConsumer4, matrix4f, o, p);
				vertex2(vertexConsumer4, matrix4f, o, p);
			}

			poseStack.popPose();
		}

		poseStack.popPose();
		if (enderDragon.nearestCrystal != null) {
			poseStack.pushPose();
			float l = (float)(enderDragon.nearestCrystal.getX() - Mth.lerp((double)g, enderDragon.xo, enderDragon.getX()));
			float m = (float)(enderDragon.nearestCrystal.getY() - Mth.lerp((double)g, enderDragon.yo, enderDragon.getY()));
			float r = (float)(enderDragon.nearestCrystal.getZ() - Mth.lerp((double)g, enderDragon.zo, enderDragon.getZ()));
			renderCrystalBeams(l, m + EndCrystalRenderer.getY(enderDragon.nearestCrystal, g), r, g, enderDragon.tickCount, poseStack, multiBufferSource, i);
			poseStack.popPose();
		}

		super.render(enderDragon, f, g, poseStack, multiBufferSource, i);
	}

	private static void vertex01(VertexConsumer vertexConsumer, Matrix4f matrix4f, int i) {
		vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F).color(255, 255, 255, i).endVertex();
		vertexConsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F).color(255, 255, 255, i).endVertex();
	}

	private static void vertex2(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g) {
		vertexConsumer.vertex(matrix4f, -HALF_SQRT_3 * g, f, -0.5F * g).color(255, 0, 255, 0).endVertex();
	}

	private static void vertex3(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g) {
		vertexConsumer.vertex(matrix4f, HALF_SQRT_3 * g, f, -0.5F * g).color(255, 0, 255, 0).endVertex();
	}

	private static void vertex4(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g) {
		vertexConsumer.vertex(matrix4f, 0.0F, f, 1.0F * g).color(255, 0, 255, 0).endVertex();
	}

	public static void renderCrystalBeams(float f, float g, float h, float i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource, int k) {
		float l = Mth.sqrt(f * f + h * h);
		float m = Mth.sqrt(f * f + g * g + h * h);
		poseStack.pushPose();
		poseStack.translate(0.0, 2.0, 0.0);
		poseStack.mulPose(Vector3f.YP.rotation((float)(-Math.atan2((double)h, (double)f)) - (float) (Math.PI / 2)));
		poseStack.mulPose(Vector3f.XP.rotation((float)(-Math.atan2((double)l, (double)g)) - (float) (Math.PI / 2)));
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(BEAM);
		float n = 0.0F - ((float)j + i) * 0.01F;
		float o = Mth.sqrt(f * f + g * g + h * h) / 32.0F - ((float)j + i) * 0.01F;
		int p = 8;
		float q = 0.0F;
		float r = 0.75F;
		float s = 0.0F;
		PoseStack.Pose pose = poseStack.last();
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();

		for (int t = 1; t <= 8; t++) {
			float u = Mth.sin((float)(t % 8) * (float) (Math.PI * 2) / 8.0F) * 0.75F;
			float v = Mth.cos((float)(t % 8) * (float) (Math.PI * 2) / 8.0F) * 0.75F;
			float w = (float)(t % 8) / 8.0F;
			vertexConsumer.vertex(matrix4f, q * 0.2F, r * 0.2F, 0.0F)
				.color(0, 0, 0, 255)
				.uv(s, n)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(k)
				.normal(matrix3f, 0.0F, 1.0F, 0.0F)
				.endVertex();
			vertexConsumer.vertex(matrix4f, q, r, m)
				.color(255, 255, 255, 255)
				.uv(s, o)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(k)
				.normal(matrix3f, 0.0F, 1.0F, 0.0F)
				.endVertex();
			vertexConsumer.vertex(matrix4f, u, v, m)
				.color(255, 255, 255, 255)
				.uv(w, o)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(k)
				.normal(matrix3f, 0.0F, 1.0F, 0.0F)
				.endVertex();
			vertexConsumer.vertex(matrix4f, u * 0.2F, v * 0.2F, 0.0F)
				.color(0, 0, 0, 255)
				.uv(w, n)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(k)
				.normal(matrix3f, 0.0F, 1.0F, 0.0F)
				.endVertex();
			q = u;
			r = v;
			s = w;
		}

		poseStack.popPose();
	}

	public ResourceLocation getTextureLocation(EnderDragon enderDragon) {
		return DRAGON_LOCATION;
	}

	@Environment(EnvType.CLIENT)
	public static class DragonModel extends EntityModel<EnderDragon> {
		private final ModelPart head;
		private final ModelPart neck;
		private final ModelPart jaw;
		private final ModelPart body;
		private ModelPart leftWing;
		private ModelPart leftWingTip;
		private ModelPart leftFrontLeg;
		private ModelPart leftFrontLegTip;
		private ModelPart leftFrontFoot;
		private ModelPart leftRearLeg;
		private ModelPart leftRearLegTip;
		private ModelPart leftRearFoot;
		private ModelPart rightWing;
		private ModelPart rightWingTip;
		private ModelPart rightFrontLeg;
		private ModelPart rightFrontLegTip;
		private ModelPart rightFrontFoot;
		private ModelPart rightRearLeg;
		private ModelPart rightRearLegTip;
		private ModelPart rightRearFoot;
		@Nullable
		private EnderDragon entity;
		private float a;

		public DragonModel() {
			this.texWidth = 256;
			this.texHeight = 256;
			float f = -16.0F;
			this.head = new ModelPart(this);
			this.head.addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, 0.0F, 176, 44);
			this.head.addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, 0.0F, 112, 30);
			this.head.mirror = true;
			this.head.addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, 0.0F, 0, 0);
			this.head.addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, 0.0F, 112, 0);
			this.head.mirror = false;
			this.head.addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, 0.0F, 0, 0);
			this.head.addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, 0.0F, 112, 0);
			this.jaw = new ModelPart(this);
			this.jaw.setPos(0.0F, 4.0F, -8.0F);
			this.jaw.addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, 0.0F, 176, 65);
			this.head.addChild(this.jaw);
			this.neck = new ModelPart(this);
			this.neck.addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10, 0.0F, 192, 104);
			this.neck.addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6, 0.0F, 48, 0);
			this.body = new ModelPart(this);
			this.body.setPos(0.0F, 4.0F, 8.0F);
			this.body.addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64, 0.0F, 0, 0);
			this.body.addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12, 0.0F, 220, 53);
			this.body.addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12, 0.0F, 220, 53);
			this.body.addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12, 0.0F, 220, 53);
			this.leftWing = new ModelPart(this);
			this.leftWing.mirror = true;
			this.leftWing.setPos(12.0F, 5.0F, 2.0F);
			this.leftWing.addBox("bone", 0.0F, -4.0F, -4.0F, 56, 8, 8, 0.0F, 112, 88);
			this.leftWing.addBox("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, 0.0F, -56, 88);
			this.leftWingTip = new ModelPart(this);
			this.leftWingTip.mirror = true;
			this.leftWingTip.setPos(56.0F, 0.0F, 0.0F);
			this.leftWingTip.addBox("bone", 0.0F, -2.0F, -2.0F, 56, 4, 4, 0.0F, 112, 136);
			this.leftWingTip.addBox("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, 0.0F, -56, 144);
			this.leftWing.addChild(this.leftWingTip);
			this.leftFrontLeg = new ModelPart(this);
			this.leftFrontLeg.setPos(12.0F, 20.0F, 2.0F);
			this.leftFrontLeg.addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, 0.0F, 112, 104);
			this.leftFrontLegTip = new ModelPart(this);
			this.leftFrontLegTip.setPos(0.0F, 20.0F, -1.0F);
			this.leftFrontLegTip.addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, 0.0F, 226, 138);
			this.leftFrontLeg.addChild(this.leftFrontLegTip);
			this.leftFrontFoot = new ModelPart(this);
			this.leftFrontFoot.setPos(0.0F, 23.0F, 0.0F);
			this.leftFrontFoot.addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, 0.0F, 144, 104);
			this.leftFrontLegTip.addChild(this.leftFrontFoot);
			this.leftRearLeg = new ModelPart(this);
			this.leftRearLeg.setPos(16.0F, 16.0F, 42.0F);
			this.leftRearLeg.addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, 0.0F, 0, 0);
			this.leftRearLegTip = new ModelPart(this);
			this.leftRearLegTip.setPos(0.0F, 32.0F, -4.0F);
			this.leftRearLegTip.addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, 0.0F, 196, 0);
			this.leftRearLeg.addChild(this.leftRearLegTip);
			this.leftRearFoot = new ModelPart(this);
			this.leftRearFoot.setPos(0.0F, 31.0F, 4.0F);
			this.leftRearFoot.addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, 0.0F, 112, 0);
			this.leftRearLegTip.addChild(this.leftRearFoot);
			this.rightWing = new ModelPart(this);
			this.rightWing.setPos(-12.0F, 5.0F, 2.0F);
			this.rightWing.addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8, 0.0F, 112, 88);
			this.rightWing.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, 0.0F, -56, 88);
			this.rightWingTip = new ModelPart(this);
			this.rightWingTip.setPos(-56.0F, 0.0F, 0.0F);
			this.rightWingTip.addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4, 0.0F, 112, 136);
			this.rightWingTip.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, 0.0F, -56, 144);
			this.rightWing.addChild(this.rightWingTip);
			this.rightFrontLeg = new ModelPart(this);
			this.rightFrontLeg.setPos(-12.0F, 20.0F, 2.0F);
			this.rightFrontLeg.addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, 0.0F, 112, 104);
			this.rightFrontLegTip = new ModelPart(this);
			this.rightFrontLegTip.setPos(0.0F, 20.0F, -1.0F);
			this.rightFrontLegTip.addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, 0.0F, 226, 138);
			this.rightFrontLeg.addChild(this.rightFrontLegTip);
			this.rightFrontFoot = new ModelPart(this);
			this.rightFrontFoot.setPos(0.0F, 23.0F, 0.0F);
			this.rightFrontFoot.addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, 0.0F, 144, 104);
			this.rightFrontLegTip.addChild(this.rightFrontFoot);
			this.rightRearLeg = new ModelPart(this);
			this.rightRearLeg.setPos(-16.0F, 16.0F, 42.0F);
			this.rightRearLeg.addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, 0.0F, 0, 0);
			this.rightRearLegTip = new ModelPart(this);
			this.rightRearLegTip.setPos(0.0F, 32.0F, -4.0F);
			this.rightRearLegTip.addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, 0.0F, 196, 0);
			this.rightRearLeg.addChild(this.rightRearLegTip);
			this.rightRearFoot = new ModelPart(this);
			this.rightRearFoot.setPos(0.0F, 31.0F, 4.0F);
			this.rightRearFoot.addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, 0.0F, 112, 0);
			this.rightRearLegTip.addChild(this.rightRearFoot);
		}

		public void prepareMobModel(EnderDragon enderDragon, float f, float g, float h) {
			this.entity = enderDragon;
			this.a = h;
		}

		public void setupAnim(EnderDragon enderDragon, float f, float g, float h, float i, float j) {
		}

		@Override
		public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
			poseStack.pushPose();
			float l = Mth.lerp(this.a, this.entity.oFlapTime, this.entity.flapTime);
			this.jaw.xRot = (float)(Math.sin((double)(l * (float) (Math.PI * 2))) + 1.0) * 0.2F;
			float m = (float)(Math.sin((double)(l * (float) (Math.PI * 2) - 1.0F)) + 1.0);
			m = (m * m + m * 2.0F) * 0.05F;
			poseStack.translate(0.0, (double)(m - 2.0F), -3.0);
			poseStack.mulPose(Vector3f.XP.rotationDegrees(m * 2.0F));
			float n = 0.0F;
			float o = 20.0F;
			float p = -12.0F;
			float q = 1.5F;
			double[] ds = this.entity.getLatencyPos(6, this.a);
			float r = Mth.rotWrap(this.entity.getLatencyPos(5, this.a)[0] - this.entity.getLatencyPos(10, this.a)[0]);
			float s = Mth.rotWrap(this.entity.getLatencyPos(5, this.a)[0] + (double)(r / 2.0F));
			float t = l * (float) (Math.PI * 2);

			for (int u = 0; u < 5; u++) {
				double[] es = this.entity.getLatencyPos(5 - u, this.a);
				float v = (float)Math.cos((double)((float)u * 0.45F + t)) * 0.15F;
				this.neck.yRot = Mth.rotWrap(es[0] - ds[0]) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.xRot = v + this.entity.getHeadPartYOffset(u, ds, es) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
				this.neck.zRot = -Mth.rotWrap(es[0] - (double)s) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.y = o;
				this.neck.z = p;
				this.neck.x = n;
				o = (float)((double)o + Math.sin((double)this.neck.xRot) * 10.0);
				p = (float)((double)p - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
				n = (float)((double)n - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
				this.neck.render(poseStack, vertexConsumer, i, j);
			}

			this.head.y = o;
			this.head.z = p;
			this.head.x = n;
			double[] fs = this.entity.getLatencyPos(0, this.a);
			this.head.yRot = Mth.rotWrap(fs[0] - ds[0]) * (float) (Math.PI / 180.0);
			this.head.xRot = Mth.rotWrap((double)this.entity.getHeadPartYOffset(6, ds, fs)) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
			this.head.zRot = -Mth.rotWrap(fs[0] - (double)s) * (float) (Math.PI / 180.0);
			this.head.render(poseStack, vertexConsumer, i, j);
			poseStack.pushPose();
			poseStack.translate(0.0, 1.0, 0.0);
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(-r * 1.5F));
			poseStack.translate(0.0, -1.0, 0.0);
			this.body.zRot = 0.0F;
			this.body.render(poseStack, vertexConsumer, i, j);
			float w = l * (float) (Math.PI * 2);
			this.leftWing.xRot = 0.125F - (float)Math.cos((double)w) * 0.2F;
			this.leftWing.yRot = -0.25F;
			this.leftWing.zRot = -((float)(Math.sin((double)w) + 0.125)) * 0.8F;
			this.leftWingTip.zRot = (float)(Math.sin((double)(w + 2.0F)) + 0.5) * 0.75F;
			this.rightWing.xRot = this.leftWing.xRot;
			this.rightWing.yRot = -this.leftWing.yRot;
			this.rightWing.zRot = -this.leftWing.zRot;
			this.rightWingTip.zRot = -this.leftWingTip.zRot;
			this.renderSide(
				poseStack,
				vertexConsumer,
				i,
				j,
				m,
				this.leftWing,
				this.leftFrontLeg,
				this.leftFrontLegTip,
				this.leftFrontFoot,
				this.leftRearLeg,
				this.leftRearLegTip,
				this.leftRearFoot
			);
			this.renderSide(
				poseStack,
				vertexConsumer,
				i,
				j,
				m,
				this.rightWing,
				this.rightFrontLeg,
				this.rightFrontLegTip,
				this.rightFrontFoot,
				this.rightRearLeg,
				this.rightRearLegTip,
				this.rightRearFoot
			);
			poseStack.popPose();
			float v = -((float)Math.sin((double)(l * (float) (Math.PI * 2)))) * 0.0F;
			t = l * (float) (Math.PI * 2);
			o = 10.0F;
			p = 60.0F;
			n = 0.0F;
			ds = this.entity.getLatencyPos(11, this.a);

			for (int x = 0; x < 12; x++) {
				fs = this.entity.getLatencyPos(12 + x, this.a);
				v = (float)((double)v + Math.sin((double)((float)x * 0.45F + t)) * 0.05F);
				this.neck.yRot = (Mth.rotWrap(fs[0] - ds[0]) * 1.5F + 180.0F) * (float) (Math.PI / 180.0);
				this.neck.xRot = v + (float)(fs[1] - ds[1]) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
				this.neck.zRot = Mth.rotWrap(fs[0] - (double)s) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.y = o;
				this.neck.z = p;
				this.neck.x = n;
				o = (float)((double)o + Math.sin((double)this.neck.xRot) * 10.0);
				p = (float)((double)p - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
				n = (float)((double)n - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
				this.neck.render(poseStack, vertexConsumer, i, j);
			}

			poseStack.popPose();
		}

		private void renderSide(
			PoseStack poseStack,
			VertexConsumer vertexConsumer,
			int i,
			int j,
			float f,
			ModelPart modelPart,
			ModelPart modelPart2,
			ModelPart modelPart3,
			ModelPart modelPart4,
			ModelPart modelPart5,
			ModelPart modelPart6,
			ModelPart modelPart7
		) {
			modelPart5.xRot = 1.0F + f * 0.1F;
			modelPart6.xRot = 0.5F + f * 0.1F;
			modelPart7.xRot = 0.75F + f * 0.1F;
			modelPart2.xRot = 1.3F + f * 0.1F;
			modelPart3.xRot = -0.5F - f * 0.1F;
			modelPart4.xRot = 0.75F + f * 0.1F;
			modelPart.render(poseStack, vertexConsumer, i, j);
			modelPart2.render(poseStack, vertexConsumer, i, j);
			modelPart5.render(poseStack, vertexConsumer, i, j);
		}
	}
}
