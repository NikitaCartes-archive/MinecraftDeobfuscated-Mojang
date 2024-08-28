package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class SpinAttackEffectModel extends EntityModel<PlayerRenderState> {
	private static final int BOX_COUNT = 2;
	private final ModelPart[] boxes = new ModelPart[2];

	public SpinAttackEffectModel(ModelPart modelPart) {
		super(modelPart);

		for (int i = 0; i < 2; i++) {
			this.boxes[i] = modelPart.getChild(boxName(i));
		}
	}

	private static String boxName(int i) {
		return "box" + i;
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();

		for (int i = 0; i < 2; i++) {
			float f = -3.2F + 9.6F * (float)(i + 1);
			float g = 0.75F * (float)(i + 1);
			partDefinition.addOrReplaceChild(
				boxName(i), CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F + f, -8.0F, 16.0F, 32.0F, 16.0F), PartPose.ZERO.withScale(g)
			);
		}

		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	public void setupAnim(PlayerRenderState playerRenderState) {
		super.setupAnim(playerRenderState);

		for (int i = 0; i < this.boxes.length; i++) {
			float f = playerRenderState.ageInTicks * (float)(-(45 + (i + 1) * 5));
			this.boxes[i].yRot = Mth.wrapDegrees(f) * (float) (Math.PI / 180.0);
		}
	}
}
