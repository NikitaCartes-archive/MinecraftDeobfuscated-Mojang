package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

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
	private final EnderDragonRenderer.DragonModel model;

	public EnderDragonRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
		this.model = new EnderDragonRenderer.DragonModel(context.bakeLayer(ModelLayers.ENDER_DRAGON));
	}

	public void render(EnderDragon enderDragon, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		float h = (float)enderDragon.getLatencyPos(7, g)[0];
		float j = (float)(enderDragon.getLatencyPos(5, g)[1] - enderDragon.getLatencyPos(10, g)[1]);
		poseStack.mulPose(Axis.YP.rotationDegrees(-h));
		poseStack.mulPose(Axis.XP.rotationDegrees(j * 10.0F));
		poseStack.translate(0.0F, 0.0F, 1.0F);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0F, -1.501F, 0.0F);
		boolean bl = enderDragon.hurtTime > 0;
		this.model.prepareMobModel(enderDragon, 0.0F, 0.0F, g);
		if (enderDragon.dragonDeathTime > 0) {
			float k = (float)enderDragon.dragonDeathTime / 200.0F;
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, k);
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
			float m = Math.min(l > 0.8F ? (l - 0.8F) / 0.2F : 0.0F, 1.0F);
			RandomSource randomSource = RandomSource.create(432L);
			VertexConsumer vertexConsumer4 = multiBufferSource.getBuffer(RenderType.lightning());
			poseStack.pushPose();
			poseStack.translate(0.0F, -1.0F, -2.0F);

			for (int n = 0; (float)n < (l + l * l) / 2.0F * 60.0F; n++) {
				poseStack.mulPose(Axis.XP.rotationDegrees(randomSource.nextFloat() * 360.0F));
				poseStack.mulPose(Axis.YP.rotationDegrees(randomSource.nextFloat() * 360.0F));
				poseStack.mulPose(Axis.ZP.rotationDegrees(randomSource.nextFloat() * 360.0F));
				poseStack.mulPose(Axis.XP.rotationDegrees(randomSource.nextFloat() * 360.0F));
				poseStack.mulPose(Axis.YP.rotationDegrees(randomSource.nextFloat() * 360.0F));
				poseStack.mulPose(Axis.ZP.rotationDegrees(randomSource.nextFloat() * 360.0F + l * 90.0F));
				float o = randomSource.nextFloat() * 20.0F + 5.0F + m * 10.0F;
				float p = randomSource.nextFloat() * 2.0F + 1.0F + m * 2.0F;
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
		poseStack.translate(0.0F, 2.0F, 0.0F);
		poseStack.mulPose(Axis.YP.rotation((float)(-Math.atan2((double)h, (double)f)) - (float) (Math.PI / 2)));
		poseStack.mulPose(Axis.XP.rotation((float)(-Math.atan2((double)l, (double)g)) - (float) (Math.PI / 2)));
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
			float u = Mth.sin((float)t * (float) (Math.PI * 2) / 8.0F) * 0.75F;
			float v = Mth.cos((float)t * (float) (Math.PI * 2) / 8.0F) * 0.75F;
			float w = (float)t / 8.0F;
			vertexConsumer.vertex(matrix4f, q * 0.2F, r * 0.2F, 0.0F)
				.color(0, 0, 0, 255)
				.uv(s, n)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(k)
				.normal(matrix3f, 0.0F, -1.0F, 0.0F)
				.endVertex();
			vertexConsumer.vertex(matrix4f, q, r, m)
				.color(255, 255, 255, 255)
				.uv(s, o)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(k)
				.normal(matrix3f, 0.0F, -1.0F, 0.0F)
				.endVertex();
			vertexConsumer.vertex(matrix4f, u, v, m)
				.color(255, 255, 255, 255)
				.uv(w, o)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(k)
				.normal(matrix3f, 0.0F, -1.0F, 0.0F)
				.endVertex();
			vertexConsumer.vertex(matrix4f, u * 0.2F, v * 0.2F, 0.0F)
				.color(0, 0, 0, 255)
				.uv(w, n)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(k)
				.normal(matrix3f, 0.0F, -1.0F, 0.0F)
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

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		float f = -16.0F;
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, 176, 44)
				.addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, 112, 30)
				.mirror()
				.addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0)
				.addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0)
				.mirror()
				.addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0)
				.addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0),
			PartPose.ZERO
		);
		partDefinition2.addOrReplaceChild("jaw", CubeListBuilder.create().addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, 176, 65), PartPose.offset(0.0F, 4.0F, -8.0F));
		partDefinition.addOrReplaceChild(
			"neck",
			CubeListBuilder.create().addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10, 192, 104).addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6, 48, 0),
			PartPose.ZERO
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create()
				.addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64, 0, 0)
				.addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12, 220, 53)
				.addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12, 220, 53)
				.addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12, 220, 53),
			PartPose.offset(0.0F, 4.0F, 8.0F)
		);
		PartDefinition partDefinition3 = partDefinition.addOrReplaceChild(
			"left_wing",
			CubeListBuilder.create().mirror().addBox("bone", 0.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88).addBox("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88),
			PartPose.offset(12.0F, 5.0F, 2.0F)
		);
		partDefinition3.addOrReplaceChild(
			"left_wing_tip",
			CubeListBuilder.create().mirror().addBox("bone", 0.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136).addBox("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144),
			PartPose.offset(56.0F, 0.0F, 0.0F)
		);
		PartDefinition partDefinition4 = partDefinition.addOrReplaceChild(
			"left_front_leg", CubeListBuilder.create().addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, 112, 104), PartPose.offset(12.0F, 20.0F, 2.0F)
		);
		PartDefinition partDefinition5 = partDefinition4.addOrReplaceChild(
			"left_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, 226, 138), PartPose.offset(0.0F, 20.0F, -1.0F)
		);
		partDefinition5.addOrReplaceChild(
			"left_front_foot", CubeListBuilder.create().addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, 144, 104), PartPose.offset(0.0F, 23.0F, 0.0F)
		);
		PartDefinition partDefinition6 = partDefinition.addOrReplaceChild(
			"left_hind_leg", CubeListBuilder.create().addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, 0, 0), PartPose.offset(16.0F, 16.0F, 42.0F)
		);
		PartDefinition partDefinition7 = partDefinition6.addOrReplaceChild(
			"left_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, 196, 0), PartPose.offset(0.0F, 32.0F, -4.0F)
		);
		partDefinition7.addOrReplaceChild(
			"left_hind_foot", CubeListBuilder.create().addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, 112, 0), PartPose.offset(0.0F, 31.0F, 4.0F)
		);
		PartDefinition partDefinition8 = partDefinition.addOrReplaceChild(
			"right_wing",
			CubeListBuilder.create().addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88).addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88),
			PartPose.offset(-12.0F, 5.0F, 2.0F)
		);
		partDefinition8.addOrReplaceChild(
			"right_wing_tip",
			CubeListBuilder.create().addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136).addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144),
			PartPose.offset(-56.0F, 0.0F, 0.0F)
		);
		PartDefinition partDefinition9 = partDefinition.addOrReplaceChild(
			"right_front_leg", CubeListBuilder.create().addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, 112, 104), PartPose.offset(-12.0F, 20.0F, 2.0F)
		);
		PartDefinition partDefinition10 = partDefinition9.addOrReplaceChild(
			"right_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, 226, 138), PartPose.offset(0.0F, 20.0F, -1.0F)
		);
		partDefinition10.addOrReplaceChild(
			"right_front_foot", CubeListBuilder.create().addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, 144, 104), PartPose.offset(0.0F, 23.0F, 0.0F)
		);
		PartDefinition partDefinition11 = partDefinition.addOrReplaceChild(
			"right_hind_leg", CubeListBuilder.create().addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, 0, 0), PartPose.offset(-16.0F, 16.0F, 42.0F)
		);
		PartDefinition partDefinition12 = partDefinition11.addOrReplaceChild(
			"right_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, 196, 0), PartPose.offset(0.0F, 32.0F, -4.0F)
		);
		partDefinition12.addOrReplaceChild(
			"right_hind_foot", CubeListBuilder.create().addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, 112, 0), PartPose.offset(0.0F, 31.0F, 4.0F)
		);
		return LayerDefinition.create(meshDefinition, 256, 256);
	}

	@Environment(EnvType.CLIENT)
	public static class DragonModel extends EntityModel<EnderDragon> {
		private final ModelPart head;
		private final ModelPart neck;
		private final ModelPart jaw;
		private final ModelPart body;
		private final ModelPart leftWing;
		private final ModelPart leftWingTip;
		private final ModelPart leftFrontLeg;
		private final ModelPart leftFrontLegTip;
		private final ModelPart leftFrontFoot;
		private final ModelPart leftRearLeg;
		private final ModelPart leftRearLegTip;
		private final ModelPart leftRearFoot;
		private final ModelPart rightWing;
		private final ModelPart rightWingTip;
		private final ModelPart rightFrontLeg;
		private final ModelPart rightFrontLegTip;
		private final ModelPart rightFrontFoot;
		private final ModelPart rightRearLeg;
		private final ModelPart rightRearLegTip;
		private final ModelPart rightRearFoot;
		@Nullable
		private EnderDragon entity;
		private float a;

		public DragonModel(ModelPart modelPart) {
			this.head = modelPart.getChild("head");
			this.jaw = this.head.getChild("jaw");
			this.neck = modelPart.getChild("neck");
			this.body = modelPart.getChild("body");
			this.leftWing = modelPart.getChild("left_wing");
			this.leftWingTip = this.leftWing.getChild("left_wing_tip");
			this.leftFrontLeg = modelPart.getChild("left_front_leg");
			this.leftFrontLegTip = this.leftFrontLeg.getChild("left_front_leg_tip");
			this.leftFrontFoot = this.leftFrontLegTip.getChild("left_front_foot");
			this.leftRearLeg = modelPart.getChild("left_hind_leg");
			this.leftRearLegTip = this.leftRearLeg.getChild("left_hind_leg_tip");
			this.leftRearFoot = this.leftRearLegTip.getChild("left_hind_foot");
			this.rightWing = modelPart.getChild("right_wing");
			this.rightWingTip = this.rightWing.getChild("right_wing_tip");
			this.rightFrontLeg = modelPart.getChild("right_front_leg");
			this.rightFrontLegTip = this.rightFrontLeg.getChild("right_front_leg_tip");
			this.rightFrontFoot = this.rightFrontLegTip.getChild("right_front_foot");
			this.rightRearLeg = modelPart.getChild("right_hind_leg");
			this.rightRearLegTip = this.rightRearLeg.getChild("right_hind_leg_tip");
			this.rightRearFoot = this.rightRearLegTip.getChild("right_hind_foot");
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
			poseStack.translate(0.0F, m - 2.0F, -3.0F);
			poseStack.mulPose(Axis.XP.rotationDegrees(m * 2.0F));
			float n = 0.0F;
			float o = 20.0F;
			float p = -12.0F;
			float q = 1.5F;
			double[] ds = this.entity.getLatencyPos(6, this.a);
			float r = Mth.wrapDegrees((float)(this.entity.getLatencyPos(5, this.a)[0] - this.entity.getLatencyPos(10, this.a)[0]));
			float s = Mth.wrapDegrees((float)(this.entity.getLatencyPos(5, this.a)[0] + (double)(r / 2.0F)));
			float t = l * (float) (Math.PI * 2);

			for (int u = 0; u < 5; u++) {
				double[] es = this.entity.getLatencyPos(5 - u, this.a);
				float v = (float)Math.cos((double)((float)u * 0.45F + t)) * 0.15F;
				this.neck.yRot = Mth.wrapDegrees((float)(es[0] - ds[0])) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.xRot = v + this.entity.getHeadPartYOffset(u, ds, es) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
				this.neck.zRot = -Mth.wrapDegrees((float)(es[0] - (double)s)) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.y = o;
				this.neck.z = p;
				this.neck.x = n;
				o += Mth.sin(this.neck.xRot) * 10.0F;
				p -= Mth.cos(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
				n -= Mth.sin(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
				this.neck.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, k);
			}

			this.head.y = o;
			this.head.z = p;
			this.head.x = n;
			double[] fs = this.entity.getLatencyPos(0, this.a);
			this.head.yRot = Mth.wrapDegrees((float)(fs[0] - ds[0])) * (float) (Math.PI / 180.0);
			this.head.xRot = Mth.wrapDegrees(this.entity.getHeadPartYOffset(6, ds, fs)) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
			this.head.zRot = -Mth.wrapDegrees((float)(fs[0] - (double)s)) * (float) (Math.PI / 180.0);
			this.head.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, k);
			poseStack.pushPose();
			poseStack.translate(0.0F, 1.0F, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(-r * 1.5F));
			poseStack.translate(0.0F, -1.0F, 0.0F);
			this.body.zRot = 0.0F;
			this.body.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, k);
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
				this.leftRearFoot,
				k
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
				this.rightRearFoot,
				k
			);
			poseStack.popPose();
			float v = -Mth.sin(l * (float) (Math.PI * 2)) * 0.0F;
			t = l * (float) (Math.PI * 2);
			o = 10.0F;
			p = 60.0F;
			n = 0.0F;
			ds = this.entity.getLatencyPos(11, this.a);

			for (int x = 0; x < 12; x++) {
				fs = this.entity.getLatencyPos(12 + x, this.a);
				v += Mth.sin((float)x * 0.45F + t) * 0.05F;
				this.neck.yRot = (Mth.wrapDegrees((float)(fs[0] - ds[0])) * 1.5F + 180.0F) * (float) (Math.PI / 180.0);
				this.neck.xRot = v + (float)(fs[1] - ds[1]) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
				this.neck.zRot = Mth.wrapDegrees((float)(fs[0] - (double)s)) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.y = o;
				this.neck.z = p;
				this.neck.x = n;
				o += Mth.sin(this.neck.xRot) * 10.0F;
				p -= Mth.cos(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
				n -= Mth.sin(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
				this.neck.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, k);
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
			ModelPart modelPart7,
			float g
		) {
			modelPart5.xRot = 1.0F + f * 0.1F;
			modelPart6.xRot = 0.5F + f * 0.1F;
			modelPart7.xRot = 0.75F + f * 0.1F;
			modelPart2.xRot = 1.3F + f * 0.1F;
			modelPart3.xRot = -0.5F - f * 0.1F;
			modelPart4.xRot = 0.75F + f * 0.1F;
			modelPart.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, g);
			modelPart2.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, g);
			modelPart5.render(poseStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, g);
		}
	}
}
