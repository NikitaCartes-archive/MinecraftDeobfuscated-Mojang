package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EvokerFangsRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class EvokerFangsModel extends EntityModel<EvokerFangsRenderState> {
	private static final String BASE = "base";
	private static final String UPPER_JAW = "upper_jaw";
	private static final String LOWER_JAW = "lower_jaw";
	private final ModelPart base;
	private final ModelPart upperJaw;
	private final ModelPart lowerJaw;

	public EvokerFangsModel(ModelPart modelPart) {
		super(modelPart);
		this.base = modelPart.getChild("base");
		this.upperJaw = this.base.getChild("upper_jaw");
		this.lowerJaw = this.base.getChild("lower_jaw");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"base", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 10.0F, 12.0F, 10.0F), PartPose.offset(-5.0F, 24.0F, -5.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(40, 0).addBox(0.0F, 0.0F, 0.0F, 4.0F, 14.0F, 8.0F);
		partDefinition2.addOrReplaceChild("upper_jaw", cubeListBuilder, PartPose.offsetAndRotation(6.5F, 0.0F, 1.0F, 0.0F, 0.0F, 2.042035F));
		partDefinition2.addOrReplaceChild("lower_jaw", cubeListBuilder, PartPose.offsetAndRotation(3.5F, 0.0F, 9.0F, 0.0F, (float) Math.PI, 4.2411504F));
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(EvokerFangsRenderState evokerFangsRenderState) {
		super.setupAnim(evokerFangsRenderState);
		float f = evokerFangsRenderState.biteProgress;
		float g = Math.min(f * 2.0F, 1.0F);
		g = 1.0F - g * g * g;
		this.upperJaw.zRot = (float) Math.PI - g * 0.35F * (float) Math.PI;
		this.lowerJaw.zRot = (float) Math.PI + g * 0.35F * (float) Math.PI;
		this.base.y = this.base.y - (f + Mth.sin(f * 2.7F)) * 7.2F;
		float h = 1.0F;
		if (f > 0.9F) {
			h *= (1.0F - f) / 0.1F;
		}

		this.root.y = 24.0F - 20.0F * h;
		this.root.xScale = h;
		this.root.yScale = h;
		this.root.zScale = h;
	}
}
