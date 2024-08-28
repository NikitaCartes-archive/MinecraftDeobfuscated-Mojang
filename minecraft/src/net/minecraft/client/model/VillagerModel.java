package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class VillagerModel extends EntityModel<VillagerRenderState> implements HeadedModel, VillagerHeadModel {
	private final ModelPart head;
	private final ModelPart hat;
	private final ModelPart hatRim;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public VillagerModel(ModelPart modelPart) {
		super(modelPart);
		this.head = modelPart.getChild("head");
		this.hat = this.head.getChild("hat");
		this.hatRim = this.hat.getChild("hat_rim");
		this.rightLeg = modelPart.getChild("right_leg");
		this.leftLeg = modelPart.getChild("left_leg");
	}

	public static MeshDefinition createBodyModel() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		float f = 0.5F;
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.ZERO
		);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.51F)), PartPose.ZERO
		);
		partDefinition3.addOrReplaceChild(
			"hat_rim", CubeListBuilder.create().texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F), PartPose.rotation((float) (-Math.PI / 2), 0.0F, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, -2.0F, 0.0F)
		);
		PartDefinition partDefinition4 = partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F), PartPose.ZERO
		);
		partDefinition4.addOrReplaceChild(
			"jacket", CubeListBuilder.create().texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.ZERO
		);
		partDefinition.addOrReplaceChild(
			"arms",
			CubeListBuilder.create()
				.texOffs(44, 22)
				.addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F)
				.texOffs(44, 22)
				.addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, true)
				.texOffs(40, 38)
				.addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
			PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-2.0F, 12.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_leg", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(2.0F, 12.0F, 0.0F)
		);
		return meshDefinition;
	}

	public void setupAnim(VillagerRenderState villagerRenderState) {
		super.setupAnim(villagerRenderState);
		this.head.yRot = villagerRenderState.yRot * (float) (Math.PI / 180.0);
		this.head.xRot = villagerRenderState.xRot * (float) (Math.PI / 180.0);
		if (villagerRenderState.isUnhappy) {
			this.head.zRot = 0.3F * Mth.sin(0.45F * villagerRenderState.ageInTicks);
			this.head.xRot = 0.4F;
		} else {
			this.head.zRot = 0.0F;
		}

		this.rightLeg.xRot = Mth.cos(villagerRenderState.walkAnimationPos * 0.6662F) * 1.4F * villagerRenderState.walkAnimationSpeed * 0.5F;
		this.leftLeg.xRot = Mth.cos(villagerRenderState.walkAnimationPos * 0.6662F + (float) Math.PI) * 1.4F * villagerRenderState.walkAnimationSpeed * 0.5F;
		this.rightLeg.yRot = 0.0F;
		this.leftLeg.yRot = 0.0F;
	}

	@Override
	public ModelPart getHead() {
		return this.head;
	}

	@Override
	public void hatVisible(boolean bl) {
		this.head.visible = bl;
		this.hat.visible = bl;
		this.hatRim.visible = bl;
	}
}
