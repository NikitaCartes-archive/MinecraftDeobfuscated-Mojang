package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.definitions.BatAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.ambient.Bat;

@Environment(EnvType.CLIENT)
public class BatModel extends HierarchicalModel<Bat> {
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart rightWing;
	private final ModelPart leftWing;
	private final ModelPart rightWingTip;
	private final ModelPart leftWingTip;
	private final ModelPart feet;

	public BatModel(ModelPart modelPart) {
		super(RenderType::entityCutout);
		this.root = modelPart.getChild("root");
		this.body = this.root.getChild("body");
		this.head = this.root.getChild("head");
		this.rightWing = this.body.getChild("right_wing");
		this.rightWingTip = this.rightWing.getChild("right_wing_tip");
		this.leftWing = this.body.getChild("left_wing");
		this.leftWingTip = this.leftWing.getChild("left_wing_tip");
		this.feet = this.body.getChild("feet");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 2.0F, -1.0F, 3.0F, 5.0F, 2.0F), PartPose.ZERO
		);
		PartDefinition partDefinition4 = partDefinition2.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 7).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 3.0F, 2.0F), PartPose.ZERO
		);
		partDefinition4.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(1, 15).addBox(-4.0F, -3.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.ZERO);
		partDefinition4.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(8, 15).addBox(1.0F, -3.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.ZERO);
		PartDefinition partDefinition5 = partDefinition3.addOrReplaceChild(
			"right_wing", CubeListBuilder.create().texOffs(12, 0).addBox(-2.0F, 0.0F, 0.0F, 2.0F, 7.0F, 0.0F), PartPose.offset(-1.5F, 0.0F, 0.0F)
		);
		partDefinition5.addOrReplaceChild(
			"right_wing_tip", CubeListBuilder.create().texOffs(16, 0).addBox(-6.0F, 0.0F, 0.0F, 6.0F, 8.0F, 0.0F), PartPose.offset(-2.0F, 0.0F, 0.0F)
		);
		PartDefinition partDefinition6 = partDefinition3.addOrReplaceChild(
			"left_wing", CubeListBuilder.create().texOffs(12, 7).addBox(0.0F, 0.0F, 0.0F, 2.0F, 7.0F, 0.0F), PartPose.offset(1.5F, 0.0F, 0.0F)
		);
		partDefinition6.addOrReplaceChild(
			"left_wing_tip", CubeListBuilder.create().texOffs(16, 8).addBox(0.0F, 0.0F, 0.0F, 6.0F, 8.0F, 0.0F), PartPose.offset(2.0F, 0.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"feet", CubeListBuilder.create().texOffs(16, 16).addBox(0.0F, 0.0F, 0.0F, 3.0F, 2.0F, 0.0F), PartPose.offset(-1.5F, 7.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public void setupAnim(Bat bat, float f, float g, float h, float i, float j) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		if (bat.isResting()) {
			this.applyHeadRotation(j, i);
		}

		this.animate(bat.flyAnimationState, BatAnimation.BAT_FLYING, h, 1.0F);
		this.animate(bat.restAnimationState, BatAnimation.BAT_RESTING, h, 1.0F);
	}

	private void applyHeadRotation(float f, float g) {
		this.head.xRot = f * (float) (Math.PI / 180.0);
		this.head.yRot = (180.0F - g) * (float) (Math.PI / 180.0);
		this.head.zRot = (float) Math.PI;
	}
}
