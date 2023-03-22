package net.minecraft.client.model;

import com.google.common.collect.ImmutableList.Builder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

@Environment(EnvType.CLIENT)
public class ChestRaftModel extends RaftModel {
	private static final String CHEST_BOTTOM = "chest_bottom";
	private static final String CHEST_LID = "chest_lid";
	private static final String CHEST_LOCK = "chest_lock";

	public ChestRaftModel(ModelPart modelPart) {
		super(modelPart);
	}

	@Override
	protected Builder<ModelPart> createPartsBuilder(ModelPart modelPart) {
		Builder<ModelPart> builder = super.createPartsBuilder(modelPart);
		builder.add(modelPart.getChild("chest_bottom"));
		builder.add(modelPart.getChild("chest_lid"));
		builder.add(modelPart.getChild("chest_lock"));
		return builder;
	}

	public static LayerDefinition createBodyModel() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		RaftModel.createChildren(partDefinition);
		partDefinition.addOrReplaceChild(
			"chest_bottom",
			CubeListBuilder.create().texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 12.0F, 8.0F, 12.0F),
			PartPose.offsetAndRotation(-2.0F, -10.0F, -6.0F, 0.0F, (float) (-Math.PI / 2), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"chest_lid",
			CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 0.0F, 0.0F, 12.0F, 4.0F, 12.0F),
			PartPose.offsetAndRotation(-2.0F, -14.0F, -6.0F, 0.0F, (float) (-Math.PI / 2), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"chest_lock",
			CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 1.0F),
			PartPose.offsetAndRotation(-1.0F, -11.0F, -1.0F, 0.0F, (float) (-Math.PI / 2), 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 128, 128);
	}
}
