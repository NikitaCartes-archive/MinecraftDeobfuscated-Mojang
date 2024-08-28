package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class EndermiteModel extends EntityModel<EntityRenderState> {
	private static final int BODY_COUNT = 4;
	private static final int[][] BODY_SIZES = new int[][]{{4, 3, 2}, {6, 4, 5}, {3, 3, 1}, {1, 2, 1}};
	private static final int[][] BODY_TEXS = new int[][]{{0, 0}, {0, 5}, {0, 14}, {0, 18}};
	private final ModelPart[] bodyParts = new ModelPart[4];

	public EndermiteModel(ModelPart modelPart) {
		super(modelPart);

		for (int i = 0; i < 4; i++) {
			this.bodyParts[i] = modelPart.getChild(createSegmentName(i));
		}
	}

	private static String createSegmentName(int i) {
		return "segment" + i;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		float f = -3.5F;

		for (int i = 0; i < 4; i++) {
			partDefinition.addOrReplaceChild(
				createSegmentName(i),
				CubeListBuilder.create()
					.texOffs(BODY_TEXS[i][0], BODY_TEXS[i][1])
					.addBox((float)BODY_SIZES[i][0] * -0.5F, 0.0F, (float)BODY_SIZES[i][2] * -0.5F, (float)BODY_SIZES[i][0], (float)BODY_SIZES[i][1], (float)BODY_SIZES[i][2]),
				PartPose.offset(0.0F, (float)(24 - BODY_SIZES[i][1]), f)
			);
			if (i < 3) {
				f += (float)(BODY_SIZES[i][2] + BODY_SIZES[i + 1][2]) * 0.5F;
			}
		}

		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public void setupAnim(EntityRenderState entityRenderState) {
		super.setupAnim(entityRenderState);

		for (int i = 0; i < this.bodyParts.length; i++) {
			this.bodyParts[i].yRot = Mth.cos(entityRenderState.ageInTicks * 0.9F + (float)i * 0.15F * (float) Math.PI)
				* (float) Math.PI
				* 0.01F
				* (float)(1 + Math.abs(i - 2));
			this.bodyParts[i].x = Mth.sin(entityRenderState.ageInTicks * 0.9F + (float)i * 0.15F * (float) Math.PI) * (float) Math.PI * 0.1F * (float)Math.abs(i - 2);
		}
	}
}
