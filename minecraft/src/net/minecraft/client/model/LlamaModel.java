package net.minecraft.client.model;

import java.util.Map.Entry;
import java.util.function.UnaryOperator;
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
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class LlamaModel extends EntityModel<LlamaRenderState> {
	public static final MeshTransformer BABY_TRANSFORMER = LlamaModel::transformToBaby;
	private final ModelPart head;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightChest;
	private final ModelPart leftChest;

	public LlamaModel(ModelPart modelPart) {
		super(modelPart);
		this.head = modelPart.getChild("head");
		this.rightChest = modelPart.getChild("right_chest");
		this.leftChest = modelPart.getChild("left_chest");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
	}

	public static LayerDefinition createBodyLayer(CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-2.0F, -14.0F, -10.0F, 4.0F, 4.0F, 9.0F, cubeDeformation)
				.texOffs(0, 14)
				.addBox("neck", -4.0F, -16.0F, -6.0F, 8.0F, 18.0F, 6.0F, cubeDeformation)
				.texOffs(17, 0)
				.addBox("ear", -4.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, cubeDeformation)
				.texOffs(17, 0)
				.addBox("ear", 1.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, cubeDeformation),
			PartPose.offset(0.0F, 7.0F, -6.0F)
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(29, 0).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, cubeDeformation),
			PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_chest",
			CubeListBuilder.create().texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, cubeDeformation),
			PartPose.offsetAndRotation(-8.5F, 3.0F, 3.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_chest",
			CubeListBuilder.create().texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, cubeDeformation),
			PartPose.offsetAndRotation(5.5F, 3.0F, 3.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
		);
		int i = 4;
		int j = 14;
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(29, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-3.5F, 10.0F, 6.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(3.5F, 10.0F, 6.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-3.5F, 10.0F, -5.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(3.5F, 10.0F, -5.0F));
		return LayerDefinition.create(meshDefinition, 128, 64);
	}

	private static MeshDefinition transformToBaby(MeshDefinition meshDefinition) {
		float f = 2.0F;
		float g = 0.7F;
		float h = 1.1F;
		UnaryOperator<PartPose> unaryOperator = partPose -> partPose.translated(0.0F, 21.0F, 3.52F).scaled(0.71428573F, 0.64935064F, 0.7936508F);
		UnaryOperator<PartPose> unaryOperator2 = partPose -> partPose.translated(0.0F, 33.0F, 0.0F).scaled(0.625F, 0.45454544F, 0.45454544F);
		UnaryOperator<PartPose> unaryOperator3 = partPose -> partPose.translated(0.0F, 33.0F, 0.0F).scaled(0.45454544F, 0.41322312F, 0.45454544F);
		MeshDefinition meshDefinition2 = new MeshDefinition();

		for (Entry<String, PartDefinition> entry : meshDefinition.getRoot().getChildren()) {
			String string = (String)entry.getKey();
			PartDefinition partDefinition = (PartDefinition)entry.getValue();

			UnaryOperator<PartPose> unaryOperator4 = switch (string) {
				case "head" -> unaryOperator;
				case "body" -> unaryOperator2;
				default -> unaryOperator3;
			};
			meshDefinition2.getRoot().addOrReplaceChild(string, partDefinition.transformed(unaryOperator4));
		}

		return meshDefinition2;
	}

	public void setupAnim(LlamaRenderState llamaRenderState) {
		super.setupAnim(llamaRenderState);
		this.head.xRot = llamaRenderState.xRot * (float) (Math.PI / 180.0);
		this.head.yRot = llamaRenderState.yRot * (float) (Math.PI / 180.0);
		float f = llamaRenderState.walkAnimationSpeed;
		float g = llamaRenderState.walkAnimationPos;
		this.rightHindLeg.xRot = Mth.cos(g * 0.6662F) * 1.4F * f;
		this.leftHindLeg.xRot = Mth.cos(g * 0.6662F + (float) Math.PI) * 1.4F * f;
		this.rightFrontLeg.xRot = Mth.cos(g * 0.6662F + (float) Math.PI) * 1.4F * f;
		this.leftFrontLeg.xRot = Mth.cos(g * 0.6662F) * 1.4F * f;
		this.rightChest.visible = llamaRenderState.hasChest;
		this.leftChest.visible = llamaRenderState.hasChest;
	}
}
