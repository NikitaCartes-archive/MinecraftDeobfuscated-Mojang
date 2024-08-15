package net.minecraft.client.model;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;

@Environment(EnvType.CLIENT)
public class LavaSlimeModel extends EntityModel<SlimeRenderState> {
	private static final int SEGMENT_COUNT = 8;
	private final ModelPart root;
	private final ModelPart[] bodyCubes = new ModelPart[8];

	public LavaSlimeModel(ModelPart modelPart) {
		this.root = modelPart;
		Arrays.setAll(this.bodyCubes, i -> modelPart.getChild(getSegmentName(i)));
	}

	private static String getSegmentName(int i) {
		return "cube" + i;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();

		for (int i = 0; i < 8; i++) {
			int j = 0;
			int k = i;
			if (i == 2) {
				j = 24;
				k = 10;
			} else if (i == 3) {
				j = 24;
				k = 19;
			}

			partDefinition.addOrReplaceChild(
				getSegmentName(i), CubeListBuilder.create().texOffs(j, k).addBox(-4.0F, (float)(16 + i), -4.0F, 8.0F, 1.0F, 8.0F), PartPose.ZERO
			);
		}

		partDefinition.addOrReplaceChild("inside_cube", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 18.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	public void setupAnim(SlimeRenderState slimeRenderState) {
		float f = Math.max(0.0F, slimeRenderState.squish);

		for (int i = 0; i < this.bodyCubes.length; i++) {
			this.bodyCubes[i].y = (float)(-(4 - i)) * f * 1.7F;
		}
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
