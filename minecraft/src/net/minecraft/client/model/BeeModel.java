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
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class BeeModel extends EntityModel<BeeRenderState> {
	public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5F);
	private static final String BONE = "bone";
	private static final String STINGER = "stinger";
	private static final String LEFT_ANTENNA = "left_antenna";
	private static final String RIGHT_ANTENNA = "right_antenna";
	private static final String FRONT_LEGS = "front_legs";
	private static final String MIDDLE_LEGS = "middle_legs";
	private static final String BACK_LEGS = "back_legs";
	private final ModelPart bone;
	private final ModelPart rightWing;
	private final ModelPart leftWing;
	private final ModelPart frontLeg;
	private final ModelPart midLeg;
	private final ModelPart backLeg;
	private final ModelPart stinger;
	private final ModelPart leftAntenna;
	private final ModelPart rightAntenna;
	private float rollAmount;

	public BeeModel(ModelPart modelPart) {
		super(modelPart);
		this.bone = modelPart.getChild("bone");
		ModelPart modelPart2 = this.bone.getChild("body");
		this.stinger = modelPart2.getChild("stinger");
		this.leftAntenna = modelPart2.getChild("left_antenna");
		this.rightAntenna = modelPart2.getChild("right_antenna");
		this.rightWing = this.bone.getChild("right_wing");
		this.leftWing = this.bone.getChild("left_wing");
		this.frontLeg = this.bone.getChild("front_legs");
		this.midLeg = this.bone.getChild("middle_legs");
		this.backLeg = this.bone.getChild("back_legs");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -4.0F, -5.0F, 7.0F, 7.0F, 10.0F), PartPose.ZERO
		);
		partDefinition3.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(26, 7).addBox(0.0F, -1.0F, 5.0F, 0.0F, 1.0F, 2.0F), PartPose.ZERO);
		partDefinition3.addOrReplaceChild(
			"left_antenna", CubeListBuilder.create().texOffs(2, 0).addBox(1.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -2.0F, -5.0F)
		);
		partDefinition3.addOrReplaceChild(
			"right_antenna", CubeListBuilder.create().texOffs(2, 3).addBox(-2.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -2.0F, -5.0F)
		);
		CubeDeformation cubeDeformation = new CubeDeformation(0.001F);
		partDefinition2.addOrReplaceChild(
			"right_wing",
			CubeListBuilder.create().texOffs(0, 18).addBox(-9.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, cubeDeformation),
			PartPose.offsetAndRotation(-1.5F, -4.0F, -3.0F, 0.0F, -0.2618F, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"left_wing",
			CubeListBuilder.create().texOffs(0, 18).mirror().addBox(0.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, cubeDeformation),
			PartPose.offsetAndRotation(1.5F, -4.0F, -3.0F, 0.0F, 0.2618F, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"front_legs", CubeListBuilder.create().addBox("front_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 1), PartPose.offset(1.5F, 3.0F, -2.0F)
		);
		partDefinition2.addOrReplaceChild(
			"middle_legs", CubeListBuilder.create().addBox("middle_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 3), PartPose.offset(1.5F, 3.0F, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"back_legs", CubeListBuilder.create().addBox("back_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 5), PartPose.offset(1.5F, 3.0F, 2.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void setupAnim(BeeRenderState beeRenderState) {
		super.setupAnim(beeRenderState);
		this.rollAmount = beeRenderState.rollAmount;
		this.stinger.visible = beeRenderState.hasStinger;
		if (!beeRenderState.isOnGround) {
			float f = beeRenderState.ageInTicks * 120.32113F * (float) (Math.PI / 180.0);
			this.rightWing.yRot = 0.0F;
			this.rightWing.zRot = Mth.cos(f) * (float) Math.PI * 0.15F;
			this.leftWing.xRot = this.rightWing.xRot;
			this.leftWing.yRot = this.rightWing.yRot;
			this.leftWing.zRot = -this.rightWing.zRot;
			this.frontLeg.xRot = (float) (Math.PI / 4);
			this.midLeg.xRot = (float) (Math.PI / 4);
			this.backLeg.xRot = (float) (Math.PI / 4);
		}

		if (!beeRenderState.isAngry && !beeRenderState.isOnGround) {
			float f = Mth.cos(beeRenderState.ageInTicks * 0.18F);
			this.bone.xRot = 0.1F + f * (float) Math.PI * 0.025F;
			this.leftAntenna.xRot = f * (float) Math.PI * 0.03F;
			this.rightAntenna.xRot = f * (float) Math.PI * 0.03F;
			this.frontLeg.xRot = -f * (float) Math.PI * 0.1F + (float) (Math.PI / 8);
			this.backLeg.xRot = -f * (float) Math.PI * 0.05F + (float) (Math.PI / 4);
			this.bone.y = this.bone.y - Mth.cos(beeRenderState.ageInTicks * 0.18F) * 0.9F;
		}

		if (this.rollAmount > 0.0F) {
			this.bone.xRot = Mth.rotLerpRad(this.rollAmount, this.bone.xRot, 3.0915928F);
		}
	}
}
