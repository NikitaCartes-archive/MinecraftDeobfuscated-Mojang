package net.minecraft.client.model;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ChickenModel extends EntityModel<ChickenRenderState> {
	public static final String RED_THING = "red_thing";
	public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(Set.of("head", "beak", "red_thing"));
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;
	private final ModelPart rightWing;
	private final ModelPart leftWing;
	private final ModelPart beak;
	private final ModelPart redThing;

	public ChickenModel(ModelPart modelPart) {
		this.root = modelPart;
		this.head = modelPart.getChild("head");
		this.beak = modelPart.getChild("beak");
		this.redThing = modelPart.getChild("red_thing");
		this.rightLeg = modelPart.getChild("right_leg");
		this.leftLeg = modelPart.getChild("left_leg");
		this.rightWing = modelPart.getChild("right_wing");
		this.leftWing = modelPart.getChild("left_wing");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		int i = 16;
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 15.0F, -4.0F)
		);
		partDefinition.addOrReplaceChild(
			"beak", CubeListBuilder.create().texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 15.0F, -4.0F)
		);
		partDefinition.addOrReplaceChild(
			"red_thing", CubeListBuilder.create().texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 15.0F, -4.0F)
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F),
			PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F);
		partDefinition.addOrReplaceChild("right_leg", cubeListBuilder, PartPose.offset(-2.0F, 19.0F, 1.0F));
		partDefinition.addOrReplaceChild("left_leg", cubeListBuilder, PartPose.offset(1.0F, 19.0F, 1.0F));
		partDefinition.addOrReplaceChild(
			"right_wing", CubeListBuilder.create().texOffs(24, 13).addBox(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(-4.0F, 13.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_wing", CubeListBuilder.create().texOffs(24, 13).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(4.0F, 13.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public void setupAnim(ChickenRenderState chickenRenderState) {
		float f = (Mth.sin(chickenRenderState.flap) + 1.0F) * chickenRenderState.flapSpeed;
		this.head.xRot = chickenRenderState.xRot * (float) (Math.PI / 180.0);
		this.head.yRot = chickenRenderState.yRot * (float) (Math.PI / 180.0);
		this.beak.xRot = this.head.xRot;
		this.beak.yRot = this.head.yRot;
		this.redThing.xRot = this.head.xRot;
		this.redThing.yRot = this.head.yRot;
		float g = chickenRenderState.walkAnimationSpeed;
		float h = chickenRenderState.walkAnimationPos;
		this.rightLeg.xRot = Mth.cos(h * 0.6662F) * 1.4F * g;
		this.leftLeg.xRot = Mth.cos(h * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.rightWing.zRot = f;
		this.leftWing.zRot = -f;
	}
}
