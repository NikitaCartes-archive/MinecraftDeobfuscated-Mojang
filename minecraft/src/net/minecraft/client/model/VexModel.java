package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Vex;

@Environment(EnvType.CLIENT)
public class VexModel extends HierarchicalModel<Vex> implements ArmedModel {
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightWing;
	private final ModelPart leftWing;

	public VexModel(ModelPart modelPart) {
		super(RenderType::entityTranslucent);
		this.root = modelPart.getChild("root");
		this.body = this.root.getChild("body");
		this.rightArm = this.body.getChild("right_arm");
		this.leftArm = this.body.getChild("left_arm");
		this.rightWing = this.body.getChild("right_wing");
		this.leftWing = this.body.getChild("left_wing");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		partDefinition2.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, 0.0F)
		);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"body",
			CubeListBuilder.create()
				.texOffs(0, 10)
				.addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 16)
				.addBox(-1.5F, 1.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)),
			PartPose.offset(0.0F, 20.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"right_arm",
			CubeListBuilder.create().texOffs(23, 0).addBox(-1.25F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)),
			PartPose.offset(-1.75F, 0.25F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"left_arm",
			CubeListBuilder.create().texOffs(23, 6).addBox(-0.75F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)),
			PartPose.offset(1.75F, 0.25F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"left_wing",
			CubeListBuilder.create().texOffs(16, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false),
			PartPose.offset(0.5F, 1.0F, 1.0F)
		);
		partDefinition3.addOrReplaceChild(
			"right_wing",
			CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-0.5F, 1.0F, 1.0F)
		);
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	public void setupAnim(Vex vex, float f, float g, float h, float i, float j) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.body.xRot = 6.440265F;
		float k = (float) (Math.PI / 5) + Mth.cos(h * 5.5F * (float) (Math.PI / 180.0)) * 0.1F;
		if (vex.isCharging()) {
			this.body.xRot = 0.0F;
			this.rightArm.xRot = (float) (Math.PI * 7.0 / 6.0);
			this.rightArm.yRot = (float) (Math.PI / 12);
			this.rightArm.zRot = -0.47123888F;
		} else {
			this.body.xRot = (float) (Math.PI / 20);
			this.rightArm.xRot = 0.0F;
			this.rightArm.yRot = 0.0F;
			this.rightArm.zRot = k;
		}

		this.leftArm.zRot = -k;
		this.rightWing.y = 1.0F;
		this.leftWing.y = 1.0F;
		this.leftWing.yRot = 1.0995574F + Mth.cos(h * 45.836624F * (float) (Math.PI / 180.0)) * (float) (Math.PI / 180.0) * 16.2F;
		this.rightWing.yRot = -this.leftWing.yRot;
		this.leftWing.xRot = 0.47123888F;
		this.leftWing.zRot = -0.47123888F;
		this.rightWing.xRot = 0.47123888F;
		this.rightWing.zRot = 0.47123888F;
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
		this.offsetSwordPivot(poseStack);
		this.rotateSwordWithArm(poseStack);
		poseStack.scale(0.55F, 0.55F, 0.55F);
		this.offsetSwordPosition(poseStack);
	}

	private void offsetSwordPivot(PoseStack poseStack) {
		poseStack.translate((this.body.x + this.rightArm.x) / 16.0F, (this.body.y + this.rightArm.y) / 16.0F, (this.body.z + this.rightArm.z) / 16.0F);
	}

	private void rotateSwordWithArm(PoseStack poseStack) {
		poseStack.mulPose(Axis.ZP.rotation(this.rightArm.zRot));
		poseStack.mulPose(Axis.YP.rotation(this.rightArm.yRot));
		poseStack.mulPose(Axis.XP.rotation(this.rightArm.xRot));
	}

	private void offsetSwordPosition(PoseStack poseStack) {
		poseStack.translate(0.046875, -0.15625, 0.078125);
	}
}
