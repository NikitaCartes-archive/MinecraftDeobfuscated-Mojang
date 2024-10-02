package net.minecraft.client.model;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.definitions.CreakingAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;

@Environment(EnvType.CLIENT)
public class CreakingModel extends EntityModel<CreakingRenderState> {
	public static final List<ModelPart> NO_PARTS = List.of();
	private final ModelPart head;
	private final List<ModelPart> headParts;

	public CreakingModel(ModelPart modelPart) {
		super(modelPart);
		ModelPart modelPart2 = modelPart.getChild("root");
		ModelPart modelPart3 = modelPart2.getChild("upper_body");
		this.head = modelPart3.getChild("head");
		this.headParts = List.of(this.head);
	}

	private static MeshDefinition createMesh() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.offset(-1.0F, -19.0F, 0.0F));
		partDefinition3.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-3.0F, -10.0F, -3.0F, 6.0F, 10.0F, 6.0F)
				.texOffs(28, 31)
				.addBox(-3.0F, -13.0F, -3.0F, 6.0F, 3.0F, 6.0F)
				.texOffs(12, 40)
				.addBox(3.0F, -13.0F, 0.0F, 9.0F, 14.0F, 0.0F)
				.texOffs(34, 12)
				.addBox(-12.0F, -14.0F, 0.0F, 9.0F, 14.0F, 0.0F),
			PartPose.offset(-3.0F, -11.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, -3.0F, -3.0F, 6.0F, 13.0F, 5.0F).texOffs(24, 0).addBox(-6.0F, -4.0F, -3.0F, 6.0F, 7.0F, 5.0F),
			PartPose.offset(0.0F, -7.0F, 1.0F)
		);
		partDefinition3.addOrReplaceChild(
			"right_arm",
			CubeListBuilder.create().texOffs(22, 13).addBox(-2.0F, -1.5F, -1.5F, 3.0F, 21.0F, 3.0F).texOffs(46, 0).addBox(-2.0F, 19.5F, -1.5F, 3.0F, 4.0F, 3.0F),
			PartPose.offset(-7.0F, -9.5F, 1.5F)
		);
		partDefinition3.addOrReplaceChild(
			"left_arm",
			CubeListBuilder.create()
				.texOffs(30, 40)
				.addBox(0.0F, -1.0F, -1.5F, 3.0F, 16.0F, 3.0F)
				.texOffs(52, 12)
				.addBox(0.0F, -5.0F, -1.5F, 3.0F, 4.0F, 3.0F)
				.texOffs(52, 19)
				.addBox(0.0F, 15.0F, -1.5F, 3.0F, 4.0F, 3.0F),
			PartPose.offset(6.0F, -9.0F, 0.5F)
		);
		partDefinition2.addOrReplaceChild(
			"left_leg",
			CubeListBuilder.create().texOffs(42, 40).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F).texOffs(45, 55).addBox(-1.5F, 15.7F, -4.5F, 5.0F, 0.0F, 9.0F),
			PartPose.offset(1.5F, -16.0F, 0.5F)
		);
		partDefinition2.addOrReplaceChild(
			"right_leg",
			CubeListBuilder.create()
				.texOffs(0, 34)
				.addBox(-3.0F, -1.5F, -1.5F, 3.0F, 19.0F, 3.0F)
				.texOffs(45, 46)
				.addBox(-5.0F, 17.2F, -4.5F, 5.0F, 0.0F, 9.0F)
				.texOffs(12, 34)
				.addBox(-3.0F, -4.5F, -1.5F, 3.0F, 3.0F, 3.0F),
			PartPose.offset(-1.0F, -17.5F, 0.5F)
		);
		return meshDefinition;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = createMesh();
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void setupAnim(CreakingRenderState creakingRenderState) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		if (creakingRenderState.canMove) {
			this.animateWalk(CreakingAnimation.CREAKING_WALK, creakingRenderState.walkAnimationPos, creakingRenderState.walkAnimationSpeed, 5.5F, 3.0F);
		}

		this.animate(creakingRenderState.attackAnimationState, CreakingAnimation.CREAKING_ATTACK, creakingRenderState.ageInTicks);
		this.animate(creakingRenderState.invulnerabilityAnimationState, CreakingAnimation.CREAKING_INVULNERABLE, creakingRenderState.ageInTicks);
	}

	public List<ModelPart> getHeadModelParts(CreakingRenderState creakingRenderState) {
		return !creakingRenderState.isActive ? NO_PARTS : this.headParts;
	}
}
