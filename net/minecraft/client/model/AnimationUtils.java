/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;

@Environment(value=EnvType.CLIENT)
public class AnimationUtils {
    public static void animateCrossbowHold(ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, boolean bl) {
        modelPart.yRot = (bl ? -0.3f : 0.3f) + modelPart3.yRot;
        modelPart2.yRot = (bl ? 0.6f : -0.6f) + modelPart3.yRot;
        modelPart.xRot = -1.5707964f + modelPart3.xRot + 0.1f;
        modelPart2.xRot = -1.5f + modelPart3.xRot;
    }

    public static void animateCrossbowCharge(ModelPart modelPart, ModelPart modelPart2, LivingEntity livingEntity, boolean bl) {
        modelPart.yRot = bl ? -0.8f : 0.8f;
        modelPart2.xRot = modelPart.xRot = -0.97079635f;
        float f = CrossbowItem.getChargeDuration(livingEntity.getUseItem());
        float g = Mth.clamp((float)livingEntity.getTicksUsingItem(), 0.0f, f);
        float h = g / f;
        modelPart2.yRot = Mth.lerp(h, 0.4f, 0.85f) * (float)(bl ? 1 : -1);
        modelPart2.xRot = Mth.lerp(h, modelPart2.xRot, -1.5707964f);
    }
}

