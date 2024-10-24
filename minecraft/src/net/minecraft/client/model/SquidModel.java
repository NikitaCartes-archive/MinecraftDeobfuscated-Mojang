package net.minecraft.client.model;

import java.util.Arrays;
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
import net.minecraft.client.renderer.entity.state.SquidRenderState;

@Environment(EnvType.CLIENT)
public class SquidModel extends EntityModel<SquidRenderState> {
	public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5F);
	private final ModelPart[] tentacles = new ModelPart[8];

	public SquidModel(ModelPart modelPart) {
		super(modelPart);
		Arrays.setAll(this.tentacles, i -> modelPart.getChild(createTentacleName(i)));
	}

	private static String createTentacleName(int i) {
		return "tentacle" + i;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		CubeDeformation cubeDeformation = new CubeDeformation(0.02F);
		int i = -16;
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 12.0F, cubeDeformation), PartPose.offset(0.0F, 8.0F, 0.0F)
		);
		int j = 8;
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F);

		for (int k = 0; k < 8; k++) {
			double d = (double)k * Math.PI * 2.0 / 8.0;
			float f = (float)Math.cos(d) * 5.0F;
			float g = 15.0F;
			float h = (float)Math.sin(d) * 5.0F;
			d = (double)k * Math.PI * -2.0 / 8.0 + (Math.PI / 2);
			float l = (float)d;
			partDefinition.addOrReplaceChild(createTentacleName(k), cubeListBuilder, PartPose.offsetAndRotation(f, 15.0F, h, 0.0F, l, 0.0F));
		}

		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(SquidRenderState squidRenderState) {
		super.setupAnim(squidRenderState);

		for (ModelPart modelPart : this.tentacles) {
			modelPart.xRot = squidRenderState.tentacleAngle;
		}
	}
}
