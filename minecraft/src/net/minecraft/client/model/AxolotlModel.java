package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class AxolotlModel extends EntityModel<AxolotlRenderState> {
	public static final float SWIMMING_LEG_XROT = 1.8849558F;
	public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5F);
	private final ModelPart tail;
	private final ModelPart leftHindLeg;
	private final ModelPart rightHindLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart topGills;
	private final ModelPart leftGills;
	private final ModelPart rightGills;

	public AxolotlModel(ModelPart modelPart) {
		super(modelPart);
		this.body = modelPart.getChild("body");
		this.head = this.body.getChild("head");
		this.rightHindLeg = this.body.getChild("right_hind_leg");
		this.leftHindLeg = this.body.getChild("left_hind_leg");
		this.rightFrontLeg = this.body.getChild("right_front_leg");
		this.leftFrontLeg = this.body.getChild("left_front_leg");
		this.tail = this.body.getChild("tail");
		this.topGills = this.head.getChild("top_gills");
		this.leftGills = this.head.getChild("left_gills");
		this.rightGills = this.head.getChild("right_gills");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(0, 11).addBox(-4.0F, -2.0F, -9.0F, 8.0F, 4.0F, 10.0F).texOffs(2, 17).addBox(0.0F, -3.0F, -8.0F, 0.0F, 5.0F, 9.0F),
			PartPose.offset(0.0F, 20.0F, 5.0F)
		);
		CubeDeformation cubeDeformation = new CubeDeformation(0.001F);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 5.0F, 5.0F, cubeDeformation), PartPose.offset(0.0F, 0.0F, -9.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(3, 37).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 3.0F, 0.0F, cubeDeformation);
		CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(0, 40).addBox(-3.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, cubeDeformation);
		CubeListBuilder cubeListBuilder3 = CubeListBuilder.create().texOffs(11, 40).addBox(0.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, cubeDeformation);
		partDefinition3.addOrReplaceChild("top_gills", cubeListBuilder, PartPose.offset(0.0F, -3.0F, -1.0F));
		partDefinition3.addOrReplaceChild("left_gills", cubeListBuilder2, PartPose.offset(-4.0F, 0.0F, -1.0F));
		partDefinition3.addOrReplaceChild("right_gills", cubeListBuilder3, PartPose.offset(4.0F, 0.0F, -1.0F));
		CubeListBuilder cubeListBuilder4 = CubeListBuilder.create().texOffs(2, 13).addBox(-1.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, cubeDeformation);
		CubeListBuilder cubeListBuilder5 = CubeListBuilder.create().texOffs(2, 13).addBox(-2.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, cubeDeformation);
		partDefinition2.addOrReplaceChild("right_hind_leg", cubeListBuilder5, PartPose.offset(-3.5F, 1.0F, -1.0F));
		partDefinition2.addOrReplaceChild("left_hind_leg", cubeListBuilder4, PartPose.offset(3.5F, 1.0F, -1.0F));
		partDefinition2.addOrReplaceChild("right_front_leg", cubeListBuilder5, PartPose.offset(-3.5F, 1.0F, -8.0F));
		partDefinition2.addOrReplaceChild("left_front_leg", cubeListBuilder4, PartPose.offset(3.5F, 1.0F, -8.0F));
		partDefinition2.addOrReplaceChild(
			"tail", CubeListBuilder.create().texOffs(2, 19).addBox(0.0F, -3.0F, 0.0F, 0.0F, 5.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 1.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void setupAnim(AxolotlRenderState axolotlRenderState) {
		super.setupAnim(axolotlRenderState);
		float f = axolotlRenderState.playingDeadFactor;
		float g = axolotlRenderState.inWaterFactor;
		float h = axolotlRenderState.onGroundFactor;
		float i = axolotlRenderState.movingFactor;
		float j = 1.0F - i;
		float k = 1.0F - Math.min(h, i);
		this.body.yRot = this.body.yRot + axolotlRenderState.yRot * (float) (Math.PI / 180.0);
		this.setupSwimmingAnimation(axolotlRenderState.ageInTicks, axolotlRenderState.xRot, Math.min(i, g));
		this.setupWaterHoveringAnimation(axolotlRenderState.ageInTicks, Math.min(j, g));
		this.setupGroundCrawlingAnimation(axolotlRenderState.ageInTicks, Math.min(i, h));
		this.setupLayStillOnGroundAnimation(axolotlRenderState.ageInTicks, Math.min(j, h));
		this.setupPlayDeadAnimation(f);
		this.applyMirrorLegRotations(k);
	}

	private void setupLayStillOnGroundAnimation(float f, float g) {
		if (!(g <= 1.0E-5F)) {
			float h = f * 0.09F;
			float i = Mth.sin(h);
			float j = Mth.cos(h);
			float k = i * i - 2.0F * i;
			float l = j * j - 3.0F * i;
			this.head.xRot += -0.09F * k * g;
			this.head.zRot += -0.2F * g;
			this.tail.yRot += (-0.1F + 0.1F * k) * g;
			float m = (0.6F + 0.05F * l) * g;
			this.topGills.xRot += m;
			this.leftGills.yRot -= m;
			this.rightGills.yRot += m;
			this.leftHindLeg.xRot += 1.1F * g;
			this.leftHindLeg.yRot += 1.0F * g;
			this.leftFrontLeg.xRot += 0.8F * g;
			this.leftFrontLeg.yRot += 2.3F * g;
			this.leftFrontLeg.zRot -= 0.5F * g;
		}
	}

	private void setupGroundCrawlingAnimation(float f, float g) {
		if (!(g <= 1.0E-5F)) {
			float h = f * 0.11F;
			float i = Mth.cos(h);
			float j = (i * i - 2.0F * i) / 5.0F;
			float k = 0.7F * i;
			float l = 0.09F * i * g;
			this.head.yRot += l;
			this.tail.yRot += l;
			float m = (0.6F - 0.08F * (i * i + 2.0F * Mth.sin(h))) * g;
			this.topGills.xRot += m;
			this.leftGills.yRot -= m;
			this.rightGills.yRot += m;
			float n = 0.9424779F * g;
			float o = 1.0995574F * g;
			this.leftHindLeg.xRot += n;
			this.leftHindLeg.yRot += (1.5F - j) * g;
			this.leftHindLeg.zRot += -0.1F * g;
			this.leftFrontLeg.xRot += o;
			this.leftFrontLeg.yRot += ((float) (Math.PI / 2) - k) * g;
			this.rightHindLeg.xRot += n;
			this.rightHindLeg.yRot += (-1.0F - j) * g;
			this.rightFrontLeg.xRot += o;
			this.rightFrontLeg.yRot += ((float) (-Math.PI / 2) - k) * g;
		}
	}

	private void setupWaterHoveringAnimation(float f, float g) {
		if (!(g <= 1.0E-5F)) {
			float h = f * 0.075F;
			float i = Mth.cos(h);
			float j = Mth.sin(h) * 0.15F;
			float k = (-0.15F + 0.075F * i) * g;
			this.body.xRot += k;
			this.body.y -= j * g;
			this.head.xRot -= k;
			this.topGills.xRot += 0.2F * i * g;
			float l = (-0.3F * i - 0.19F) * g;
			this.leftGills.yRot += l;
			this.rightGills.yRot -= l;
			this.leftHindLeg.xRot += ((float) (Math.PI * 3.0 / 4.0) - i * 0.11F) * g;
			this.leftHindLeg.yRot += 0.47123894F * g;
			this.leftHindLeg.zRot += 1.7278761F * g;
			this.leftFrontLeg.xRot += ((float) (Math.PI / 4) - i * 0.2F) * g;
			this.leftFrontLeg.yRot += 2.042035F * g;
			this.tail.yRot += 0.5F * i * g;
		}
	}

	private void setupSwimmingAnimation(float f, float g, float h) {
		if (!(h <= 1.0E-5F)) {
			float i = f * 0.33F;
			float j = Mth.sin(i);
			float k = Mth.cos(i);
			float l = 0.13F * j;
			this.body.xRot += (g * (float) (Math.PI / 180.0) + l) * h;
			this.head.xRot -= l * 1.8F * h;
			this.body.y -= 0.45F * k * h;
			this.topGills.xRot += (-0.5F * j - 0.8F) * h;
			float m = (0.3F * j + 0.9F) * h;
			this.leftGills.yRot += m;
			this.rightGills.yRot -= m;
			this.tail.yRot = this.tail.yRot + 0.3F * Mth.cos(i * 0.9F) * h;
			this.leftHindLeg.xRot += 1.8849558F * h;
			this.leftHindLeg.yRot += -0.4F * j * h;
			this.leftHindLeg.zRot += (float) (Math.PI / 2) * h;
			this.leftFrontLeg.xRot += 1.8849558F * h;
			this.leftFrontLeg.yRot += (-0.2F * k - 0.1F) * h;
			this.leftFrontLeg.zRot += (float) (Math.PI / 2) * h;
		}
	}

	private void setupPlayDeadAnimation(float f) {
		if (!(f <= 1.0E-5F)) {
			this.leftHindLeg.xRot += 1.4137167F * f;
			this.leftHindLeg.yRot += 1.0995574F * f;
			this.leftHindLeg.zRot += (float) (Math.PI / 4) * f;
			this.leftFrontLeg.xRot += (float) (Math.PI / 4) * f;
			this.leftFrontLeg.yRot += 2.042035F * f;
			this.body.xRot += -0.15F * f;
			this.body.zRot += 0.35F * f;
		}
	}

	private void applyMirrorLegRotations(float f) {
		if (!(f <= 1.0E-5F)) {
			this.rightHindLeg.xRot = this.rightHindLeg.xRot + this.leftHindLeg.xRot * f;
			ModelPart var2 = this.rightHindLeg;
			var2.yRot = var2.yRot + -this.leftHindLeg.yRot * f;
			var2 = this.rightHindLeg;
			var2.zRot = var2.zRot + -this.leftHindLeg.zRot * f;
			this.rightFrontLeg.xRot = this.rightFrontLeg.xRot + this.leftFrontLeg.xRot * f;
			var2 = this.rightFrontLeg;
			var2.yRot = var2.yRot + -this.leftFrontLeg.yRot * f;
			var2 = this.rightFrontLeg;
			var2.zRot = var2.zRot + -this.leftFrontLeg.zRot * f;
		}
	}
}
