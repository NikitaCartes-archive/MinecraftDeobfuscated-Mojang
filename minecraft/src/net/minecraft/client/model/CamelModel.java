package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.definitions.CamelAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.CamelRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class CamelModel extends EntityModel<CamelRenderState> {
	private static final float MAX_WALK_ANIMATION_SPEED = 2.0F;
	private static final float WALK_ANIMATION_SCALE_FACTOR = 2.5F;
	public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.45F);
	private static final String SADDLE = "saddle";
	private static final String BRIDLE = "bridle";
	private static final String REINS = "reins";
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart[] saddleParts;
	private final ModelPart[] ridingParts;

	public CamelModel(ModelPart modelPart) {
		this.root = modelPart;
		ModelPart modelPart2 = modelPart.getChild("body");
		this.head = modelPart2.getChild("head");
		this.saddleParts = new ModelPart[]{modelPart2.getChild("saddle"), this.head.getChild("bridle")};
		this.ridingParts = new ModelPart[]{this.head.getChild("reins")};
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		CubeDeformation cubeDeformation = new CubeDeformation(0.05F);
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 25).addBox(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F), PartPose.offset(0.0F, 4.0F, 9.5F)
		);
		partDefinition2.addOrReplaceChild(
			"hump", CubeListBuilder.create().texOffs(74, 0).addBox(-4.5F, -5.0F, -5.5F, 9.0F, 5.0F, 11.0F), PartPose.offset(0.0F, -12.0F, -10.0F)
		);
		partDefinition2.addOrReplaceChild(
			"tail", CubeListBuilder.create().texOffs(122, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 0.0F), PartPose.offset(0.0F, -9.0F, 3.5F)
		);
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(60, 24)
				.addBox(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F)
				.texOffs(21, 0)
				.addBox(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F)
				.texOffs(50, 0)
				.addBox(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F),
			PartPose.offset(0.0F, -3.0F, -19.5F)
		);
		partDefinition3.addOrReplaceChild(
			"left_ear", CubeListBuilder.create().texOffs(45, 0).addBox(-0.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(2.5F, -21.0F, -9.5F)
		);
		partDefinition3.addOrReplaceChild(
			"right_ear", CubeListBuilder.create().texOffs(67, 0).addBox(-2.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(-2.5F, -21.0F, -9.5F)
		);
		partDefinition.addOrReplaceChild(
			"left_hind_leg", CubeListBuilder.create().texOffs(58, 16).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(4.9F, 1.0F, 9.5F)
		);
		partDefinition.addOrReplaceChild(
			"right_hind_leg", CubeListBuilder.create().texOffs(94, 16).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(-4.9F, 1.0F, 9.5F)
		);
		partDefinition.addOrReplaceChild(
			"left_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(4.9F, 1.0F, -10.5F)
		);
		partDefinition.addOrReplaceChild(
			"right_front_leg", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(-4.9F, 1.0F, -10.5F)
		);
		partDefinition2.addOrReplaceChild(
			"saddle",
			CubeListBuilder.create()
				.texOffs(74, 64)
				.addBox(-4.5F, -17.0F, -15.5F, 9.0F, 5.0F, 11.0F, cubeDeformation)
				.texOffs(92, 114)
				.addBox(-3.5F, -20.0F, -15.5F, 7.0F, 3.0F, 11.0F, cubeDeformation)
				.texOffs(0, 89)
				.addBox(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F, cubeDeformation),
			PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"reins",
			CubeListBuilder.create()
				.texOffs(98, 42)
				.addBox(3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F)
				.texOffs(84, 57)
				.addBox(-3.5F, -18.0F, -2.0F, 7.0F, 7.0F, 0.0F)
				.texOffs(98, 42)
				.addBox(-3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F),
			PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"bridle",
			CubeListBuilder.create()
				.texOffs(60, 87)
				.addBox(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F, cubeDeformation)
				.texOffs(21, 64)
				.addBox(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F, cubeDeformation)
				.texOffs(50, 64)
				.addBox(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F, cubeDeformation)
				.texOffs(74, 70)
				.addBox(2.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F)
				.texOffs(74, 70)
				.mirror()
				.addBox(-3.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F),
			PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 128, 128);
	}

	public void setupAnim(CamelRenderState camelRenderState) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(camelRenderState, camelRenderState.yRot, camelRenderState.xRot);
		this.toggleInvisibleParts(camelRenderState);
		this.animateWalk(CamelAnimation.CAMEL_WALK, camelRenderState.walkAnimationPos, camelRenderState.walkAnimationSpeed, 2.0F, 2.5F);
		this.animate(camelRenderState.sitAnimationState, CamelAnimation.CAMEL_SIT, camelRenderState.ageInTicks, 1.0F);
		this.animate(camelRenderState.sitPoseAnimationState, CamelAnimation.CAMEL_SIT_POSE, camelRenderState.ageInTicks, 1.0F);
		this.animate(camelRenderState.sitUpAnimationState, CamelAnimation.CAMEL_STANDUP, camelRenderState.ageInTicks, 1.0F);
		this.animate(camelRenderState.idleAnimationState, CamelAnimation.CAMEL_IDLE, camelRenderState.ageInTicks, 1.0F);
		this.animate(camelRenderState.dashAnimationState, CamelAnimation.CAMEL_DASH, camelRenderState.ageInTicks, 1.0F);
	}

	private void applyHeadRotation(CamelRenderState camelRenderState, float f, float g) {
		f = Mth.clamp(f, -30.0F, 30.0F);
		g = Mth.clamp(g, -25.0F, 45.0F);
		if (camelRenderState.jumpCooldown > 0.0F) {
			float h = 45.0F * camelRenderState.jumpCooldown / 55.0F;
			g = Mth.clamp(g + h, -25.0F, 70.0F);
		}

		this.head.yRot = f * (float) (Math.PI / 180.0);
		this.head.xRot = g * (float) (Math.PI / 180.0);
	}

	private void toggleInvisibleParts(CamelRenderState camelRenderState) {
		boolean bl = camelRenderState.isSaddled;
		boolean bl2 = camelRenderState.isRidden;

		for (ModelPart modelPart : this.saddleParts) {
			modelPart.visible = bl;
		}

		for (ModelPart modelPart : this.ridingParts) {
			modelPart.visible = bl2 && bl;
		}
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
