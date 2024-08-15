package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class AbstractPiglinModel<S extends HumanoidRenderState> extends HumanoidModel<S> {
	private static final String LEFT_SLEEVE = "left_sleeve";
	private static final String RIGHT_SLEEVE = "right_sleeve";
	private static final String LEFT_PANTS = "left_pants";
	private static final String RIGHT_PANTS = "right_pants";
	public final ModelPart leftSleeve = this.leftArm.getChild("left_sleeve");
	public final ModelPart rightSleeve = this.rightArm.getChild("right_sleeve");
	public final ModelPart leftPants = this.leftLeg.getChild("left_pants");
	public final ModelPart rightPants = this.rightLeg.getChild("right_pants");
	public final ModelPart jacket = this.body.getChild("jacket");
	public final ModelPart rightEar = this.head.getChild("right_ear");
	public final ModelPart leftEar = this.head.getChild("left_ear");

	public AbstractPiglinModel(ModelPart modelPart) {
		super(modelPart, RenderType::entityTranslucent);
	}

	public static MeshDefinition createMesh(CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = PlayerModel.createMesh(cubeDeformation, false);
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation), PartPose.ZERO
		);
		PartDefinition partDefinition2 = addHead(cubeDeformation, meshDefinition);
		partDefinition2.clearChild("hat");
		return meshDefinition;
	}

	public static PartDefinition addHead(CubeDeformation cubeDeformation, MeshDefinition meshDefinition) {
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, cubeDeformation)
				.texOffs(31, 1)
				.addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, cubeDeformation)
				.texOffs(2, 4)
				.addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, cubeDeformation)
				.texOffs(2, 0)
				.addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, cubeDeformation),
			PartPose.ZERO
		);
		partDefinition2.addOrReplaceChild(
			"left_ear",
			CubeListBuilder.create().texOffs(51, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, cubeDeformation),
			PartPose.offsetAndRotation(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 6))
		);
		partDefinition2.addOrReplaceChild(
			"right_ear",
			CubeListBuilder.create().texOffs(39, 6).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, cubeDeformation),
			PartPose.offsetAndRotation(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 6))
		);
		return partDefinition2;
	}

	@Override
	public void setupAnim(S humanoidRenderState) {
		super.setupAnim(humanoidRenderState);
		float f = humanoidRenderState.walkAnimationPos;
		float g = humanoidRenderState.walkAnimationSpeed;
		float h = (float) (Math.PI / 6);
		float i = humanoidRenderState.ageInTicks * 0.1F + f * 0.5F;
		float j = 0.08F + g * 0.4F;
		this.leftEar.zRot = (float) (-Math.PI / 6) - Mth.cos(i * 1.2F) * j;
		this.rightEar.zRot = (float) (Math.PI / 6) + Mth.cos(i) * j;
	}

	@Override
	public void setAllVisible(boolean bl) {
		super.setAllVisible(bl);
		this.leftSleeve.visible = bl;
		this.rightSleeve.visible = bl;
		this.leftPants.visible = bl;
		this.rightPants.visible = bl;
		this.jacket.visible = bl;
	}
}
