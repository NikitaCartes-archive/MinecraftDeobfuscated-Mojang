package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class WitchModel extends EntityModel<WitchRenderState> implements HeadedModel, VillagerHeadModel {
	protected final ModelPart nose;
	private final ModelPart head;
	private final ModelPart hat;
	private final ModelPart hatRim;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public WitchModel(ModelPart modelPart) {
		super(modelPart);
		this.head = modelPart.getChild("head");
		this.hat = this.head.getChild("hat");
		this.hatRim = this.hat.getChild("hat_rim");
		this.nose = this.head.getChild("nose");
		this.rightLeg = modelPart.getChild("right_leg");
		this.leftLeg = modelPart.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = VillagerModel.createBodyModel();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.ZERO
		);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"hat", CubeListBuilder.create().texOffs(0, 64).addBox(0.0F, 0.0F, 0.0F, 10.0F, 2.0F, 10.0F), PartPose.offset(-5.0F, -10.03125F, -5.0F)
		);
		PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild(
			"hat2",
			CubeListBuilder.create().texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 7.0F, 4.0F, 7.0F),
			PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.05235988F, 0.0F, 0.02617994F)
		);
		PartDefinition partDefinition5 = partDefinition4.addOrReplaceChild(
			"hat3",
			CubeListBuilder.create().texOffs(0, 87).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F),
			PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.10471976F, 0.0F, 0.05235988F)
		);
		partDefinition5.addOrReplaceChild(
			"hat4",
			CubeListBuilder.create().texOffs(0, 95).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)),
			PartPose.offsetAndRotation(1.75F, -2.0F, 2.0F, (float) (-Math.PI / 15), 0.0F, 0.10471976F)
		);
		PartDefinition partDefinition6 = partDefinition2.getChild("nose");
		partDefinition6.addOrReplaceChild(
			"mole", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 3.0F, -6.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)), PartPose.offset(0.0F, -2.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 128);
	}

	public void setupAnim(WitchRenderState witchRenderState) {
		super.setupAnim(witchRenderState);
		this.head.yRot = witchRenderState.yRot * (float) (Math.PI / 180.0);
		this.head.xRot = witchRenderState.xRot * (float) (Math.PI / 180.0);
		this.rightLeg.xRot = Mth.cos(witchRenderState.walkAnimationPos * 0.6662F) * 1.4F * witchRenderState.walkAnimationSpeed * 0.5F;
		this.leftLeg.xRot = Mth.cos(witchRenderState.walkAnimationPos * 0.6662F + (float) Math.PI) * 1.4F * witchRenderState.walkAnimationSpeed * 0.5F;
		float f = 0.01F * (float)(witchRenderState.entityId % 10);
		this.nose.xRot = Mth.sin(witchRenderState.ageInTicks * f) * 4.5F * (float) (Math.PI / 180.0);
		this.nose.zRot = Mth.cos(witchRenderState.ageInTicks * f) * 2.5F * (float) (Math.PI / 180.0);
		if (witchRenderState.isHoldingItem) {
			this.nose.setPos(0.0F, 1.0F, -1.5F);
			this.nose.xRot = -0.9F;
		}
	}

	public ModelPart getNose() {
		return this.nose;
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
