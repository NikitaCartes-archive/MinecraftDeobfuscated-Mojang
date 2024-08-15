package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class GhastModel extends EntityModel<GhastRenderState> {
	private final ModelPart root;
	private final ModelPart[] tentacles = new ModelPart[9];

	public GhastModel(ModelPart modelPart) {
		this.root = modelPart;

		for (int i = 0; i < this.tentacles.length; i++) {
			this.tentacles[i] = modelPart.getChild(createTentacleName(i));
		}
	}

	private static String createTentacleName(int i) {
		return "tentacle" + i;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 17.6F, 0.0F)
		);
		RandomSource randomSource = RandomSource.create(1660L);

		for (int i = 0; i < 9; i++) {
			float f = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
			float g = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
			int j = randomSource.nextInt(7) + 8;
			partDefinition.addOrReplaceChild(
				createTentacleName(i), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, (float)j, 2.0F), PartPose.offset(f, 24.6F, g)
			);
		}

		return LayerDefinition.create(meshDefinition, 64, 32).apply(MeshTransformer.scaling(4.5F));
	}

	public void setupAnim(GhastRenderState ghastRenderState) {
		for (int i = 0; i < this.tentacles.length; i++) {
			this.tentacles[i].xRot = 0.2F * Mth.sin(ghastRenderState.ageInTicks * 0.3F + (float)i) + 0.4F;
		}
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
