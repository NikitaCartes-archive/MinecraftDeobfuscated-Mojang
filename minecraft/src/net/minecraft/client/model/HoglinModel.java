package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;

@Environment(EnvType.CLIENT)
public class HoglinModel<T extends Mob & HoglinBase> extends AgeableListModel<T> {
	private final ModelPart head;
	private final ModelPart rightEar;
	private final ModelPart leftEar;
	private final ModelPart body;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart mane;

	public HoglinModel(ModelPart modelPart) {
		super(true, 8.0F, 6.0F, 1.9F, 2.0F, 24.0F);
		this.body = modelPart.getChild("body");
		this.mane = this.body.getChild("mane");
		this.head = modelPart.getChild("head");
		this.rightEar = this.head.getChild("right_ear");
		this.leftEar = this.head.getChild("left_ear");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(1, 1).addBox(-8.0F, -7.0F, -13.0F, 16.0F, 14.0F, 26.0F), PartPose.offset(0.0F, 7.0F, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"mane",
			CubeListBuilder.create().texOffs(90, 33).addBox(0.0F, 0.0F, -9.0F, 0.0F, 10.0F, 19.0F, new CubeDeformation(0.001F)),
			PartPose.offset(0.0F, -14.0F, -5.0F)
		);
		PartDefinition partDefinition3 = partDefinition.addOrReplaceChild(
			"head",
			CubeListBuilder.create().texOffs(61, 1).addBox(-7.0F, -3.0F, -19.0F, 14.0F, 6.0F, 19.0F),
			PartPose.offsetAndRotation(0.0F, 2.0F, -12.0F, 0.87266463F, 0.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"right_ear",
			CubeListBuilder.create().texOffs(1, 1).addBox(-6.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F),
			PartPose.offsetAndRotation(-6.0F, -2.0F, -3.0F, 0.0F, 0.0F, (float) (-Math.PI * 2.0 / 9.0))
		);
		partDefinition3.addOrReplaceChild(
			"left_ear",
			CubeListBuilder.create().texOffs(1, 6).addBox(0.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F),
			PartPose.offsetAndRotation(6.0F, -2.0F, -3.0F, 0.0F, 0.0F, (float) (Math.PI * 2.0 / 9.0))
		);
		partDefinition3.addOrReplaceChild(
			"right_horn", CubeListBuilder.create().texOffs(10, 13).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(-7.0F, 2.0F, -12.0F)
		);
		partDefinition3.addOrReplaceChild(
			"left_horn", CubeListBuilder.create().texOffs(1, 13).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(7.0F, 2.0F, -12.0F)
		);
		int i = 14;
		int j = 11;
		partDefinition.addOrReplaceChild(
			"right_front_leg", CubeListBuilder.create().texOffs(66, 42).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F), PartPose.offset(-4.0F, 10.0F, -8.5F)
		);
		partDefinition.addOrReplaceChild(
			"left_front_leg", CubeListBuilder.create().texOffs(41, 42).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F), PartPose.offset(4.0F, 10.0F, -8.5F)
		);
		partDefinition.addOrReplaceChild(
			"right_hind_leg", CubeListBuilder.create().texOffs(21, 45).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F), PartPose.offset(-5.0F, 13.0F, 10.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_hind_leg", CubeListBuilder.create().texOffs(0, 45).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F), PartPose.offset(5.0F, 13.0F, 10.0F)
		);
		return LayerDefinition.create(meshDefinition, 128, 64);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.body, this.rightFrontLeg, this.leftFrontLeg, this.rightHindLeg, this.leftHindLeg);
	}

	public void setupAnim(T mob, float f, float g, float h, float i, float j) {
		this.rightEar.zRot = (float) (-Math.PI * 2.0 / 9.0) - g * Mth.sin(f);
		this.leftEar.zRot = (float) (Math.PI * 2.0 / 9.0) + g * Mth.sin(f);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		int k = mob.getAttackAnimationRemainingTicks();
		float l = 1.0F - (float)Mth.abs(10 - 2 * k) / 10.0F;
		this.head.xRot = Mth.lerp(l, 0.87266463F, (float) (-Math.PI / 9));
		if (mob.isBaby()) {
			this.head.y = Mth.lerp(l, 2.0F, 5.0F);
			this.mane.z = -3.0F;
		} else {
			this.head.y = 2.0F;
			this.mane.z = -7.0F;
		}

		float m = 1.2F;
		this.rightFrontLeg.xRot = Mth.cos(f) * 1.2F * g;
		this.leftFrontLeg.xRot = Mth.cos(f + (float) Math.PI) * 1.2F * g;
		this.rightHindLeg.xRot = this.leftFrontLeg.xRot;
		this.leftHindLeg.xRot = this.rightFrontLeg.xRot;
	}
}
