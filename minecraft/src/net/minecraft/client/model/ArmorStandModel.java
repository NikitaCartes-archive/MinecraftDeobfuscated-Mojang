package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.ArmorStand;

@Environment(EnvType.CLIENT)
public class ArmorStandModel extends ArmorStandArmorModel {
	private final ModelPart rightBodyStick;
	private final ModelPart leftBodyStick;
	private final ModelPart shoulderStick;
	private final ModelPart basePlate;

	public ArmorStandModel(ModelPart modelPart) {
		super(modelPart);
		this.rightBodyStick = modelPart.getChild("right_body_stick");
		this.leftBodyStick = modelPart.getChild("left_body_stick");
		this.shoulderStick = modelPart.getChild("shoulder_stick");
		this.basePlate = modelPart.getChild("base_plate");
		this.hat.visible = false;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(0.0F, 1.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 26).addBox(-6.0F, 0.0F, -1.5F, 12.0F, 3.0F, 3.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild(
			"right_arm", CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-5.0F, 2.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_arm", CubeListBuilder.create().texOffs(32, 16).mirror().addBox(0.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(5.0F, 2.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_leg", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(-1.9F, 12.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_leg", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(1.9F, 12.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild("right_body_stick", CubeListBuilder.create().texOffs(16, 0).addBox(-3.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("left_body_stick", CubeListBuilder.create().texOffs(48, 16).addBox(1.0F, 3.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("shoulder_stick", CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, 10.0F, -1.0F, 8.0F, 2.0F, 2.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild(
			"base_plate", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, 11.0F, -6.0F, 12.0F, 1.0F, 12.0F), PartPose.offset(0.0F, 12.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void prepareMobModel(ArmorStand armorStand, float f, float g, float h) {
		this.basePlate.xRot = 0.0F;
		this.basePlate.yRot = (float) (Math.PI / 180.0) * -Mth.rotLerp(h, armorStand.yRotO, armorStand.yRot);
		this.basePlate.zRot = 0.0F;
	}

	@Override
	public void setupAnim(ArmorStand armorStand, float f, float g, float h, float i, float j) {
		super.setupAnim(armorStand, f, g, h, i, j);
		this.leftArm.visible = armorStand.isShowArms();
		this.rightArm.visible = armorStand.isShowArms();
		this.basePlate.visible = !armorStand.isNoBasePlate();
		this.rightBodyStick.xRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getX();
		this.rightBodyStick.yRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getY();
		this.rightBodyStick.zRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getZ();
		this.leftBodyStick.xRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getX();
		this.leftBodyStick.yRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getY();
		this.leftBodyStick.zRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getZ();
		this.shoulderStick.xRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getX();
		this.shoulderStick.yRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getY();
		this.shoulderStick.zRot = (float) (Math.PI / 180.0) * armorStand.getBodyPose().getZ();
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return Iterables.concat(super.bodyParts(), ImmutableList.of(this.rightBodyStick, this.leftBodyStick, this.shoulderStick, this.basePlate));
	}

	@Override
	public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
		ModelPart modelPart = this.getArm(humanoidArm);
		boolean bl = modelPart.visible;
		modelPart.visible = true;
		super.translateToHand(humanoidArm, poseStack);
		modelPart.visible = bl;
	}
}
