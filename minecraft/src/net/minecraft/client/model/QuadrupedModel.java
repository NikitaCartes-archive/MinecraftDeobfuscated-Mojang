package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class QuadrupedModel<T extends Entity> extends AgeableListModel<T> {
	protected final ModelPart head;
	protected final ModelPart body;
	protected final ModelPart rightHindLeg;
	protected final ModelPart leftHindLeg;
	protected final ModelPart rightFrontLeg;
	protected final ModelPart leftFrontLeg;

	protected QuadrupedModel(ModelPart modelPart, boolean bl, float f, float g, float h, float i, int j) {
		super(bl, f, g, h, i, (float)j);
		this.head = modelPart.getChild("head");
		this.body = modelPart.getChild("body");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
	}

	public static MeshDefinition createBodyMesh(int i, CubeDeformation cubeDeformation) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.offset(0.0F, (float)(18 - i), -6.0F)
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(28, 8).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, cubeDeformation),
			PartPose.offsetAndRotation(0.0F, (float)(17 - i), 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)i, 4.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-3.0F, (float)(24 - i), 7.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(3.0F, (float)(24 - i), 7.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-3.0F, (float)(24 - i), -5.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(3.0F, (float)(24 - i), -5.0F));
		return meshDefinition;
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
	}
}
