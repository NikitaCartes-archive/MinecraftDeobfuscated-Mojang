package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;

@Environment(EnvType.CLIENT)
public class AnimationUtils {
	public static void animateCrossbowHold(ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, boolean bl) {
		modelPart.yRot = (bl ? -0.3F : 0.3F) + modelPart3.yRot;
		modelPart2.yRot = (bl ? 0.6F : -0.6F) + modelPart3.yRot;
		modelPart.xRot = (float) (-Math.PI / 2) + modelPart3.xRot + 0.1F;
		modelPart2.xRot = -1.5F + modelPart3.xRot;
	}

	public static void animateCrossbowCharge(ModelPart modelPart, ModelPart modelPart2, LivingEntity livingEntity, boolean bl) {
		modelPart.yRot = bl ? -0.8F : 0.8F;
		modelPart.xRot = -0.97079635F;
		modelPart2.xRot = modelPart.xRot;
		float f = (float)CrossbowItem.getChargeDuration(livingEntity.getUseItem());
		float g = Mth.clamp((float)livingEntity.getTicksUsingItem(), 0.0F, f);
		float h = g / f;
		modelPart2.yRot = Mth.lerp(h, 0.4F, 0.85F) * (float)(bl ? 1 : -1);
		modelPart2.xRot = Mth.lerp(h, modelPart2.xRot, (float) (-Math.PI / 2));
	}
}
