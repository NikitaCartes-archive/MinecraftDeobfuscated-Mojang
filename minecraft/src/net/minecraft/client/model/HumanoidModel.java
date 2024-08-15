package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

@Environment(EnvType.CLIENT)
public class HumanoidModel<T extends HumanoidRenderState> extends EntityModel<T> implements ArmedModel, HeadedModel {
	public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F, Set.of("head"));
	public static final float OVERLAY_SCALE = 0.25F;
	public static final float HAT_OVERLAY_SCALE = 0.5F;
	public static final float LEGGINGS_OVERLAY_SCALE = -0.1F;
	private static final float DUCK_WALK_ROTATION = 0.005F;
	private static final float SPYGLASS_ARM_ROT_Y = (float) (Math.PI / 12);
	private static final float SPYGLASS_ARM_ROT_X = 1.9198622F;
	private static final float SPYGLASS_ARM_CROUCH_ROT_X = (float) (Math.PI / 12);
	private static final float HIGHEST_SHIELD_BLOCKING_ANGLE = (float) (-Math.PI * 4.0 / 9.0);
	private static final float LOWEST_SHIELD_BLOCKING_ANGLE = 0.43633232F;
	private static final float HORIZONTAL_SHIELD_MOVEMENT_LIMIT = (float) (Math.PI / 6);
	public static final float TOOT_HORN_XROT_BASE = 1.4835298F;
	public static final float TOOT_HORN_YROT_BASE = (float) (Math.PI / 6);
	private final ModelPart root;
	public final ModelPart head;
	public final ModelPart hat;
	public final ModelPart body;
	public final ModelPart rightArm;
	public final ModelPart leftArm;
	public final ModelPart rightLeg;
	public final ModelPart leftLeg;

	public HumanoidModel(ModelPart modelPart) {
		this(modelPart, RenderType::entityCutoutNoCull);
	}

	public HumanoidModel(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
		super(function);
		this.root = modelPart;
		this.head = modelPart.getChild("head");
		this.hat = this.head.getChild("hat");
		this.body = modelPart.getChild("body");
		this.rightArm = modelPart.getChild("right_arm");
		this.leftArm = modelPart.getChild("left_arm");
		this.rightLeg = modelPart.getChild("right_leg");
		this.leftLeg = modelPart.getChild("left_leg");
	}

	public static MeshDefinition createMesh(CubeDeformation cubeDeformation, float f) {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation), PartPose.offset(0.0F, 0.0F + f, 0.0F)
		);
		partDefinition2.addOrReplaceChild(
			"hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubeDeformation.extend(0.5F)), PartPose.ZERO
		);
		partDefinition.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation), PartPose.offset(0.0F, 0.0F + f, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_arm",
			CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation),
			PartPose.offset(-5.0F, 2.0F + f, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_arm",
			CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation),
			PartPose.offset(5.0F, 2.0F + f, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation), PartPose.offset(-1.9F, 12.0F + f, 0.0F)
		);
		partDefinition.addOrReplaceChild(
			"left_leg",
			CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation),
			PartPose.offset(1.9F, 12.0F + f, 0.0F)
		);
		return meshDefinition;
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	protected HumanoidModel.ArmPose getArmPose(T humanoidRenderState, HumanoidArm humanoidArm) {
		return HumanoidModel.ArmPose.EMPTY;
	}

	public void setupAnim(T humanoidRenderState) {
		HumanoidModel.ArmPose armPose = this.getArmPose(humanoidRenderState, HumanoidArm.LEFT);
		HumanoidModel.ArmPose armPose2 = this.getArmPose(humanoidRenderState, HumanoidArm.RIGHT);
		float f = humanoidRenderState.swimAmount;
		this.body.resetPose();
		this.head.resetPose();
		this.hat.resetPose();
		this.rightLeg.resetPose();
		this.leftLeg.resetPose();
		this.rightArm.resetPose();
		this.leftArm.resetPose();
		boolean bl = humanoidRenderState.isFallFlying;
		this.head.yRot = humanoidRenderState.yRot * (float) (Math.PI / 180.0);
		if (bl) {
			this.head.xRot = (float) (-Math.PI / 4);
		} else if (f > 0.0F) {
			if (humanoidRenderState.isSwimming) {
				this.head.xRot = Mth.rotLerpRad(f, this.head.xRot, (float) (-Math.PI / 4));
			} else {
				this.head.xRot = Mth.rotLerpRad(f, this.head.xRot, humanoidRenderState.xRot * (float) (Math.PI / 180.0));
			}
		} else {
			this.head.xRot = humanoidRenderState.xRot * (float) (Math.PI / 180.0);
		}

		float g = humanoidRenderState.walkAnimationPos;
		float h = humanoidRenderState.walkAnimationSpeed;
		this.rightArm.xRot = Mth.cos(g * 0.6662F + (float) Math.PI) * 2.0F * h * 0.5F / humanoidRenderState.speedValue;
		this.leftArm.xRot = Mth.cos(g * 0.6662F) * 2.0F * h * 0.5F / humanoidRenderState.speedValue;
		this.rightLeg.xRot = Mth.cos(g * 0.6662F) * 1.4F * h / humanoidRenderState.speedValue;
		this.leftLeg.xRot = Mth.cos(g * 0.6662F + (float) Math.PI) * 1.4F * h / humanoidRenderState.speedValue;
		this.rightLeg.yRot = 0.005F;
		this.leftLeg.yRot = -0.005F;
		this.rightLeg.zRot = 0.005F;
		this.leftLeg.zRot = -0.005F;
		if (humanoidRenderState.isPassenger) {
			this.rightArm.xRot += (float) (-Math.PI / 5);
			this.leftArm.xRot += (float) (-Math.PI / 5);
			this.rightLeg.xRot = -1.4137167F;
			this.rightLeg.yRot = (float) (Math.PI / 10);
			this.rightLeg.zRot = 0.07853982F;
			this.leftLeg.xRot = -1.4137167F;
			this.leftLeg.yRot = (float) (-Math.PI / 10);
			this.leftLeg.zRot = -0.07853982F;
		}

		boolean bl2 = humanoidRenderState.mainArm == HumanoidArm.RIGHT;
		if (humanoidRenderState.isUsingItem) {
			boolean bl3 = humanoidRenderState.useItemHand == InteractionHand.MAIN_HAND;
			if (bl3 == bl2) {
				this.poseRightArm(humanoidRenderState, armPose2);
			} else {
				this.poseLeftArm(humanoidRenderState, armPose);
			}
		} else {
			boolean bl3 = bl2 ? armPose.isTwoHanded() : armPose2.isTwoHanded();
			if (bl2 != bl3) {
				this.poseLeftArm(humanoidRenderState, armPose);
				this.poseRightArm(humanoidRenderState, armPose2);
			} else {
				this.poseRightArm(humanoidRenderState, armPose2);
				this.poseLeftArm(humanoidRenderState, armPose);
			}
		}

		this.setupAttackAnimation(humanoidRenderState, humanoidRenderState.ageInTicks);
		if (humanoidRenderState.isCrouching) {
			this.body.xRot = 0.5F;
			this.rightArm.xRot += 0.4F;
			this.leftArm.xRot += 0.4F;
			this.rightLeg.z += 4.0F;
			this.leftLeg.z += 4.0F;
			this.head.y += 4.2F;
			this.body.y += 3.2F;
			this.leftArm.y += 3.2F;
			this.rightArm.y += 3.2F;
		}

		if (armPose2 != HumanoidModel.ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(this.rightArm, humanoidRenderState.ageInTicks, 1.0F);
		}

		if (armPose != HumanoidModel.ArmPose.SPYGLASS) {
			AnimationUtils.bobModelPart(this.leftArm, humanoidRenderState.ageInTicks, -1.0F);
		}

		if (f > 0.0F) {
			float i = g % 26.0F;
			HumanoidArm humanoidArm = humanoidRenderState.attackArm;
			float j = humanoidArm == HumanoidArm.RIGHT && humanoidRenderState.attackTime > 0.0F ? 0.0F : f;
			float k = humanoidArm == HumanoidArm.LEFT && humanoidRenderState.attackTime > 0.0F ? 0.0F : f;
			if (!humanoidRenderState.isUsingItem) {
				if (i < 14.0F) {
					this.leftArm.xRot = Mth.rotLerpRad(k, this.leftArm.xRot, 0.0F);
					this.rightArm.xRot = Mth.lerp(j, this.rightArm.xRot, 0.0F);
					this.leftArm.yRot = Mth.rotLerpRad(k, this.leftArm.yRot, (float) Math.PI);
					this.rightArm.yRot = Mth.lerp(j, this.rightArm.yRot, (float) Math.PI);
					this.leftArm.zRot = Mth.rotLerpRad(k, this.leftArm.zRot, (float) Math.PI + 1.8707964F * this.quadraticArmUpdate(i) / this.quadraticArmUpdate(14.0F));
					this.rightArm.zRot = Mth.lerp(j, this.rightArm.zRot, (float) Math.PI - 1.8707964F * this.quadraticArmUpdate(i) / this.quadraticArmUpdate(14.0F));
				} else if (i >= 14.0F && i < 22.0F) {
					float l = (i - 14.0F) / 8.0F;
					this.leftArm.xRot = Mth.rotLerpRad(k, this.leftArm.xRot, (float) (Math.PI / 2) * l);
					this.rightArm.xRot = Mth.lerp(j, this.rightArm.xRot, (float) (Math.PI / 2) * l);
					this.leftArm.yRot = Mth.rotLerpRad(k, this.leftArm.yRot, (float) Math.PI);
					this.rightArm.yRot = Mth.lerp(j, this.rightArm.yRot, (float) Math.PI);
					this.leftArm.zRot = Mth.rotLerpRad(k, this.leftArm.zRot, 5.012389F - 1.8707964F * l);
					this.rightArm.zRot = Mth.lerp(j, this.rightArm.zRot, 1.2707963F + 1.8707964F * l);
				} else if (i >= 22.0F && i < 26.0F) {
					float l = (i - 22.0F) / 4.0F;
					this.leftArm.xRot = Mth.rotLerpRad(k, this.leftArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * l);
					this.rightArm.xRot = Mth.lerp(j, this.rightArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * l);
					this.leftArm.yRot = Mth.rotLerpRad(k, this.leftArm.yRot, (float) Math.PI);
					this.rightArm.yRot = Mth.lerp(j, this.rightArm.yRot, (float) Math.PI);
					this.leftArm.zRot = Mth.rotLerpRad(k, this.leftArm.zRot, (float) Math.PI);
					this.rightArm.zRot = Mth.lerp(j, this.rightArm.zRot, (float) Math.PI);
				}
			}

			float l = 0.3F;
			float m = 0.33333334F;
			this.leftLeg.xRot = Mth.lerp(f, this.leftLeg.xRot, 0.3F * Mth.cos(g * 0.33333334F + (float) Math.PI));
			this.rightLeg.xRot = Mth.lerp(f, this.rightLeg.xRot, 0.3F * Mth.cos(g * 0.33333334F));
		}
	}

	private void poseRightArm(T humanoidRenderState, HumanoidModel.ArmPose armPose) {
		switch (armPose) {
			case EMPTY:
				this.rightArm.yRot = 0.0F;
				break;
			case ITEM:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) (Math.PI / 10);
				this.rightArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.poseBlockingArm(this.rightArm, true);
				break;
			case BOW_AND_ARROW:
				this.rightArm.yRot = -0.1F + this.head.yRot;
				this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
				this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
				this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
				break;
			case THROW_SPEAR:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) Math.PI;
				this.rightArm.yRot = 0.0F;
				break;
			case CROSSBOW_CHARGE:
				AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, humanoidRenderState.maxCrossbowChargeDuration, humanoidRenderState.ticksUsingItem, true);
				break;
			case CROSSBOW_HOLD:
				AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
				break;
			case SPYGLASS:
				this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (humanoidRenderState.isCrouching ? (float) (Math.PI / 12) : 0.0F), -2.4F, 3.3F);
				this.rightArm.yRot = this.head.yRot - (float) (Math.PI / 12);
				break;
			case TOOT_HORN:
				this.rightArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
				this.rightArm.yRot = this.head.yRot - (float) (Math.PI / 6);
				break;
			case BRUSH:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) (Math.PI / 5);
				this.rightArm.yRot = 0.0F;
		}
	}

	private void poseLeftArm(T humanoidRenderState, HumanoidModel.ArmPose armPose) {
		switch (armPose) {
			case EMPTY:
				this.leftArm.yRot = 0.0F;
				break;
			case ITEM:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) (Math.PI / 10);
				this.leftArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.poseBlockingArm(this.leftArm, false);
				break;
			case BOW_AND_ARROW:
				this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
				this.leftArm.yRot = 0.1F + this.head.yRot;
				this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
				this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
				break;
			case THROW_SPEAR:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
				this.leftArm.yRot = 0.0F;
				break;
			case CROSSBOW_CHARGE:
				AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, humanoidRenderState.maxCrossbowChargeDuration, humanoidRenderState.ticksUsingItem, false);
				break;
			case CROSSBOW_HOLD:
				AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
				break;
			case SPYGLASS:
				this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (humanoidRenderState.isCrouching ? (float) (Math.PI / 12) : 0.0F), -2.4F, 3.3F);
				this.leftArm.yRot = this.head.yRot + (float) (Math.PI / 12);
				break;
			case TOOT_HORN:
				this.leftArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
				this.leftArm.yRot = this.head.yRot + (float) (Math.PI / 6);
				break;
			case BRUSH:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) (Math.PI / 5);
				this.leftArm.yRot = 0.0F;
		}
	}

	private void poseBlockingArm(ModelPart modelPart, boolean bl) {
		modelPart.xRot = modelPart.xRot * 0.5F - 0.9424779F + Mth.clamp(this.head.xRot, (float) (-Math.PI * 4.0 / 9.0), 0.43633232F);
		modelPart.yRot = (bl ? -30.0F : 30.0F) * (float) (Math.PI / 180.0) + Mth.clamp(this.head.yRot, (float) (-Math.PI / 6), (float) (Math.PI / 6));
	}

	protected void setupAttackAnimation(T humanoidRenderState, float f) {
		float g = humanoidRenderState.attackTime;
		if (!(g <= 0.0F)) {
			HumanoidArm humanoidArm = humanoidRenderState.attackArm;
			ModelPart modelPart = this.getArm(humanoidArm);
			this.body.yRot = Mth.sin(Mth.sqrt(g) * (float) (Math.PI * 2)) * 0.2F;
			if (humanoidArm == HumanoidArm.LEFT) {
				this.body.yRot *= -1.0F;
			}

			this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
			this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
			this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
			this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
			this.rightArm.yRot = this.rightArm.yRot + this.body.yRot;
			this.leftArm.yRot = this.leftArm.yRot + this.body.yRot;
			this.leftArm.xRot = this.leftArm.xRot + this.body.yRot;
			float h = 1.0F - g;
			h *= h;
			h *= h;
			h = 1.0F - h;
			float i = Mth.sin(h * (float) Math.PI);
			float j = Mth.sin(g * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
			modelPart.xRot -= i * 1.2F + j;
			modelPart.yRot = modelPart.yRot + this.body.yRot * 2.0F;
			modelPart.zRot = modelPart.zRot + Mth.sin(g * (float) Math.PI) * -0.4F;
		}
	}

	private float quadraticArmUpdate(float f) {
		return -65.0F * f + f * f;
	}

	public void copyPropertiesTo(HumanoidModel<T> humanoidModel) {
		humanoidModel.head.copyFrom(this.head);
		humanoidModel.body.copyFrom(this.body);
		humanoidModel.rightArm.copyFrom(this.rightArm);
		humanoidModel.leftArm.copyFrom(this.leftArm);
		humanoidModel.rightLeg.copyFrom(this.rightLeg);
		humanoidModel.leftLeg.copyFrom(this.leftLeg);
	}

	public void setAllVisible(boolean bl) {
		this.head.visible = bl;
		this.hat.visible = bl;
		this.body.visible = bl;
		this.rightArm.visible = bl;
		this.leftArm.visible = bl;
		this.rightLeg.visible = bl;
		this.leftLeg.visible = bl;
	}

	@Override
	public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
		this.root.translateAndRotate(poseStack);
		this.getArm(humanoidArm).translateAndRotate(poseStack);
	}

	protected ModelPart getArm(HumanoidArm humanoidArm) {
		return humanoidArm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
	}

	@Override
	public ModelPart getHead() {
		return this.head;
	}

	@Environment(EnvType.CLIENT)
	public static enum ArmPose {
		EMPTY(false),
		ITEM(false),
		BLOCK(false),
		BOW_AND_ARROW(true),
		THROW_SPEAR(false),
		CROSSBOW_CHARGE(true),
		CROSSBOW_HOLD(true),
		SPYGLASS(false),
		TOOT_HORN(false),
		BRUSH(false);

		private final boolean twoHanded;

		private ArmPose(final boolean bl) {
			this.twoHanded = bl;
		}

		public boolean isTwoHanded() {
			return this.twoHanded;
		}
	}
}
