package net.minecraft.client.model;

import java.util.Set;
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
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ambient.Batato;

@Environment(EnvType.CLIENT)
public class BatatoModel extends HierarchicalModel<Batato> {
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart rightWing;
	private final ModelPart leftWing;
	private final ModelPart rightWingTip;
	private final ModelPart leftWingTip;
	private final ModelPart feet;

	public BatatoModel(ModelPart modelPart) {
		super(RenderType::entityCutout);
		this.root = modelPart;
		this.body = modelPart.getChild("body");
		this.rightWing = this.body.getChild("right_wing");
		this.rightWingTip = this.rightWing.getChild("right_wing_tip");
		this.leftWing = this.body.getChild("left_wing");
		this.leftWingTip = this.leftWing.getChild("left_wing_tip");
		this.feet = this.body.getChild("feet");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create()
				.texOffs(19, 20)
				.addBox(-1.5F, -2.0F, 0.0F, 13.0F, 12.0F, 0.01F, Set.of(Direction.NORTH))
				.texOffs(6, 20)
				.mirror()
				.addBox(-1.5F, -2.0F, 0.0F, 13.0F, 12.0F, 0.01F, Set.of(Direction.SOUTH)),
			PartPose.offset(0.0F, 17.0F, 0.0F)
		);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"right_wing", CubeListBuilder.create().texOffs(12, 0).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 7.0F, 0.0F), PartPose.offset(-1.5F, 0.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"right_wing_tip", CubeListBuilder.create().texOffs(16, 0).addBox(-6.0F, -2.0F, 0.0F, 6.0F, 8.0F, 0.0F), PartPose.offset(-2.0F, 0.0F, 0.0F)
		);
		PartDefinition partDefinition4 = partDefinition2.addOrReplaceChild(
			"left_wing", CubeListBuilder.create().texOffs(12, 7).addBox(0.0F, -2.0F, 0.0F, 2.0F, 7.0F, 0.0F), PartPose.offset(11.5F, 2.0F, 0.0F)
		);
		partDefinition4.addOrReplaceChild(
			"left_wing_tip", CubeListBuilder.create().texOffs(16, 8).addBox(0.0F, -2.0F, 0.0F, 6.0F, 8.0F, 0.0F), PartPose.offset(2.0F, 0.0F, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"feet", CubeListBuilder.create().texOffs(16, 16).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 2.0F, 0.0F), PartPose.offset(3.0F, 10.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public void setupAnim(Batato batato, float f, float g, float h, float i, float j) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(batato.flyAnimationState, BatAnimation.BAT_FLYING, h, 1.0F);
		this.animate(batato.restAnimationState, BatAnimation.BAT_RESTING, h, 1.0F);
	}
}
