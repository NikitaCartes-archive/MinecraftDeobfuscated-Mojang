package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.definitions.WardenAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class WardenModel extends EntityModel<WardenRenderState> {
	private static final float DEFAULT_ARM_X_Y = 13.0F;
	private static final float DEFAULT_ARM_Z = 1.0F;
	protected final ModelPart bone;
	protected final ModelPart body;
	protected final ModelPart head;
	protected final ModelPart rightTendril;
	protected final ModelPart leftTendril;
	protected final ModelPart leftLeg;
	protected final ModelPart leftArm;
	protected final ModelPart leftRibcage;
	protected final ModelPart rightArm;
	protected final ModelPart rightLeg;
	protected final ModelPart rightRibcage;
	private final List<ModelPart> tendrilsLayerModelParts;
	private final List<ModelPart> heartLayerModelParts;
	private final List<ModelPart> bioluminescentLayerModelParts;
	private final List<ModelPart> pulsatingSpotsLayerModelParts;

	public WardenModel(ModelPart modelPart) {
		super(modelPart, RenderType::entityCutoutNoCull);
		this.bone = modelPart.getChild("bone");
		this.body = this.bone.getChild("body");
		this.head = this.body.getChild("head");
		this.rightLeg = this.bone.getChild("right_leg");
		this.leftLeg = this.bone.getChild("left_leg");
		this.rightArm = this.body.getChild("right_arm");
		this.leftArm = this.body.getChild("left_arm");
		this.rightTendril = this.head.getChild("right_tendril");
		this.leftTendril = this.head.getChild("left_tendril");
		this.rightRibcage = this.body.getChild("right_ribcage");
		this.leftRibcage = this.body.getChild("left_ribcage");
		this.tendrilsLayerModelParts = ImmutableList.of(this.leftTendril, this.rightTendril);
		this.heartLayerModelParts = ImmutableList.of(this.body);
		this.bioluminescentLayerModelParts = ImmutableList.of(this.head, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg);
		this.pulsatingSpotsLayerModelParts = ImmutableList.of(this.body, this.head, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -13.0F, -4.0F, 18.0F, 21.0F, 11.0F), PartPose.offset(0.0F, -21.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"right_ribcage", CubeListBuilder.create().texOffs(90, 11).addBox(-2.0F, -11.0F, -0.1F, 9.0F, 21.0F, 0.0F), PartPose.offset(-7.0F, -2.0F, -4.0F)
		);
		partDefinition3.addOrReplaceChild(
			"left_ribcage",
			CubeListBuilder.create().texOffs(90, 11).mirror().addBox(-7.0F, -11.0F, -0.1F, 9.0F, 21.0F, 0.0F).mirror(false),
			PartPose.offset(7.0F, -2.0F, -4.0F)
		);
		PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -16.0F, -5.0F, 16.0F, 16.0F, 10.0F), PartPose.offset(0.0F, -13.0F, 0.0F)
		);
		partDefinition4.addOrReplaceChild(
			"right_tendril", CubeListBuilder.create().texOffs(52, 32).addBox(-16.0F, -13.0F, 0.0F, 16.0F, 16.0F, 0.0F), PartPose.offset(-8.0F, -12.0F, 0.0F)
		);
		partDefinition4.addOrReplaceChild(
			"left_tendril", CubeListBuilder.create().texOffs(58, 0).addBox(0.0F, -13.0F, 0.0F, 16.0F, 16.0F, 0.0F), PartPose.offset(8.0F, -12.0F, 0.0F)
		);
		partDefinition3.addOrReplaceChild(
			"right_arm", CubeListBuilder.create().texOffs(44, 50).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 28.0F, 8.0F), PartPose.offset(-13.0F, -13.0F, 1.0F)
		);
		partDefinition3.addOrReplaceChild(
			"left_arm", CubeListBuilder.create().texOffs(0, 58).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 28.0F, 8.0F), PartPose.offset(13.0F, -13.0F, 1.0F)
		);
		partDefinition2.addOrReplaceChild(
			"right_leg", CubeListBuilder.create().texOffs(76, 48).addBox(-3.1F, 0.0F, -3.0F, 6.0F, 13.0F, 6.0F), PartPose.offset(-5.9F, -13.0F, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"left_leg", CubeListBuilder.create().texOffs(76, 76).addBox(-2.9F, 0.0F, -3.0F, 6.0F, 13.0F, 6.0F), PartPose.offset(5.9F, -13.0F, 0.0F)
		);
		return LayerDefinition.create(meshDefinition, 128, 128);
	}

	public void setupAnim(WardenRenderState wardenRenderState) {
		super.setupAnim(wardenRenderState);
		this.animateHeadLookTarget(wardenRenderState.yRot, wardenRenderState.xRot);
		this.animateWalk(wardenRenderState.walkAnimationPos, wardenRenderState.walkAnimationSpeed);
		this.animateIdlePose(wardenRenderState.ageInTicks);
		this.animateTendrils(wardenRenderState, wardenRenderState.ageInTicks);
		this.animate(wardenRenderState.attackAnimationState, WardenAnimation.WARDEN_ATTACK, wardenRenderState.ageInTicks);
		this.animate(wardenRenderState.sonicBoomAnimationState, WardenAnimation.WARDEN_SONIC_BOOM, wardenRenderState.ageInTicks);
		this.animate(wardenRenderState.diggingAnimationState, WardenAnimation.WARDEN_DIG, wardenRenderState.ageInTicks);
		this.animate(wardenRenderState.emergeAnimationState, WardenAnimation.WARDEN_EMERGE, wardenRenderState.ageInTicks);
		this.animate(wardenRenderState.roarAnimationState, WardenAnimation.WARDEN_ROAR, wardenRenderState.ageInTicks);
		this.animate(wardenRenderState.sniffAnimationState, WardenAnimation.WARDEN_SNIFF, wardenRenderState.ageInTicks);
	}

	private void animateHeadLookTarget(float f, float g) {
		this.head.xRot = g * (float) (Math.PI / 180.0);
		this.head.yRot = f * (float) (Math.PI / 180.0);
	}

	private void animateIdlePose(float f) {
		float g = f * 0.1F;
		float h = Mth.cos(g);
		float i = Mth.sin(g);
		this.head.zRot += 0.06F * h;
		this.head.xRot += 0.06F * i;
		this.body.zRot += 0.025F * i;
		this.body.xRot += 0.025F * h;
	}

	private void animateWalk(float f, float g) {
		float h = Math.min(0.5F, 3.0F * g);
		float i = f * 0.8662F;
		float j = Mth.cos(i);
		float k = Mth.sin(i);
		float l = Math.min(0.35F, h);
		this.head.zRot += 0.3F * k * h;
		this.head.xRot = this.head.xRot + 1.2F * Mth.cos(i + (float) (Math.PI / 2)) * l;
		this.body.zRot = 0.1F * k * h;
		this.body.xRot = 1.0F * j * l;
		this.leftLeg.xRot = 1.0F * j * h;
		this.rightLeg.xRot = 1.0F * Mth.cos(i + (float) Math.PI) * h;
		this.leftArm.xRot = -(0.8F * j * h);
		this.leftArm.zRot = 0.0F;
		this.rightArm.xRot = -(0.8F * k * h);
		this.rightArm.zRot = 0.0F;
		this.resetArmPoses();
	}

	private void resetArmPoses() {
		this.leftArm.yRot = 0.0F;
		this.leftArm.z = 1.0F;
		this.leftArm.x = 13.0F;
		this.leftArm.y = -13.0F;
		this.rightArm.yRot = 0.0F;
		this.rightArm.z = 1.0F;
		this.rightArm.x = -13.0F;
		this.rightArm.y = -13.0F;
	}

	private void animateTendrils(WardenRenderState wardenRenderState, float f) {
		float g = wardenRenderState.tendrilAnimation * (float)(Math.cos((double)f * 2.25) * Math.PI * 0.1F);
		this.leftTendril.xRot = g;
		this.rightTendril.xRot = -g;
	}

	public List<ModelPart> getTendrilsLayerModelParts(WardenRenderState wardenRenderState) {
		return this.tendrilsLayerModelParts;
	}

	public List<ModelPart> getHeartLayerModelParts(WardenRenderState wardenRenderState) {
		return this.heartLayerModelParts;
	}

	public List<ModelPart> getBioluminescentLayerModelParts(WardenRenderState wardenRenderState) {
		return this.bioluminescentLayerModelParts;
	}

	public List<ModelPart> getPulsatingSpotsLayerModelParts(WardenRenderState wardenRenderState) {
		return this.pulsatingSpotsLayerModelParts;
	}
}
