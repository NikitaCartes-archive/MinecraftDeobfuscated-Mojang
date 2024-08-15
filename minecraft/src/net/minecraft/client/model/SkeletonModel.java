package net.minecraft.client.model;

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
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class SkeletonModel<S extends SkeletonRenderState> extends HumanoidModel<S> {
	public SkeletonModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partDefinition = meshDefinition.getRoot();
		createDefaultSkeletonMesh(partDefinition);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	protected static void createDefaultSkeletonMesh(PartDefinition partDefinition) {
		partDefinition.addOrReplaceChild(
			"right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-5.0F, 2.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(5.0F, 2.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-2.0F, 12.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(2.0F, 12.0F, 0.0F)
		);
	}

	protected HumanoidModel.ArmPose getArmPose(S skeletonRenderState, HumanoidArm humanoidArm) {
		return skeletonRenderState.getMainHandItem().is(Items.BOW) && skeletonRenderState.isAggressive && skeletonRenderState.mainArm == humanoidArm
			? HumanoidModel.ArmPose.BOW_AND_ARROW
			: HumanoidModel.ArmPose.EMPTY;
	}

	public void setupAnim(S skeletonRenderState) {
		super.setupAnim(skeletonRenderState);
		ItemStack itemStack = skeletonRenderState.getMainHandItem();
		if (skeletonRenderState.isAggressive && !itemStack.is(Items.BOW)) {
			float f = skeletonRenderState.attackTime;
			float g = Mth.sin(f * (float) Math.PI);
			float h = Mth.sin((1.0F - (1.0F - f) * (1.0F - f)) * (float) Math.PI);
			this.rightArm.zRot = 0.0F;
			this.leftArm.zRot = 0.0F;
			this.rightArm.yRot = -(0.1F - g * 0.6F);
			this.leftArm.yRot = 0.1F - g * 0.6F;
			this.rightArm.xRot = (float) (-Math.PI / 2);
			this.leftArm.xRot = (float) (-Math.PI / 2);
			this.rightArm.xRot -= g * 1.2F - h * 0.4F;
			this.leftArm.xRot -= g * 1.2F - h * 0.4F;
			AnimationUtils.bobArms(this.rightArm, this.leftArm, skeletonRenderState.ageInTicks);
		}
	}

	@Override
	public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
		this.root().translateAndRotate(poseStack);
		float f = humanoidArm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		ModelPart modelPart = this.getArm(humanoidArm);
		modelPart.x += f;
		modelPart.translateAndRotate(poseStack);
		modelPart.x -= f;
	}
}
