package net.minecraft.client.model;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class GhastModel<T extends Entity> extends HierarchicalModel<T> {
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
		Random random = new Random(1660L);

		for (int i = 0; i < 9; i++) {
			float f = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
			float g = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
			int j = random.nextInt(7) + 8;
			partDefinition.addOrReplaceChild(
				createTentacleName(i), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, (float)j, 2.0F), PartPose.offset(f, 24.6F, g)
			);
		}

		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		for (int k = 0; k < this.tentacles.length; k++) {
			this.tentacles[k].xRot = 0.2F * Mth.sin(h * 0.3F + (float)k) + 0.4F;
		}
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
