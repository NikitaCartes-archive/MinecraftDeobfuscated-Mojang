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
import net.minecraft.client.renderer.entity.state.PolarBearRenderState;

@Environment(EnvType.CLIENT)
public class PolarBearModel extends QuadrupedModel<PolarBearRenderState> {
	public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 16.0F, 4.0F, 2.25F, 2.0F, 24.0F, Set.of("head"));

	public PolarBearModel(ModelPart modelPart) {
		super(modelPart);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-3.5F, -3.0F, -3.0F, 7.0F, 7.0F, 7.0F)
				.texOffs(0, 44)
				.addBox("mouth", -2.5F, 1.0F, -6.0F, 5.0F, 3.0F, 3.0F)
				.texOffs(26, 0)
				.addBox("right_ear", -4.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F)
				.texOffs(26, 0)
				.mirror()
				.addBox("left_ear", 2.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F),
			PartPose.offset(0.0F, 10.0F, -16.0F)
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(0, 19).addBox(-5.0F, -13.0F, -7.0F, 14.0F, 14.0F, 11.0F).texOffs(39, 0).addBox(-4.0F, -25.0F, -7.0F, 12.0F, 12.0F, 10.0F),
			PartPose.offsetAndRotation(-2.0F, 9.0F, 12.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		int i = 10;
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(50, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F);
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-4.5F, 14.0F, 6.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(4.5F, 14.0F, 6.0F));
		CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(50, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F);
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-3.5F, 14.0F, -8.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offset(3.5F, 14.0F, -8.0F));
		return LayerDefinition.create(meshDefinition, 128, 64).apply(MeshTransformer.scaling(1.2F));
	}

	public void setupAnim(PolarBearRenderState polarBearRenderState) {
		super.setupAnim(polarBearRenderState);
		float f = polarBearRenderState.standScale * polarBearRenderState.standScale;
		float g = 1.0F - f;
		this.body.xRot = (float) (Math.PI / 2) - f * (float) Math.PI * 0.35F;
		this.body.y = 9.0F * g + 11.0F * f;
		this.rightFrontLeg.y = 14.0F * g - 6.0F * f;
		this.rightFrontLeg.z = -8.0F * g - 4.0F * f;
		this.rightFrontLeg.xRot -= f * (float) Math.PI * 0.45F;
		this.leftFrontLeg.y = this.rightFrontLeg.y;
		this.leftFrontLeg.z = this.rightFrontLeg.z;
		this.leftFrontLeg.xRot -= f * (float) Math.PI * 0.45F;
		if (polarBearRenderState.isBaby) {
			this.head.y = 10.0F * g - 9.0F * f;
			this.head.z = -16.0F * g - 7.0F * f;
		} else {
			this.head.y = 10.0F * g - 14.0F * f;
			this.head.z = -16.0F * g - 3.0F * f;
		}

		this.head.xRot += f * (float) Math.PI * 0.15F;
	}
}
