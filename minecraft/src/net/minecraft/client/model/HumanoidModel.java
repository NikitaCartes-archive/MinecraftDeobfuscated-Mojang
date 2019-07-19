package net.minecraft.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ArmedModel;
import net.minecraft.client.renderer.entity.HeadedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;

@Environment(EnvType.CLIENT)
public class HumanoidModel<T extends LivingEntity> extends EntityModel<T> implements ArmedModel, HeadedModel {
	public ModelPart head;
	public ModelPart hat;
	public ModelPart body;
	public ModelPart rightArm;
	public ModelPart leftArm;
	public ModelPart rightLeg;
	public ModelPart leftLeg;
	public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
	public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
	public boolean sneaking;
	public float swimAmount;
	private float itemUseTicks;

	public HumanoidModel() {
		this(0.0F);
	}

	public HumanoidModel(float f) {
		this(f, 0.0F, 64, 32);
	}

	public HumanoidModel(float f, float g, int i, int j) {
		this.texWidth = i;
		this.texHeight = j;
		this.head = new ModelPart(this, 0, 0);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, f);
		this.head.setPos(0.0F, 0.0F + g, 0.0F);
		this.hat = new ModelPart(this, 32, 0);
		this.hat.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, f + 0.5F);
		this.hat.setPos(0.0F, 0.0F + g, 0.0F);
		this.body = new ModelPart(this, 16, 16);
		this.body.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, f);
		this.body.setPos(0.0F, 0.0F + g, 0.0F);
		this.rightArm = new ModelPart(this, 40, 16);
		this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, f);
		this.rightArm.setPos(-5.0F, 2.0F + g, 0.0F);
		this.leftArm = new ModelPart(this, 40, 16);
		this.leftArm.mirror = true;
		this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, f);
		this.leftArm.setPos(5.0F, 2.0F + g, 0.0F);
		this.rightLeg = new ModelPart(this, 0, 16);
		this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, f);
		this.rightLeg.setPos(-1.9F, 12.0F + g, 0.0F);
		this.leftLeg = new ModelPart(this, 0, 16);
		this.leftLeg.mirror = true;
		this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, f);
		this.leftLeg.setPos(1.9F, 12.0F + g, 0.0F);
	}

	public void render(T livingEntity, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(livingEntity, f, g, h, i, j, k);
		GlStateManager.pushMatrix();
		if (this.young) {
			float l = 2.0F;
			GlStateManager.scalef(0.75F, 0.75F, 0.75F);
			GlStateManager.translatef(0.0F, 16.0F * k, 0.0F);
			this.head.render(k);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scalef(0.5F, 0.5F, 0.5F);
			GlStateManager.translatef(0.0F, 24.0F * k, 0.0F);
			this.body.render(k);
			this.rightArm.render(k);
			this.leftArm.render(k);
			this.rightLeg.render(k);
			this.leftLeg.render(k);
			this.hat.render(k);
		} else {
			if (livingEntity.isVisuallySneaking()) {
				GlStateManager.translatef(0.0F, 0.2F, 0.0F);
			}

			this.head.render(k);
			this.body.render(k);
			this.rightArm.render(k);
			this.leftArm.render(k);
			this.rightLeg.render(k);
			this.leftLeg.render(k);
			this.hat.render(k);
		}

		GlStateManager.popMatrix();
	}

	public void prepareMobModel(T livingEntity, float f, float g, float h) {
		this.swimAmount = livingEntity.getSwimAmount(h);
		this.itemUseTicks = (float)livingEntity.getTicksUsingItem();
		super.prepareMobModel(livingEntity, f, g, h);
	}

	public void setupAnim(T livingEntity, float f, float g, float h, float i, float j, float k) {
		boolean bl = livingEntity.getFallFlyingTicks() > 4;
		boolean bl2 = livingEntity.isVisuallySwimming();
		this.head.yRot = i * (float) (Math.PI / 180.0);
		if (bl) {
			this.head.xRot = (float) (-Math.PI / 4);
		} else if (this.swimAmount > 0.0F) {
			if (bl2) {
				this.head.xRot = this.rotlerpRad(this.head.xRot, (float) (-Math.PI / 4), this.swimAmount);
			} else {
				this.head.xRot = this.rotlerpRad(this.head.xRot, j * (float) (Math.PI / 180.0), this.swimAmount);
			}
		} else {
			this.head.xRot = j * (float) (Math.PI / 180.0);
		}

		this.body.yRot = 0.0F;
		this.rightArm.z = 0.0F;
		this.rightArm.x = -5.0F;
		this.leftArm.z = 0.0F;
		this.leftArm.x = 5.0F;
		float l = 1.0F;
		if (bl) {
			l = (float)livingEntity.getDeltaMovement().lengthSqr();
			l /= 0.2F;
			l *= l * l;
		}

		if (l < 1.0F) {
			l = 1.0F;
		}

		this.rightArm.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 2.0F * g * 0.5F / l;
		this.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * g * 0.5F / l;
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * g / l;
		this.leftLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g / l;
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
		this.rightArm.zRot = 0.0F;
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
		}

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
		}

		if (this.leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR
			&& this.rightArmPose != HumanoidModel.ArmPose.BLOCK
			&& this.rightArmPose != HumanoidModel.ArmPose.THROW_SPEAR
			&& this.rightArmPose != HumanoidModel.ArmPose.BOW_AND_ARROW) {
			this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
			this.leftArm.yRot = 0.0F;
		}

		if (this.attackTime > 0.0F) {
			HumanoidArm humanoidArm = this.getAttackArm(livingEntity);
			ModelPart modelPart = this.getArm(humanoidArm);
			float m = this.attackTime;
			this.body.yRot = Mth.sin(Mth.sqrt(m) * (float) (Math.PI * 2)) * 0.2F;
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
			m = 1.0F - this.attackTime;
			m *= m;
			m *= m;
			m = 1.0F - m;
			float n = Mth.sin(m * (float) Math.PI);
			float o = Mth.sin(this.attackTime * (float) Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
			modelPart.xRot = (float)((double)modelPart.xRot - ((double)n * 1.2 + (double)o));
			modelPart.yRot = modelPart.yRot + this.body.yRot * 2.0F;
			modelPart.zRot = modelPart.zRot + Mth.sin(this.attackTime * (float) Math.PI) * -0.4F;
		}

		if (this.sneaking) {
			this.body.xRot = 0.5F;
			this.rightArm.xRot += 0.4F;
			this.leftArm.xRot += 0.4F;
			this.rightLeg.z = 4.0F;
			this.leftLeg.z = 4.0F;
			this.rightLeg.y = 9.0F;
			this.leftLeg.y = 9.0F;
			this.head.y = 1.0F;
		} else {
			this.body.xRot = 0.0F;
			this.rightLeg.z = 0.1F;
			this.leftLeg.z = 0.1F;
			this.rightLeg.y = 12.0F;
			this.leftLeg.y = 12.0F;
			this.head.y = 0.0F;
		}

		this.rightArm.zRot = this.rightArm.zRot + Mth.cos(h * 0.09F) * 0.05F + 0.05F;
		this.leftArm.zRot = this.leftArm.zRot - (Mth.cos(h * 0.09F) * 0.05F + 0.05F);
		this.rightArm.xRot = this.rightArm.xRot + Mth.sin(h * 0.067F) * 0.05F;
		this.leftArm.xRot = this.leftArm.xRot - Mth.sin(h * 0.067F) * 0.05F;
		if (this.rightArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW) {
			this.rightArm.yRot = -0.1F + this.head.yRot;
			this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
			this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
			this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
		} else if (this.leftArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW
			&& this.rightArmPose != HumanoidModel.ArmPose.THROW_SPEAR
			&& this.rightArmPose != HumanoidModel.ArmPose.BLOCK) {
			this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
			this.leftArm.yRot = 0.1F + this.head.yRot;
			this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
			this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
		}

		float p = (float)CrossbowItem.getChargeDuration(livingEntity.getUseItem());
		if (this.rightArmPose == HumanoidModel.ArmPose.CROSSBOW_CHARGE) {
			this.rightArm.yRot = -0.8F;
			this.rightArm.xRot = -0.97079635F;
			this.leftArm.xRot = -0.97079635F;
			float q = Mth.clamp(this.itemUseTicks, 0.0F, p);
			this.leftArm.yRot = Mth.lerp(q / p, 0.4F, 0.85F);
			this.leftArm.xRot = Mth.lerp(q / p, this.leftArm.xRot, (float) (-Math.PI / 2));
		} else if (this.leftArmPose == HumanoidModel.ArmPose.CROSSBOW_CHARGE) {
			this.leftArm.yRot = 0.8F;
			this.rightArm.xRot = -0.97079635F;
			this.leftArm.xRot = -0.97079635F;
			float q = Mth.clamp(this.itemUseTicks, 0.0F, p);
			this.rightArm.yRot = Mth.lerp(q / p, -0.4F, -0.85F);
			this.rightArm.xRot = Mth.lerp(q / p, this.rightArm.xRot, (float) (-Math.PI / 2));
		}

		if (this.rightArmPose == HumanoidModel.ArmPose.CROSSBOW_HOLD && this.attackTime <= 0.0F) {
			this.rightArm.yRot = -0.3F + this.head.yRot;
			this.leftArm.yRot = 0.6F + this.head.yRot;
			this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot + 0.1F;
			this.leftArm.xRot = -1.5F + this.head.xRot;
		} else if (this.leftArmPose == HumanoidModel.ArmPose.CROSSBOW_HOLD) {
			this.rightArm.yRot = -0.6F + this.head.yRot;
			this.leftArm.yRot = 0.3F + this.head.yRot;
			this.rightArm.xRot = -1.5F + this.head.xRot;
			this.leftArm.xRot = (float) (-Math.PI / 2) + this.head.xRot + 0.1F;
		}

		if (this.swimAmount > 0.0F) {
			float q = f % 26.0F;
			float m = this.attackTime > 0.0F ? 0.0F : this.swimAmount;
			if (q < 14.0F) {
				this.leftArm.xRot = this.rotlerpRad(this.leftArm.xRot, 0.0F, this.swimAmount);
				this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, 0.0F);
				this.leftArm.yRot = this.rotlerpRad(this.leftArm.yRot, (float) Math.PI, this.swimAmount);
				this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float) Math.PI);
				this.leftArm.zRot = this.rotlerpRad(
					this.leftArm.zRot, (float) Math.PI + 1.8707964F * this.quadraticArmUpdate(q) / this.quadraticArmUpdate(14.0F), this.swimAmount
				);
				this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, (float) Math.PI - 1.8707964F * this.quadraticArmUpdate(q) / this.quadraticArmUpdate(14.0F));
			} else if (q >= 14.0F && q < 22.0F) {
				float n = (q - 14.0F) / 8.0F;
				this.leftArm.xRot = this.rotlerpRad(this.leftArm.xRot, (float) (Math.PI / 2) * n, this.swimAmount);
				this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, (float) (Math.PI / 2) * n);
				this.leftArm.yRot = this.rotlerpRad(this.leftArm.yRot, (float) Math.PI, this.swimAmount);
				this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float) Math.PI);
				this.leftArm.zRot = this.rotlerpRad(this.leftArm.zRot, 5.012389F - 1.8707964F * n, this.swimAmount);
				this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, 1.2707963F + 1.8707964F * n);
			} else if (q >= 22.0F && q < 26.0F) {
				float n = (q - 22.0F) / 4.0F;
				this.leftArm.xRot = this.rotlerpRad(this.leftArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * n, this.swimAmount);
				this.rightArm.xRot = Mth.lerp(m, this.rightArm.xRot, (float) (Math.PI / 2) - (float) (Math.PI / 2) * n);
				this.leftArm.yRot = this.rotlerpRad(this.leftArm.yRot, (float) Math.PI, this.swimAmount);
				this.rightArm.yRot = Mth.lerp(m, this.rightArm.yRot, (float) Math.PI);
				this.leftArm.zRot = this.rotlerpRad(this.leftArm.zRot, (float) Math.PI, this.swimAmount);
				this.rightArm.zRot = Mth.lerp(m, this.rightArm.zRot, (float) Math.PI);
			}

			float n = 0.3F;
			float o = 0.33333334F;
			this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(f * 0.33333334F + (float) Math.PI));
			this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(f * 0.33333334F));
		}

		this.hat.copyFrom(this.head);
	}

	protected float rotlerpRad(float f, float g, float h) {
		float i = (g - f) % (float) (Math.PI * 2);
		if (i < (float) -Math.PI) {
			i += (float) (Math.PI * 2);
		}

		if (i >= (float) Math.PI) {
			i -= (float) (Math.PI * 2);
		}

		return f + h * i;
	}

	private float quadraticArmUpdate(float f) {
		return -65.0F * f + f * f;
	}

	public void copyPropertiesTo(HumanoidModel<T> humanoidModel) {
		super.copyPropertiesTo(humanoidModel);
		humanoidModel.leftArmPose = this.leftArmPose;
		humanoidModel.rightArmPose = this.rightArmPose;
		humanoidModel.sneaking = this.sneaking;
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
	public void translateToHand(float f, HumanoidArm humanoidArm) {
		this.getArm(humanoidArm).translateTo(f);
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
		EMPTY,
		ITEM,
		BLOCK,
		BOW_AND_ARROW,
		THROW_SPEAR,
		CROSSBOW_CHARGE,
		CROSSBOW_HOLD;
	}
}
