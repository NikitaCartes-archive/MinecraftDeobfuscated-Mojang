package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

@Environment(EnvType.CLIENT)
public class BoatModel extends ListModel<Boat> {
	private final ModelPart leftPaddle;
	private final ModelPart rightPaddle;
	private final ModelPart waterPatch;
	private final ImmutableList<ModelPart> parts;

	public BoatModel(ModelPart modelPart) {
		this.leftPaddle = modelPart.getChild("left_paddle");
		this.rightPaddle = modelPart.getChild("right_paddle");
		this.waterPatch = modelPart.getChild("water_patch");
		this.parts = ImmutableList.of(
			modelPart.getChild("bottom"),
			modelPart.getChild("back"),
			modelPart.getChild("front"),
			modelPart.getChild("right"),
			modelPart.getChild("left"),
			this.leftPaddle,
			this.rightPaddle
		);
	}

	public static LayerDefinition createBodyModel() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		int i = 32;
		int j = 6;
		int k = 20;
		int l = 4;
		int m = 28;
		partDefinition.addOrReplaceChild(
			"bottom",
			CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F),
			PartPose.offsetAndRotation(0.0F, 3.0F, 1.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"back",
			CubeListBuilder.create().texOffs(0, 19).addBox(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F),
			PartPose.offsetAndRotation(-15.0F, 4.0F, 4.0F, 0.0F, (float) (Math.PI * 3.0 / 2.0), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"front",
			CubeListBuilder.create().texOffs(0, 27).addBox(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F),
			PartPose.offsetAndRotation(15.0F, 4.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right",
			CubeListBuilder.create().texOffs(0, 35).addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F),
			PartPose.offsetAndRotation(0.0F, 4.0F, -9.0F, 0.0F, (float) Math.PI, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left", CubeListBuilder.create().texOffs(0, 43).addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 9.0F)
		);
		int n = 20;
		int o = 7;
		int p = 6;
		float f = -5.0F;
		partDefinition.addOrReplaceChild(
			"left_paddle",
			CubeListBuilder.create().texOffs(62, 0).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
			PartPose.offsetAndRotation(3.0F, -5.0F, 9.0F, 0.0F, 0.0F, (float) (Math.PI / 16))
		);
		partDefinition.addOrReplaceChild(
			"right_paddle",
			CubeListBuilder.create().texOffs(62, 20).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F),
			PartPose.offsetAndRotation(3.0F, -5.0F, -9.0F, 0.0F, (float) Math.PI, (float) (Math.PI / 16))
		);
		partDefinition.addOrReplaceChild(
			"water_patch",
			CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F),
			PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 128, 64);
	}

	public void setupAnim(Boat boat, float f, float g, float h, float i, float j) {
		animatePaddle(boat, 0, this.leftPaddle, f);
		animatePaddle(boat, 1, this.rightPaddle, f);
	}

	public ImmutableList<ModelPart> parts() {
		return this.parts;
	}

	public ModelPart waterPatch() {
		return this.waterPatch;
	}

	private static void animatePaddle(Boat boat, int i, ModelPart modelPart, float f) {
		float g = boat.getRowingTime(i, f);
		modelPart.xRot = (float)Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (double)((Mth.sin(-g) + 1.0F) / 2.0F));
		modelPart.yRot = (float)Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (double)((Mth.sin(-g + 1.0F) + 1.0F) / 2.0F));
		if (i == 1) {
			modelPart.yRot = (float) Math.PI - modelPart.yRot;
		}
	}
}
