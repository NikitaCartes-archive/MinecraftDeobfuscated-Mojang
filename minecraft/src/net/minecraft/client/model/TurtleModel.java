package net.minecraft.client.model;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.TurtleRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class TurtleModel extends QuadrupedModel<TurtleRenderState> {
	private static final String EGG_BELLY = "egg_belly";
	public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 120.0F, 0.0F, 9.0F, 6.0F, 120.0F, Set.of("head"));
	private final ModelPart eggBelly;

	public TurtleModel(ModelPart modelPart) {
		super(modelPart);
		this.eggBelly = modelPart.getChild("egg_belly");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(3, 0).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 5.0F, 6.0F), PartPose.offset(0.0F, 19.0F, -10.0F)
		);
		partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create()
				.texOffs(7, 37)
				.addBox("shell", -9.5F, 3.0F, -10.0F, 19.0F, 20.0F, 6.0F)
				.texOffs(31, 1)
				.addBox("belly", -5.5F, 3.0F, -13.0F, 11.0F, 18.0F, 3.0F),
			PartPose.offsetAndRotation(0.0F, 11.0F, -10.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"egg_belly",
			CubeListBuilder.create().texOffs(70, 33).addBox(-4.5F, 3.0F, -14.0F, 9.0F, 18.0F, 1.0F),
			PartPose.offsetAndRotation(0.0F, 11.0F, -10.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		int i = 1;
		partDefinition.addOrReplaceChild(
			"right_hind_leg", CubeListBuilder.create().texOffs(1, 23).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F), PartPose.offset(-3.5F, 22.0F, 11.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_hind_leg", CubeListBuilder.create().texOffs(1, 12).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F), PartPose.offset(3.5F, 22.0F, 11.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_front_leg", CubeListBuilder.create().texOffs(27, 30).addBox(-13.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F), PartPose.offset(-5.0F, 21.0F, -4.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_front_leg", CubeListBuilder.create().texOffs(27, 24).addBox(0.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F), PartPose.offset(5.0F, 21.0F, -4.0F)
		);
		return LayerDefinition.create(meshDefinition, 128, 64);
	}

	public void setupAnim(TurtleRenderState turtleRenderState) {
		super.setupAnim(turtleRenderState);
		float f = turtleRenderState.walkAnimationPos;
		float g = turtleRenderState.walkAnimationSpeed;
		if (turtleRenderState.isOnLand) {
			float h = turtleRenderState.isLayingEgg ? 4.0F : 1.0F;
			float i = turtleRenderState.isLayingEgg ? 2.0F : 1.0F;
			float j = f * 5.0F;
			float k = Mth.cos(h * j);
			float l = Mth.cos(j);
			this.rightFrontLeg.yRot = -k * 8.0F * g * i;
			this.leftFrontLeg.yRot = k * 8.0F * g * i;
			this.rightHindLeg.yRot = -l * 3.0F * g;
			this.leftHindLeg.yRot = l * 3.0F * g;
		} else {
			float h = 0.5F * g;
			float i = Mth.cos(f * 0.6662F * 0.6F) * h;
			this.rightHindLeg.xRot = i;
			this.leftHindLeg.xRot = -i;
			this.rightFrontLeg.zRot = -i;
			this.leftFrontLeg.zRot = i;
		}

		this.eggBelly.visible = turtleRenderState.hasEgg;
		if (this.eggBelly.visible) {
			this.root.y--;
		}
	}
}
