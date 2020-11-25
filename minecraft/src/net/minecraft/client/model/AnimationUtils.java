package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;

@Environment(EnvType.CLIENT)
public class AnimationUtils {
	public static void animateCrossbowHold(ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, boolean bl) {
		ModelPart modelPart4 = bl ? modelPart : modelPart2;
		ModelPart modelPart5 = bl ? modelPart2 : modelPart;
		modelPart4.yRot = (bl ? -0.3F : 0.3F) + modelPart3.yRot;
		modelPart5.yRot = (bl ? 0.6F : -0.6F) + modelPart3.yRot;
		modelPart4.xRot = (float) (-Math.PI / 2) + modelPart3.xRot + 0.1F;
		modelPart5.xRot = -1.5F + modelPart3.xRot;
	}

	public static void animateCrossbowCharge(ModelPart modelPart, ModelPart modelPart2, LivingEntity livingEntity, boolean bl) {
		ModelPart modelPart3 = bl ? modelPart : modelPart2;
		ModelPart modelPart4 = bl ? modelPart2 : modelPart;
		modelPart3.yRot = bl ? -0.8F : 0.8F;
		modelPart3.xRot = -0.97079635F;
		modelPart4.xRot = modelPart3.xRot;
		float f = (float)CrossbowItem.getChargeDuration(livingEntity.getUseItem());
		float g = Mth.clamp((float)livingEntity.getTicksUsingItem(), 0.0F, f);
		float h = g / f;
		modelPart4.yRot = Mth.lerp(h, 0.4F, 0.85F) * (float)(bl ? 1 : -1);
		modelPart4.xRot = Mth.lerp(h, modelPart4.xRot, (float) (-Math.PI / 2));
	}

	public static <T extends Mob> void swingWeaponDown(ModelPart modelPart, ModelPart modelPart2, T mob, float f, float g) {
		float h = Mth.sin(f * (float) Math.PI);
		float i = Mth.sin((1.0F - (1.0F - f) * (1.0F - f)) * (float) Math.PI);
		modelPart.zRot = 0.0F;
		modelPart2.zRot = 0.0F;
		modelPart.yRot = (float) (Math.PI / 20);
		modelPart2.yRot = (float) (-Math.PI / 20);
		if (mob.getMainArm() == HumanoidArm.RIGHT) {
			modelPart.xRot = -1.8849558F + Mth.cos(g * 0.09F) * 0.15F;
			modelPart2.xRot = -0.0F + Mth.cos(g * 0.19F) * 0.5F;
			modelPart.xRot += h * 2.2F - i * 0.4F;
			modelPart2.xRot += h * 1.2F - i * 0.4F;
		} else {
			modelPart.xRot = -0.0F + Mth.cos(g * 0.19F) * 0.5F;
			modelPart2.xRot = -1.8849558F + Mth.cos(g * 0.09F) * 0.15F;
			modelPart.xRot += h * 1.2F - i * 0.4F;
			modelPart2.xRot += h * 2.2F - i * 0.4F;
		}

		bobArms(modelPart, modelPart2, g);
	}

	public static void bobModelPart(ModelPart modelPart, float f, float g) {
		modelPart.zRot = modelPart.zRot + g * (Mth.cos(f * 0.09F) * 0.05F + 0.05F);
		modelPart.xRot = modelPart.xRot + g * Mth.sin(f * 0.067F) * 0.05F;
	}

	public static void bobArms(ModelPart modelPart, ModelPart modelPart2, float f) {
		bobModelPart(modelPart, f, 1.0F);
		bobModelPart(modelPart2, f, -1.0F);
	}

	public static void animateZombieArms(ModelPart modelPart, ModelPart modelPart2, boolean bl, float f, float g) {
		float h = Mth.sin(f * (float) Math.PI);
		float i = Mth.sin((1.0F - (1.0F - f) * (1.0F - f)) * (float) Math.PI);
		modelPart2.zRot = 0.0F;
		modelPart.zRot = 0.0F;
		modelPart2.yRot = -(0.1F - h * 0.6F);
		modelPart.yRot = 0.1F - h * 0.6F;
		float j = (float) -Math.PI / (bl ? 1.5F : 2.25F);
		modelPart2.xRot = j;
		modelPart.xRot = j;
		modelPart2.xRot += h * 1.2F - i * 0.4F;
		modelPart.xRot += h * 1.2F - i * 0.4F;
		bobArms(modelPart2, modelPart, g);
	}
}
