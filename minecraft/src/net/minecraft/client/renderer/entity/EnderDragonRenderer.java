package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
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
	private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
	private final EnderDragonRenderer.DragonModel model = new EnderDragonRenderer.DragonModel(0.0F);

	public EnderDragonRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.5F;
	}

	public void render(EnderDragon enderDragon, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		poseStack.pushPose();
		float i = (float)enderDragon.getLatencyPos(7, h)[0];
		float j = (float)(enderDragon.getLatencyPos(5, h)[1] - enderDragon.getLatencyPos(10, h)[1]);
		poseStack.mulPose(Vector3f.YP.rotation(-i, true));
		poseStack.mulPose(Vector3f.XP.rotation(j * 10.0F, true));
		poseStack.translate(0.0, 0.0, 1.0);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		float k = 0.0625F;
		poseStack.translate(0.0, -1.501F, 0.0);
		boolean bl = enderDragon.hurtTime > 0;
		int l = enderDragon.getLightColor();
		if (enderDragon.dragonDeathTime > 0) {
			float m = (float)enderDragon.dragonDeathTime / 200.0F;
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(DRAGON_EXPLODING_LOCATION, false, true, true, m, false, true));
			OverlayTexture.setDefault(vertexConsumer);
			this.model.render(poseStack, vertexConsumer, enderDragon, 0.0625F, h, l);
			vertexConsumer.unsetDefaultOverlayCoords();
			VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(DRAGON_LOCATION, false, true, true, 0.1F, true, true));
			vertexConsumer2.defaultOverlayCoords(OverlayTexture.u(0.0F), OverlayTexture.v(bl));
			this.model.render(poseStack, vertexConsumer2, enderDragon, 0.0625F, h, l);
			vertexConsumer2.unsetDefaultOverlayCoords();
		} else {
			VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(DRAGON_LOCATION, false, true, true));
			vertexConsumer3.defaultOverlayCoords(OverlayTexture.u(0.0F), OverlayTexture.v(bl));
			this.model.render(poseStack, vertexConsumer3, enderDragon, 0.0625F, h, l);
			vertexConsumer3.unsetDefaultOverlayCoords();
		}

		VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(RenderType.EYES(DRAGON_EYES_LOCATION));
		OverlayTexture.setDefault(vertexConsumer3);
		this.model.render(poseStack, vertexConsumer3, enderDragon, 0.0625F, h, l);
		vertexConsumer3.unsetDefaultOverlayCoords();
		if (enderDragon.dragonDeathTime > 0) {
			float n = ((float)enderDragon.dragonDeathTime + h) / 200.0F;
			float o = 0.0F;
			if (n > 0.8F) {
				o = (n - 0.8F) / 0.2F;
			}

			Random random = new Random(432L);
			VertexConsumer vertexConsumer4 = multiBufferSource.getBuffer(RenderType.LIGHTNING);
			poseStack.pushPose();
			poseStack.translate(0.0, -1.0, -2.0);

			for (int p = 0; (float)p < (n + n * n) / 2.0F * 60.0F; p++) {
				poseStack.mulPose(Vector3f.XP.rotation(random.nextFloat() * 360.0F, true));
				poseStack.mulPose(Vector3f.YP.rotation(random.nextFloat() * 360.0F, true));
				poseStack.mulPose(Vector3f.ZP.rotation(random.nextFloat() * 360.0F, true));
				poseStack.mulPose(Vector3f.XP.rotation(random.nextFloat() * 360.0F, true));
				poseStack.mulPose(Vector3f.YP.rotation(random.nextFloat() * 360.0F, true));
				poseStack.mulPose(Vector3f.ZP.rotation(random.nextFloat() * 360.0F + n * 90.0F, true));
				float q = random.nextFloat() * 20.0F + 5.0F + o * 10.0F;
				float r = random.nextFloat() * 2.0F + 1.0F + o * 2.0F;
				Matrix4f matrix4f = poseStack.getPose();
				int s = (int)(255.0F * (1.0F - o));
				vertex01(vertexConsumer4, matrix4f, s);
				vertex2(vertexConsumer4, matrix4f, q, r);
				vertex3(vertexConsumer4, matrix4f, q, r);
				vertex01(vertexConsumer4, matrix4f, s);
				vertex3(vertexConsumer4, matrix4f, q, r);
				vertex4(vertexConsumer4, matrix4f, q, r);
				vertex01(vertexConsumer4, matrix4f, s);
				vertex4(vertexConsumer4, matrix4f, q, r);
				vertex2(vertexConsumer4, matrix4f, q, r);
			}

			poseStack.popPose();
		}

		poseStack.popPose();
		if (enderDragon.nearestCrystal != null) {
			poseStack.pushPose();
			float n = (float)(enderDragon.nearestCrystal.x - Mth.lerp((double)h, enderDragon.xo, enderDragon.x));
			float o = (float)(enderDragon.nearestCrystal.y - Mth.lerp((double)h, enderDragon.yo, enderDragon.y));
			float t = (float)(enderDragon.nearestCrystal.z - Mth.lerp((double)h, enderDragon.zo, enderDragon.z));
			renderCrystalBeams(n, o + EndCrystalRenderer.getY(enderDragon.nearestCrystal, h), t, h, enderDragon.tickCount, poseStack, multiBufferSource, l);
			poseStack.popPose();
		}

		super.render(enderDragon, d, e, f, g, h, poseStack, multiBufferSource);
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
		poseStack.mulPose(Vector3f.YP.rotation((float)(-Math.atan2((double)h, (double)f)) - (float) (Math.PI / 2), false));
		poseStack.mulPose(Vector3f.XP.rotation((float)(-Math.atan2((double)l, (double)g)) - (float) (Math.PI / 2), false));
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(CRYSTAL_BEAM_LOCATION, false, true, true));
		OverlayTexture.setDefault(vertexConsumer);
		float n = 0.0F - ((float)j + i) * 0.01F;
		float o = Mth.sqrt(f * f + g * g + h * h) / 32.0F - ((float)j + i) * 0.01F;
		int p = 8;
		float q = 0.0F;
		float r = 0.75F;
		float s = 0.0F;
		Matrix4f matrix4f = poseStack.getPose();

		for (int t = 1; t <= 8; t++) {
			float u = Mth.sin((float)(t % 8) * (float) (Math.PI * 2) / 8.0F) * 0.75F;
			float v = Mth.cos((float)(t % 8) * (float) (Math.PI * 2) / 8.0F) * 0.75F;
			float w = (float)(t % 8) / 8.0F;
			vertexConsumer.vertex(matrix4f, q * 0.2F, r * 0.2F, 0.0F).color(0, 0, 0, 255).uv(s, n).uv2(k).normal(0.0F, 1.0F, 0.0F).endVertex();
			vertexConsumer.vertex(matrix4f, q, r, m).color(255, 255, 255, 255).uv(s, o).uv2(k).normal(0.0F, 1.0F, 0.0F).endVertex();
			vertexConsumer.vertex(matrix4f, u, v, m).color(255, 255, 255, 255).uv(w, o).uv2(k).normal(0.0F, 1.0F, 0.0F).endVertex();
			vertexConsumer.vertex(matrix4f, u * 0.2F, v * 0.2F, 0.0F).color(0, 0, 0, 255).uv(w, n).uv2(k).normal(0.0F, 1.0F, 0.0F).endVertex();
			q = u;
			r = v;
			s = w;
		}

		poseStack.popPose();
		vertexConsumer.unsetDefaultOverlayCoords();
	}

	public ResourceLocation getTextureLocation(EnderDragon enderDragon) {
		return DRAGON_LOCATION;
	}

	@Environment(EnvType.CLIENT)
	public static class DragonModel extends Model {
		private final ModelPart head;
		private final ModelPart neck;
		private final ModelPart jaw;
		private final ModelPart body;
		private final ModelPart rearLeg;
		private final ModelPart frontLeg;
		private final ModelPart rearLegTip;
		private final ModelPart frontLegTip;
		private final ModelPart rearFoot;
		private final ModelPart frontFoot;
		private final ModelPart wing;
		private final ModelPart wingTip;

		public DragonModel(float f) {
			this.texWidth = 256;
			this.texHeight = 256;
			float g = -16.0F;
			this.head = new ModelPart(this);
			this.head.addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, f, 176, 44);
			this.head.addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, f, 112, 30);
			this.head.mirror = true;
			this.head.addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, f, 0, 0);
			this.head.addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, f, 112, 0);
			this.head.mirror = false;
			this.head.addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, f, 0, 0);
			this.head.addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, f, 112, 0);
			this.jaw = new ModelPart(this);
			this.jaw.setPos(0.0F, 4.0F, -8.0F);
			this.jaw.addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, f, 176, 65);
			this.head.addChild(this.jaw);
			this.neck = new ModelPart(this);
			this.neck.addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10, f, 192, 104);
			this.neck.addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6, f, 48, 0);
			this.body = new ModelPart(this);
			this.body.setPos(0.0F, 4.0F, 8.0F);
			this.body.addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64, f, 0, 0);
			this.body.addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12, f, 220, 53);
			this.body.addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12, f, 220, 53);
			this.body.addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12, f, 220, 53);
			this.wing = new ModelPart(this);
			this.wing.setPos(-12.0F, 5.0F, 2.0F);
			this.wing.addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8, f, 112, 88);
			this.wing.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, f, -56, 88);
			this.wingTip = new ModelPart(this);
			this.wingTip.setPos(-56.0F, 0.0F, 0.0F);
			this.wingTip.addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4, f, 112, 136);
			this.wingTip.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, f, -56, 144);
			this.wing.addChild(this.wingTip);
			this.frontLeg = new ModelPart(this);
			this.frontLeg.setPos(-12.0F, 20.0F, 2.0F);
			this.frontLeg.addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, f, 112, 104);
			this.frontLegTip = new ModelPart(this);
			this.frontLegTip.setPos(0.0F, 20.0F, -1.0F);
			this.frontLegTip.addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, f, 226, 138);
			this.frontLeg.addChild(this.frontLegTip);
			this.frontFoot = new ModelPart(this);
			this.frontFoot.setPos(0.0F, 23.0F, 0.0F);
			this.frontFoot.addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, f, 144, 104);
			this.frontLegTip.addChild(this.frontFoot);
			this.rearLeg = new ModelPart(this);
			this.rearLeg.setPos(-16.0F, 16.0F, 42.0F);
			this.rearLeg.addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, f, 0, 0);
			this.rearLegTip = new ModelPart(this);
			this.rearLegTip.setPos(0.0F, 32.0F, -4.0F);
			this.rearLegTip.addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, f, 196, 0);
			this.rearLeg.addChild(this.rearLegTip);
			this.rearFoot = new ModelPart(this);
			this.rearFoot.setPos(0.0F, 31.0F, 4.0F);
			this.rearFoot.addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, f, 112, 0);
			this.rearLegTip.addChild(this.rearFoot);
		}

		public void render(PoseStack poseStack, VertexConsumer vertexConsumer, EnderDragon enderDragon, float f, float g, int i) {
			poseStack.pushPose();
			float h = Mth.lerp(g, enderDragon.oFlapTime, enderDragon.flapTime);
			this.jaw.xRot = (float)(Math.sin((double)(h * (float) (Math.PI * 2))) + 1.0) * 0.2F;
			float j = (float)(Math.sin((double)(h * (float) (Math.PI * 2) - 1.0F)) + 1.0);
			j = (j * j + j * 2.0F) * 0.05F;
			poseStack.translate(0.0, (double)(j - 2.0F), -3.0);
			poseStack.mulPose(Vector3f.XP.rotation(j * 2.0F, true));
			float k = 0.0F;
			float l = 20.0F;
			float m = -12.0F;
			float n = 1.5F;
			double[] ds = enderDragon.getLatencyPos(6, g);
			float o = Mth.rotWrap(enderDragon.getLatencyPos(5, g)[0] - enderDragon.getLatencyPos(10, g)[0]);
			float p = Mth.rotWrap(enderDragon.getLatencyPos(5, g)[0] + (double)(o / 2.0F));
			float q = h * (float) (Math.PI * 2);

			for (int r = 0; r < 5; r++) {
				double[] es = enderDragon.getLatencyPos(5 - r, g);
				float s = (float)Math.cos((double)((float)r * 0.45F + q)) * 0.15F;
				this.neck.yRot = Mth.rotWrap(es[0] - ds[0]) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.xRot = s + enderDragon.getHeadPartYOffset(r, ds, es) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
				this.neck.zRot = -Mth.rotWrap(es[0] - (double)p) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.y = l;
				this.neck.z = m;
				this.neck.x = k;
				l = (float)((double)l + Math.sin((double)this.neck.xRot) * 10.0);
				m = (float)((double)m - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
				k = (float)((double)k - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
				this.neck.render(poseStack, vertexConsumer, f, i, null);
			}

			this.head.y = l;
			this.head.z = m;
			this.head.x = k;
			double[] fs = enderDragon.getLatencyPos(0, g);
			this.head.yRot = Mth.rotWrap(fs[0] - ds[0]) * (float) (Math.PI / 180.0);
			this.head.xRot = Mth.rotWrap((double)enderDragon.getHeadPartYOffset(6, ds, fs)) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
			this.head.zRot = -Mth.rotWrap(fs[0] - (double)p) * (float) (Math.PI / 180.0);
			this.head.render(poseStack, vertexConsumer, f, i, null);
			poseStack.pushPose();
			poseStack.translate(0.0, 1.0, 0.0);
			poseStack.mulPose(Vector3f.ZP.rotation(-o * 1.5F, true));
			poseStack.translate(0.0, -1.0, 0.0);
			this.body.zRot = 0.0F;
			this.body.render(poseStack, vertexConsumer, f, i, null);

			for (int t = 0; t < 2; t++) {
				float s = h * (float) (Math.PI * 2);
				this.wing.xRot = 0.125F - (float)Math.cos((double)s) * 0.2F;
				this.wing.yRot = 0.25F;
				this.wing.zRot = (float)(Math.sin((double)s) + 0.125) * 0.8F;
				this.wingTip.zRot = -((float)(Math.sin((double)(s + 2.0F)) + 0.5)) * 0.75F;
				this.rearLeg.xRot = 1.0F + j * 0.1F;
				this.rearLegTip.xRot = 0.5F + j * 0.1F;
				this.rearFoot.xRot = 0.75F + j * 0.1F;
				this.frontLeg.xRot = 1.3F + j * 0.1F;
				this.frontLegTip.xRot = -0.5F - j * 0.1F;
				this.frontFoot.xRot = 0.75F + j * 0.1F;
				this.wing.render(poseStack, vertexConsumer, f, i, null);
				this.frontLeg.render(poseStack, vertexConsumer, f, i, null);
				this.rearLeg.render(poseStack, vertexConsumer, f, i, null);
				poseStack.scale(-1.0F, 1.0F, 1.0F);
			}

			poseStack.popPose();
			float u = -((float)Math.sin((double)(h * (float) (Math.PI * 2)))) * 0.0F;
			q = h * (float) (Math.PI * 2);
			l = 10.0F;
			m = 60.0F;
			k = 0.0F;
			ds = enderDragon.getLatencyPos(11, g);

			for (int v = 0; v < 12; v++) {
				fs = enderDragon.getLatencyPos(12 + v, g);
				u = (float)((double)u + Math.sin((double)((float)v * 0.45F + q)) * 0.05F);
				this.neck.yRot = (Mth.rotWrap(fs[0] - ds[0]) * 1.5F + 180.0F) * (float) (Math.PI / 180.0);
				this.neck.xRot = u + (float)(fs[1] - ds[1]) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
				this.neck.zRot = Mth.rotWrap(fs[0] - (double)p) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.y = l;
				this.neck.z = m;
				this.neck.x = k;
				l = (float)((double)l + Math.sin((double)this.neck.xRot) * 10.0);
				m = (float)((double)m - Math.cos((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
				k = (float)((double)k - Math.sin((double)this.neck.yRot) * Math.cos((double)this.neck.xRot) * 10.0);
				this.neck.render(poseStack, vertexConsumer, f, i, null);
			}

			poseStack.popPose();
		}
	}
}
