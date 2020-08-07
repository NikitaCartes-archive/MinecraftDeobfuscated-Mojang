package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class HumanoidModel<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
	public ModelPart head;
	public ModelPart hat;
	public ModelPart body;
	public ModelPart rightArm;
	public ModelPart leftArm;
	public ModelPart rightLeg;
	public ModelPart leftLeg;
	public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
	public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
	public boolean crouching;
	public float swimAmount;

	public HumanoidModel(float f) {
		this(RenderType::entityCutoutNoCull, f, 0.0F, 64, 32);
	}

	protected HumanoidModel(float f, float g, int i, int j) {
		this(RenderType::entityCutoutNoCull, f, g, i, j);
	}

	public HumanoidModel(Function<ResourceLocation, RenderType> function, float f, float g, int i, int j) {
		super(function, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
		this.texWidth = i;
		this.texHeight = j;
		this.head = new ModelPart(this, 0, 0);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, f);
		this.head.setPos(0.0F, 0.0F + g, 0.0F);
		this.hat = new ModelPart(this, 32, 0);
		this.hat.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, f + 0.5F);
		this.hat.setPos(0.0F, 0.0F + g, 0.0F);
		this.body = new ModelPart(this, 16, 16);
		this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, f);
		this.body.setPos(0.0F, 0.0F + g, 0.0F);
		this.rightArm = new ModelPart(this, 40, 16);
		this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.rightArm.setPos(-5.0F, 2.0F + g, 0.0F);
		this.leftArm = new ModelPart(this, 40, 16);
		this.leftArm.mirror = true;
		this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.leftArm.setPos(5.0F, 2.0F + g, 0.0F);
		this.rightLeg = new ModelPart(this, 0, 16);
		this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.rightLeg.setPos(-1.9F, 12.0F + g, 0.0F);
		this.leftLeg = new ModelPart(this, 0, 16);
		this.leftLeg.mirror = true;
		this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.leftLeg.setPos(1.9F, 12.0F + g, 0.0F);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
	}

	public void prepareMobModel(T livingEntity, float f, float g, float h) {
		this.swimAmount = livingEntity.getSwimAmount(h);
		super.prepareMobModel(livingEntity, f, g, h);
	}

	public void setupAnim(T livingEntity, float f, float g, float h, float i, float j) {
		boolean bl = livingEntity.getFallFlyingTicks() > 4;
		boolean bl2 = livingEntity.isVisuallySwimming();
		this.head.yRot = i * (float) (Math.PI / 180.0);
		if (bl) {
			this.head.xRot = (float) (-Math.PI / 4);
		} else if (this.swimAmount > 0.0F) {
			if (bl2) {
				this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (float) (-Math.PI / 4));
			} else {
				this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, j * (float) (Math.PI / 180.0));
			}
		} else {
			this.head.xRot = j * (float) (Math.PI / 180.0);
		}

		this.body.yRot = 0.0F;
		this.rightArm.z = 0.0F;
		this.rightArm.x = -5.0F;
		this.leftArm.z = 0.0F;
		this.leftArm.x = 5.0F;
		float k = 1.0F;
		if (bl) {
			k = (float)livingEntity.getDeltaMovement().lengthSqr();
			k /= 0.2F;
			k *= k * k;
		}

		if (k < 1.0F) {
			k = 1.0F;
		}

		this.rightArm.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 2.0F * g * 0.5F / k;
		this.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F / k;
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g / k;
		this.leftLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g / k;
		this.rightLeg.yRot = 0.0F;
		this.leftLeg.yRot = 0.0F;
		this.rightLeg.zRot = 0.0F;
		this.leftLeg.zRot = 0.0F;
		if (this.riding) {
			this.rightArm.xRot += (float) (-Math.PI / 5);
			this.leftArm.xRot += (float) (-Math.PI / 5);
			this.rightLeg.xRot = -1.4137167F;
			this.rightLeg.yRot = (float) (Math.PI / 10);
			this.rightLeg.zRot = 0.07853982F;
			this.leftLeg.xRot = -1.4137167F;
			this.leftLeg.yRot = (float) (-Math.PI / 10);
			this.leftLeg.zRot = -0.07853982F;
		}

		this.rightArm.yRot = 0.0F;
		this.leftArm.yRot = 0.0F;
		boolean bl3 = livingEntity.getMainArm() == HumanoidArm.RIGHT;
		boolean bl4 = bl3 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
		if (bl3 != bl4) {
			this.poseLeftArm(livingEntity);
			this.poseRightArm(livingEntity);
		} else {
			this.poseRightArm(livingEntity);
			this.poseLeftArm(livingEntity);
		}

		this.setupAttackAnimation(livingEntity, h);
		if (this.crouching) {
			this.body.xRot = 0.5F;
			this.rightArm.xRot += 0.4F;
			this.leftArm.xRot += 0.4F;
			this.rightLeg.z = 4.0F;
			this.leftLeg.z = 4.0F;
			this.rightLeg.y = 12.2F;
			this.leftLeg.y = 12.2F;
			this.head.y = 4.2F;
			this.body.y = 3.2F;
			this.leftArm.y = 5.2F;
			this.rightArm.y = 5.2F;
		} else {
			this.body.xRot = 0.0F;
			this.rightLeg.z = 0.1F;
			this.leftLeg.z = 0.1F;
			this.rightLeg.y = 12.0F;
			this.leftLeg.y = 12.0F;
			this.head.y = 0.0F;
			this.body.y = 0.0F;
			this.leftArm.y = 2.0F;
			this.rightArm.y = 2.0F;
		}

		AnimationUtils.bobArms(this.rightArm, this.leftArm, h);
		if (this.swimAmount > 0.0F) {
			float l = f % 26.0F;
			HumanoidArm humanoidArm = this.getAttackArm(livingEntity);
			float m = humanoidArm == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			float n = humanoidArm == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			if (l < 14.0F) {
				this.leftArm.xRot = this.rotlerpRad(n, this.leftArm.xRot, 0.0F);
				this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, 0.0F);
				this.leftArm.yRot = this.rotlerpRad(n, this.leftArm.yRot, (float) Math.PI);
				this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float) Math.PI);
				this.leftArm.zRot = this.rotlerpRad(n, this.leftArm.zRot, (float) Math.PI + 1.8707964F * this.quadraticArmUpdate(l) / this.quadraticArmUpdate(14.0F));
				this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, (float) Math.PI - 1.8707964F * this.quadraticArmUpdate(l) / this.quadraticArmUpdate(14.0F));
			} else if (l >= 14.0F && l < 22.0F) {
				float o = (l - 14.0F) / 8.0F;
				this.leftArm.xRot = this.rotlerpRad(n, this.leftArm.xRot, (float) (Math.PI / 2) * o);
				this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, (float) (Math.PI / 2) * o);
				this.leftArm.yRot = this.rotlerpRad(n, this.leftArm.yRot, (float) Math.PI);
				this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float) Math.PI);
				this.leftArm.zRot = this.rotlerpRad(n, this.leftArm.zRot, 5.012389F - 1.8707964F * o);
				this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, 1.2707963F + 1.8707964F * o);
			} else if (l >= 22.0F && l < 26.0F) {
				float o = (l - 22.0F) / 4.0F;
				this.leftArm.xRot = this.rotlerpRad(n, this.leftArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * o);
				this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * o);
				this.leftArm.yRot = this.rotlerpRad(n, this.leftArm.yRot, (float) Math.PI);
				this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float) Math.PI);
				this.leftArm.zRot = this.rotlerpRad(n, this.leftArm.zRot, (float) Math.PI);
				this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, (float) Math.PI);
			}

			float o = 0.3F;
			float p = 0.33333334F;
			this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(f * 0.33333334F + (float) Math.PI));
			this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(f * 0.33333334F));
		}

		this.hat.copyFrom(this.head);
	}

	private void poseRightArm(T livingEntity) {
		switch (this.rightArmPose) {
			case EMPTY:
				this.rightArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F;
				this.rightArm.yRot = (float) (-Math.PI / 6);
				break;
			case ITEM:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) (Math.PI / 10);
				this.rightArm.yRot = 0.0F;
				break;
			case THROW_SPEAR:
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) Math.PI;
				this.rightArm.yRot = 0.0F;
				break;
			case BOW_AND_ARROW:
				this.rightArm.yRot = -0.1F + this.head.yRot;
				this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
				this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
				this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
				break;
			case CROSSBOW_CHARGE:
				AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, livingEntity, true);
				break;
			case CROSSBOW_HOLD:
				AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
		}
	}

	private void poseLeftArm(T livingEntity) {
		switch (this.leftArmPose) {
			case EMPTY:
				this.leftArm.yRot = 0.0F;
				break;
			case BLOCK:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.9424779F;
				this.leftArm.yRot = (float) (Math.PI / 6);
				break;
			case ITEM:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) (Math.PI / 10);
				this.leftArm.yRot = 0.0F;
				break;
			case THROW_SPEAR:
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
				this.leftArm.yRot = 0.0F;
				break;
			case BOW_AND_ARROW:
				this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
				this.leftArm.yRot = 0.1F + this.head.yRot;
				this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
				this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
				break;
			case CROSSBOW_CHARGE:
				AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, livingEntity, false);
				break;
			case CROSSBOW_HOLD:
				AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
		}
	}

	protected void setupAttackAnimation(T livingEntity, float f) {
		if (!(this.attackTime <= 0.0F)) {
			HumanoidArm humanoidArm = this.getAttackArm(livingEntity);
			ModelPart modelPart = this.getArm(humanoidArm);
			float g = this.attackTime;
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
			g = 1.0F - this.attackTime;
			g *= g;
			g *= g;
			g = 1.0F - g;
			float h = Mth.sin(g * (float) Math.PI);
			float i = Mth.sin(this.attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
			modelPart.xRot = (float)((double)modelPart.xRot - ((double)h * 1.2 + (double)i));
			modelPart.yRot = modelPart.yRot + this.body.yRot * 2.0F;
			modelPart.zRot = modelPart.zRot + Mth.sin(this.attackTime * (float) Math.PI) * -0.4F;
		}
	}

	protected float rotlerpRad(float f, float g, float h) {
		float i = (h - g) % (float) (Math.PI * 2);
		if (i < (float) -Math.PI) {
			i += (float) (Math.PI * 2);
		}

		if (i >= (float) Math.PI) {
			i -= (float) (Math.PI * 2);
		}

		return g + f * i;
	}

	private float quadraticArmUpdate(float f) {
		return -65.0F * f + f * f;
	}

	public void copyPropertiesTo(HumanoidModel<T> humanoidModel) {
		super.copyPropertiesTo(humanoidModel);
		humanoidModel.leftArmPose = this.leftArmPose;
		humanoidModel.rightArmPose = this.rightArmPose;
		humanoidModel.crouching = this.crouching;
		humanoidModel.head.copyFrom(this.head);
		humanoidModel.hat.copyFrom(this.hat);
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
		this.getArm(humanoidArm).translateAndRotate(poseStack);
	}

	protected ModelPart getArm(HumanoidArm humanoidArm) {
		return humanoidArm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
	}

	@Override
	public ModelPart getHead() {
		return this.head;
	}

	protected HumanoidArm getAttackArm(T livingEntity) {
		HumanoidArm humanoidArm = livingEntity.getMainArm();
		return livingEntity.swingingArm == InteractionHand.MAIN_HAND ? humanoidArm : humanoidArm.getOpposite();
	}

	@Environment(EnvType.CLIENT)
	public static enum ArmPose {
		EMPTY(false),
		ITEM(false),
		BLOCK(false),
		BOW_AND_ARROW(true),
		THROW_SPEAR(false),
		CROSSBOW_CHARGE(true),
		CROSSBOW_HOLD(true);

		private final boolean twoHanded;

		private ArmPose(boolean bl) {
			this.twoHanded = bl;
		}

		public boolean isTwoHanded() {
			return this.twoHanded;
		}
	}
}
