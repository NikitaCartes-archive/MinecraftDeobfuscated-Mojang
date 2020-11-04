package net.minecraft.client.model;

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
public class SpiderModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightMiddleHindLeg;
	private final ModelPart leftMiddleHindLeg;
	private final ModelPart rightMiddleFrontLeg;
	private final ModelPart leftMiddleFrontLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;

	public SpiderModel(ModelPart modelPart) {
		this.root = modelPart;
		this.head = modelPart.getChild("head");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.rightMiddleHindLeg = modelPart.getChild("right_middle_hind_leg");
		this.leftMiddleHindLeg = modelPart.getChild("left_middle_hind_leg");
		this.rightMiddleFrontLeg = modelPart.getChild("right_middle_front_leg");
		this.leftMiddleFrontLeg = modelPart.getChild("left_middle_front_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
	}

	public static LayerDefinition createSpiderBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		int i = 15;
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 15.0F, -3.0F)
		);
		partDefinition.addOrReplaceChild(
			"body0", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 15.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"body1", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F), PartPose.offset(0.0F, 15.0F, 9.0F)
		);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
		CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(18, 0).addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-4.0F, 15.0F, 2.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder2, PartPose.offset(4.0F, 15.0F, 2.0F));
		partDefinition.addOrReplaceChild("right_middle_hind_leg", cubeListBuilder, PartPose.offset(-4.0F, 15.0F, 1.0F));
		partDefinition.addOrReplaceChild("left_middle_hind_leg", cubeListBuilder2, PartPose.offset(4.0F, 15.0F, 1.0F));
		partDefinition.addOrReplaceChild("right_middle_front_leg", cubeListBuilder, PartPose.offset(-4.0F, 15.0F, 0.0F));
		partDefinition.addOrReplaceChild("left_middle_front_leg", cubeListBuilder2, PartPose.offset(4.0F, 15.0F, 0.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder, PartPose.offset(-4.0F, 15.0F, -1.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offset(4.0F, 15.0F, -1.0F));
		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.head.xRot = j * (float) (Math.PI / 180.0);
		float k = (float) (Math.PI / 4);
		this.rightHindLeg.zRot = (float) (-Math.PI / 4);
		this.leftHindLeg.zRot = (float) (Math.PI / 4);
		this.rightMiddleHindLeg.zRot = -0.58119464F;
		this.leftMiddleHindLeg.zRot = 0.58119464F;
		this.rightMiddleFrontLeg.zRot = -0.58119464F;
		this.leftMiddleFrontLeg.zRot = 0.58119464F;
		this.rightFrontLeg.zRot = (float) (-Math.PI / 4);
		this.leftFrontLeg.zRot = (float) (Math.PI / 4);
		float l = -0.0F;
		float m = (float) (Math.PI / 8);
		this.rightHindLeg.yRot = (float) (Math.PI / 4);
		this.leftHindLeg.yRot = (float) (-Math.PI / 4);
		this.rightMiddleHindLeg.yRot = (float) (Math.PI / 8);
		this.leftMiddleHindLeg.yRot = (float) (-Math.PI / 8);
		this.rightMiddleFrontLeg.yRot = (float) (-Math.PI / 8);
		this.leftMiddleFrontLeg.yRot = (float) (Math.PI / 8);
		this.rightFrontLeg.yRot = (float) (-Math.PI / 4);
		this.leftFrontLeg.yRot = (float) (Math.PI / 4);
		float n = -(Mth.cos(f * 0.6662F * 2.0F + 0.0F) * 0.4F) * g;
		float o = -(Mth.cos(f * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * g;
		float p = -(Mth.cos(f * 0.6662F * 2.0F + (float) (Math.PI / 2)) * 0.4F) * g;
		float q = -(Mth.cos(f * 0.6662F * 2.0F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * g;
		float r = Math.abs(Mth.sin(f * 0.6662F + 0.0F) * 0.4F) * g;
		float s = Math.abs(Mth.sin(f * 0.6662F + (float) Math.PI) * 0.4F) * g;
		float t = Math.abs(Mth.sin(f * 0.6662F + (float) (Math.PI / 2)) * 0.4F) * g;
		float u = Math.abs(Mth.sin(f * 0.6662F + (float) (Math.PI * 3.0 / 2.0)) * 0.4F) * g;
		this.rightHindLeg.yRot += n;
		this.leftHindLeg.yRot += -n;
		this.rightMiddleHindLeg.yRot += o;
		this.leftMiddleHindLeg.yRot += -o;
		this.rightMiddleFrontLeg.yRot += p;
		this.leftMiddleFrontLeg.yRot += -p;
		this.rightFrontLeg.yRot += q;
		this.leftFrontLeg.yRot += -q;
		this.rightHindLeg.zRot += r;
		this.leftHindLeg.zRot += -r;
		this.rightMiddleHindLeg.zRot += s;
		this.leftMiddleHindLeg.zRot += -s;
		this.rightMiddleFrontLeg.zRot += t;
		this.leftMiddleFrontLeg.zRot += -t;
		this.rightFrontLeg.zRot += u;
		this.leftFrontLeg.zRot += -u;
	}
}
