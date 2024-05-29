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
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class EnderDragonRenderer extends EntityRenderer<EnderDragon> {
	public static final ResourceLocation CRYSTAL_BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_crystal/end_crystal_beam.png");
	private static final ResourceLocation DRAGON_EXPLODING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_exploding.png");
	private static final ResourceLocation DRAGON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
	private static final ResourceLocation DRAGON_EYES_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_eyes.png");
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
			int l = FastColor.ARGB32.color(Mth.floor(k * 255.0F), -1);
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION));
			this.model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, l);
			VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(DECAL);
			this.model.renderToBuffer(poseStack, vertexConsumer2, i, OverlayTexture.pack(0.0F, bl));
		} else {
			VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(RENDER_TYPE);
			this.model.renderToBuffer(poseStack, vertexConsumer3, i, OverlayTexture.pack(0.0F, bl));
		}

		VertexConsumer vertexConsumer3 = multiBufferSource.getBuffer(EYES);
		this.model.renderToBuffer(poseStack, vertexConsumer3, i, OverlayTexture.NO_OVERLAY);
		if (enderDragon.dragonDeathTime > 0) {
			float m = ((float)enderDragon.dragonDeathTime + g) / 200.0F;
			float n = Math.min(m > 0.8F ? (m - 0.8F) / 0.2F : 0.0F, 1.0F);
			int o = FastColor.ARGB32.colorFromFloat(1.0F - n, 1.0F, 1.0F, 1.0F);
			int p = 16711935;
			RandomSource randomSource = RandomSource.create(432L);
			VertexConsumer vertexConsumer4 = multiBufferSource.getBuffer(RenderType.dragonRays());
			poseStack.pushPose();
			poseStack.translate(0.0F, -1.0F, -2.0F);
			Vector3f vector3f = new Vector3f();
			Vector3f vector3f2 = new Vector3f();
			Vector3f vector3f3 = new Vector3f();
			Vector3f vector3f4 = new Vector3f();
			Quaternionf quaternionf = new Quaternionf();
			int q = Mth.floor((m + m * m) / 2.0F * 60.0F);

			for (int r = 0; r < q; r++) {
				quaternionf.rotationXYZ(
						randomSource.nextFloat() * (float) (Math.PI * 2), randomSource.nextFloat() * (float) (Math.PI * 2), randomSource.nextFloat() * (float) (Math.PI * 2)
					)
					.rotateXYZ(
						randomSource.nextFloat() * (float) (Math.PI * 2),
						randomSource.nextFloat() * (float) (Math.PI * 2),
						randomSource.nextFloat() * (float) (Math.PI * 2) + m * (float) (Math.PI / 2)
					);
				poseStack.mulPose(quaternionf);
				float s = randomSource.nextFloat() * 20.0F + 5.0F + n * 10.0F;
				float t = randomSource.nextFloat() * 2.0F + 1.0F + n * 2.0F;
				vector3f2.set(-HALF_SQRT_3 * t, s, -0.5F * t);
				vector3f3.set(HALF_SQRT_3 * t, s, -0.5F * t);
				vector3f4.set(0.0F, s, t);
				PoseStack.Pose pose = poseStack.last();
				vertexConsumer4.addVertex(pose, vector3f).setColor(o);
				vertexConsumer4.addVertex(pose, vector3f2).setColor(16711935);
				vertexConsumer4.addVertex(pose, vector3f3).setColor(16711935);
				vertexConsumer4.addVertex(pose, vector3f).setColor(o);
				vertexConsumer4.addVertex(pose, vector3f3).setColor(16711935);
				vertexConsumer4.addVertex(pose, vector3f4).setColor(16711935);
				vertexConsumer4.addVertex(pose, vector3f).setColor(o);
				vertexConsumer4.addVertex(pose, vector3f4).setColor(16711935);
				vertexConsumer4.addVertex(pose, vector3f2).setColor(16711935);
			}

			poseStack.popPose();
		}

		poseStack.popPose();
		if (enderDragon.nearestCrystal != null) {
			poseStack.pushPose();
			float m = (float)(enderDragon.nearestCrystal.getX() - Mth.lerp((double)g, enderDragon.xo, enderDragon.getX()));
			float n = (float)(enderDragon.nearestCrystal.getY() - Mth.lerp((double)g, enderDragon.yo, enderDragon.getY()));
			float u = (float)(enderDragon.nearestCrystal.getZ() - Mth.lerp((double)g, enderDragon.zo, enderDragon.getZ()));
			renderCrystalBeams(m, n + EndCrystalRenderer.getY(enderDragon.nearestCrystal, g), u, g, enderDragon.tickCount, poseStack, multiBufferSource, i);
			poseStack.popPose();
		}

		super.render(enderDragon, f, g, poseStack, multiBufferSource, i);
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

		for (int t = 1; t <= 8; t++) {
			float u = Mth.sin((float)t * (float) (Math.PI * 2) / 8.0F) * 0.75F;
			float v = Mth.cos((float)t * (float) (Math.PI * 2) / 8.0F) * 0.75F;
			float w = (float)t / 8.0F;
			vertexConsumer.addVertex(pose, q * 0.2F, r * 0.2F, 0.0F)
				.setColor(-16777216)
				.setUv(s, n)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(k)
				.setNormal(pose, 0.0F, -1.0F, 0.0F);
			vertexConsumer.addVertex(pose, q, r, m).setColor(-1).setUv(s, o).setOverlay(OverlayTexture.NO_OVERLAY).setLight(k).setNormal(pose, 0.0F, -1.0F, 0.0F);
			vertexConsumer.addVertex(pose, u, v, m).setColor(-1).setUv(w, o).setOverlay(OverlayTexture.NO_OVERLAY).setLight(k).setNormal(pose, 0.0F, -1.0F, 0.0F);
			vertexConsumer.addVertex(pose, u * 0.2F, v * 0.2F, 0.0F)
				.setColor(-16777216)
				.setUv(w, n)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(k)
				.setNormal(pose, 0.0F, -1.0F, 0.0F);
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
		public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
			poseStack.pushPose();
			float f = Mth.lerp(this.a, this.entity.oFlapTime, this.entity.flapTime);
			this.jaw.xRot = (float)(Math.sin((double)(f * (float) (Math.PI * 2))) + 1.0) * 0.2F;
			float g = (float)(Math.sin((double)(f * (float) (Math.PI * 2) - 1.0F)) + 1.0);
			g = (g * g + g * 2.0F) * 0.05F;
			poseStack.translate(0.0F, g - 2.0F, -3.0F);
			poseStack.mulPose(Axis.XP.rotationDegrees(g * 2.0F));
			float h = 0.0F;
			float l = 20.0F;
			float m = -12.0F;
			float n = 1.5F;
			double[] ds = this.entity.getLatencyPos(6, this.a);
			float o = Mth.wrapDegrees((float)(this.entity.getLatencyPos(5, this.a)[0] - this.entity.getLatencyPos(10, this.a)[0]));
			float p = Mth.wrapDegrees((float)(this.entity.getLatencyPos(5, this.a)[0] + (double)(o / 2.0F)));
			float q = f * (float) (Math.PI * 2);

			for (int r = 0; r < 5; r++) {
				double[] es = this.entity.getLatencyPos(5 - r, this.a);
				float s = (float)Math.cos((double)((float)r * 0.45F + q)) * 0.15F;
				this.neck.yRot = Mth.wrapDegrees((float)(es[0] - ds[0])) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.xRot = s + this.entity.getHeadPartYOffset(r, ds, es) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
				this.neck.zRot = -Mth.wrapDegrees((float)(es[0] - (double)p)) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.y = l;
				this.neck.z = m;
				this.neck.x = h;
				l += Mth.sin(this.neck.xRot) * 10.0F;
				m -= Mth.cos(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
				h -= Mth.sin(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
				this.neck.render(poseStack, vertexConsumer, i, j, k);
			}

			this.head.y = l;
			this.head.z = m;
			this.head.x = h;
			double[] fs = this.entity.getLatencyPos(0, this.a);
			this.head.yRot = Mth.wrapDegrees((float)(fs[0] - ds[0])) * (float) (Math.PI / 180.0);
			this.head.xRot = Mth.wrapDegrees(this.entity.getHeadPartYOffset(6, ds, fs)) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
			this.head.zRot = -Mth.wrapDegrees((float)(fs[0] - (double)p)) * (float) (Math.PI / 180.0);
			this.head.render(poseStack, vertexConsumer, i, j, k);
			poseStack.pushPose();
			poseStack.translate(0.0F, 1.0F, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(-o * 1.5F));
			poseStack.translate(0.0F, -1.0F, 0.0F);
			this.body.zRot = 0.0F;
			this.body.render(poseStack, vertexConsumer, i, j, k);
			float t = f * (float) (Math.PI * 2);
			this.leftWing.xRot = 0.125F - (float)Math.cos((double)t) * 0.2F;
			this.leftWing.yRot = -0.25F;
			this.leftWing.zRot = -((float)(Math.sin((double)t) + 0.125)) * 0.8F;
			this.leftWingTip.zRot = (float)(Math.sin((double)(t + 2.0F)) + 0.5) * 0.75F;
			this.rightWing.xRot = this.leftWing.xRot;
			this.rightWing.yRot = -this.leftWing.yRot;
			this.rightWing.zRot = -this.leftWing.zRot;
			this.rightWingTip.zRot = -this.leftWingTip.zRot;
			this.renderSide(
				poseStack,
				vertexConsumer,
				i,
				j,
				g,
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
				g,
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
			float s = -Mth.sin(f * (float) (Math.PI * 2)) * 0.0F;
			q = f * (float) (Math.PI * 2);
			l = 10.0F;
			m = 60.0F;
			h = 0.0F;
			ds = this.entity.getLatencyPos(11, this.a);

			for (int u = 0; u < 12; u++) {
				fs = this.entity.getLatencyPos(12 + u, this.a);
				s += Mth.sin((float)u * 0.45F + q) * 0.05F;
				this.neck.yRot = (Mth.wrapDegrees((float)(fs[0] - ds[0])) * 1.5F + 180.0F) * (float) (Math.PI / 180.0);
				this.neck.xRot = s + (float)(fs[1] - ds[1]) * (float) (Math.PI / 180.0) * 1.5F * 5.0F;
				this.neck.zRot = Mth.wrapDegrees((float)(fs[0] - (double)p)) * (float) (Math.PI / 180.0) * 1.5F;
				this.neck.y = l;
				this.neck.z = m;
				this.neck.x = h;
				l += Mth.sin(this.neck.xRot) * 10.0F;
				m -= Mth.cos(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
				h -= Mth.sin(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
				this.neck.render(poseStack, vertexConsumer, i, j, k);
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
			int k
		) {
			modelPart5.xRot = 1.0F + f * 0.1F;
			modelPart6.xRot = 0.5F + f * 0.1F;
			modelPart7.xRot = 0.75F + f * 0.1F;
			modelPart2.xRot = 1.3F + f * 0.1F;
			modelPart3.xRot = -0.5F - f * 0.1F;
			modelPart4.xRot = 0.75F + f * 0.1F;
			modelPart.render(poseStack, vertexConsumer, i, j, k);
			modelPart2.render(poseStack, vertexConsumer, i, j, k);
			modelPart5.render(poseStack, vertexConsumer, i, j, k);
		}
	}
}
