package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
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
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Vex;

@Environment(EnvType.CLIENT)
public class VexModel extends HumanoidModel<Vex> {
	private final ModelPart leftWing;
	private final ModelPart rightWing;

	public VexModel(ModelPart modelPart) {
		super(modelPart);
		this.leftLeg.visible = false;
		this.hat.visible = false;
		this.rightWing = modelPart.getChild("right_wing");
		this.leftWing = modelPart.getChild("left_wing");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"right_leg", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -1.0F, -2.0F, 6.0F, 10.0F, 4.0F), PartPose.offset(-1.9F, 12.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 32).addBox(-20.0F, 0.0F, 0.0F, 20.0F, 12.0F, 1.0F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(0.0F, 0.0F, 0.0F, 20.0F, 12.0F, 1.0F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return Iterables.concat(super.bodyParts(), ImmutableList.of(this.rightWing, this.leftWing));
	}

	public void setupAnim(Vex vex, float f, float g, float h, float i, float j) {
		super.setupAnim(vex, f, g, h, i, j);
		if (vex.isCharging()) {
			if (vex.getMainHandItem().isEmpty()) {
				this.rightArm.xRot = (float) (Math.PI * 3.0 / 2.0);
				this.leftArm.xRot = (float) (Math.PI * 3.0 / 2.0);
			} else if (vex.getMainArm() == HumanoidArm.RIGHT) {
				this.rightArm.xRot = 3.7699115F;
			} else {
				this.leftArm.xRot = 3.7699115F;
			}
		}

		this.rightLeg.xRot += (float) (Math.PI / 5);
		this.rightWing.z = 2.0F;
		this.leftWing.z = 2.0F;
		this.rightWing.y = 1.0F;
		this.leftWing.y = 1.0F;
		this.rightWing.yRot = 0.47123894F + Mth.cos(h * 0.8F) * (float) Math.PI * 0.05F;
		this.leftWing.yRot = -this.rightWing.yRot;
		this.leftWing.zRot = -0.47123894F;
		this.leftWing.xRot = 0.47123894F;
		this.rightWing.xRot = 0.47123894F;
		this.rightWing.zRot = 0.47123894F;
	}
}
