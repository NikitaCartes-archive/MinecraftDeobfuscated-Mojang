package net.minecraft.client.model;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class FoxModel extends EntityModel<FoxRenderState> {
	public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 8.0F, 3.35F, Set.of("head"));
	public final ModelPart head;
	private final ModelPart body;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart tail;
	private static final int LEG_SIZE = 6;
	private static final float HEAD_HEIGHT = 16.5F;
	private static final float LEG_POS = 17.5F;
	private float legMotionPos;

	public FoxModel(ModelPart modelPart) {
		super(modelPart);
		this.head = modelPart.getChild("head");
		this.body = modelPart.getChild("body");
		this.rightHindLeg = modelPart.getChild("right_hind_leg");
		this.leftHindLeg = modelPart.getChild("left_hind_leg");
		this.rightFrontLeg = modelPart.getChild("right_front_leg");
		this.leftFrontLeg = modelPart.getChild("left_front_leg");
		this.tail = this.body.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(1, 5).addBox(-3.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F), PartPose.offset(-1.0F, 16.5F, -3.0F)
		);
		partDefinition2.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(8, 1).addBox(-3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F), PartPose.ZERO);
		partDefinition2.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(15, 1).addBox(3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F), PartPose.ZERO);
		partDefinition2.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(6, 18).addBox(-1.0F, 2.01F, -8.0F, 4.0F, 2.0F, 3.0F), PartPose.ZERO);
		PartDefinition partDefinition3 = partDefinition.addOrReplaceChild(
			"body",
			CubeListBuilder.create().texOffs(24, 15).addBox(-3.0F, 3.999F, -3.5F, 6.0F, 11.0F, 6.0F),
			PartPose.offsetAndRotation(0.0F, 16.0F, -6.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
		);
		CubeDeformation cubeDeformation = new CubeDeformation(0.001F);
		CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(4, 24).addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, cubeDeformation);
		CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(13, 24).addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, cubeDeformation);
		partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder2, PartPose.offset(-5.0F, 17.5F, 7.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(-1.0F, 17.5F, 7.0F));
		partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-5.0F, 17.5F, 0.0F));
		partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder, PartPose.offset(-1.0F, 17.5F, 0.0F));
		partDefinition3.addOrReplaceChild(
			"tail",
			CubeListBuilder.create().texOffs(30, 0).addBox(2.0F, 0.0F, -1.0F, 4.0F, 9.0F, 5.0F),
			PartPose.offsetAndRotation(-4.0F, 15.0F, -1.0F, -0.05235988F, 0.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 48, 32);
	}

	public void setupAnim(FoxRenderState foxRenderState) {
		super.setupAnim(foxRenderState);
		float f = foxRenderState.walkAnimationSpeed;
		float g = foxRenderState.walkAnimationPos;
		this.rightHindLeg.xRot = Mth.cos(g * 0.6662F) * 1.4F * f;
		this.leftHindLeg.xRot = Mth.cos(g * 0.6662F + (float) Math.PI) * 1.4F * f;
		this.rightFrontLeg.xRot = Mth.cos(g * 0.6662F + (float) Math.PI) * 1.4F * f;
		this.leftFrontLeg.xRot = Mth.cos(g * 0.6662F) * 1.4F * f;
		this.head.zRot = foxRenderState.headRollAngle;
		this.rightHindLeg.visible = true;
		this.leftHindLeg.visible = true;
		this.rightFrontLeg.visible = true;
		this.leftFrontLeg.visible = true;
		float h = foxRenderState.ageScale;
		if (foxRenderState.isCrouching) {
			this.body.xRot += 0.10471976F;
			float i = foxRenderState.crouchAmount;
			this.body.y += i * h;
			this.head.y += i * h;
		} else if (foxRenderState.isSleeping) {
			this.body.zRot = (float) (-Math.PI / 2);
			this.body.y += 5.0F * h;
			this.tail.xRot = (float) (-Math.PI * 5.0 / 6.0);
			if (foxRenderState.isBaby) {
				this.tail.xRot = -2.1816616F;
				this.body.z += 2.0F;
			}

			this.head.x += 2.0F * h;
			this.head.y += 2.99F * h;
			this.head.yRot = (float) (-Math.PI * 2.0 / 3.0);
			this.head.zRot = 0.0F;
			this.rightHindLeg.visible = false;
			this.leftHindLeg.visible = false;
			this.rightFrontLeg.visible = false;
			this.leftFrontLeg.visible = false;
		} else if (foxRenderState.isSitting) {
			this.body.xRot = (float) (Math.PI / 6);
			this.body.y -= 7.0F * h;
			this.body.z += 3.0F * h;
			this.tail.xRot = (float) (Math.PI / 4);
			this.tail.z -= 1.0F * h;
			this.head.xRot = 0.0F;
			this.head.yRot = 0.0F;
			if (foxRenderState.isBaby) {
				this.head.y--;
				this.head.z -= 0.375F;
			} else {
				this.head.y -= 6.5F;
				this.head.z += 2.75F;
			}

			this.rightHindLeg.xRot = (float) (-Math.PI * 5.0 / 12.0);
			this.rightHindLeg.y += 4.0F * h;
			this.rightHindLeg.z -= 0.25F * h;
			this.leftHindLeg.xRot = (float) (-Math.PI * 5.0 / 12.0);
			this.leftHindLeg.y += 4.0F * h;
			this.leftHindLeg.z -= 0.25F * h;
			this.rightFrontLeg.xRot = (float) (-Math.PI / 12);
			this.leftFrontLeg.xRot = (float) (-Math.PI / 12);
		}

		if (!foxRenderState.isSleeping && !foxRenderState.isFaceplanted && !foxRenderState.isCrouching) {
			this.head.xRot = foxRenderState.xRot * (float) (Math.PI / 180.0);
			this.head.yRot = foxRenderState.yRot * (float) (Math.PI / 180.0);
		}

		if (foxRenderState.isSleeping) {
			this.head.xRot = 0.0F;
			this.head.yRot = (float) (-Math.PI * 2.0 / 3.0);
			this.head.zRot = Mth.cos(foxRenderState.ageInTicks * 0.027F) / 22.0F;
		}

		if (foxRenderState.isCrouching) {
			float i = Mth.cos(foxRenderState.ageInTicks) * 0.01F;
			this.body.yRot = i;
			this.rightHindLeg.zRot = i;
			this.leftHindLeg.zRot = i;
			this.rightFrontLeg.zRot = i / 2.0F;
			this.leftFrontLeg.zRot = i / 2.0F;
		}

		if (foxRenderState.isFaceplanted) {
			float i = 0.1F;
			this.legMotionPos += 0.67F;
			this.rightHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
			this.leftHindLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
			this.rightFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F + (float) Math.PI) * 0.1F;
			this.leftFrontLeg.xRot = Mth.cos(this.legMotionPos * 0.4662F) * 0.1F;
		}
	}
}
