/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;

@Environment(value=EnvType.CLIENT)
public class AnimationUtils {
    public static void animateCrossbowHold(ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3, boolean bl) {
        ModelPart modelPart4 = bl ? modelPart : modelPart2;
        ModelPart modelPart5 = bl ? modelPart2 : modelPart;
        modelPart4.yRot = (bl ? -0.3f : 0.3f) + modelPart3.yRot;
        modelPart5.yRot = (bl ? 0.6f : -0.6f) + modelPart3.yRot;
        modelPart4.xRot = -1.5707964f + modelPart3.xRot + 0.1f;
        modelPart5.xRot = -1.5f + modelPart3.xRot;
    }

    public static void animateCrossbowCharge(ModelPart modelPart, ModelPart modelPart2, LivingEntity livingEntity, boolean bl) {
        ModelPart modelPart3 = bl ? modelPart : modelPart2;
        ModelPart modelPart4 = bl ? modelPart2 : modelPart;
        modelPart3.yRot = bl ? -0.8f : 0.8f;
        modelPart4.xRot = modelPart3.xRot = -0.97079635f;
        float f = CrossbowItem.getChargeDuration(livingEntity.getUseItem());
        float g = Mth.clamp((float)livingEntity.getTicksUsingItem(), 0.0f, f);
        float h = g / f;
        modelPart4.yRot = Mth.lerp(h, 0.4f, 0.85f) * (float)(bl ? 1 : -1);
        modelPart4.xRot = Mth.lerp(h, modelPart4.xRot, -1.5707964f);
    }

    public static <T extends Mob> void swingWeaponDown(ModelPart modelPart, ModelPart modelPart2, T mob, float f, float g) {
        float h = Mth.sin(f * (float)Math.PI);
        float i = Mth.sin((1.0f - (1.0f - f) * (1.0f - f)) * (float)Math.PI);
        modelPart.zRot = 0.0f;
        modelPart2.zRot = 0.0f;
        modelPart.yRot = 0.15707964f;
        modelPart2.yRot = -0.15707964f;
        if (mob.getMainArm() == HumanoidArm.RIGHT) {
            modelPart.xRot = -1.8849558f + Mth.cos(g * 0.09f) * 0.15f;
            modelPart2.xRot = -0.0f + Mth.cos(g * 0.19f) * 0.5f;
            modelPart.xRot += h * 2.2f - i * 0.4f;
            modelPart2.xRot += h * 1.2f - i * 0.4f;
        } else {
            modelPart.xRot = -0.0f + Mth.cos(g * 0.19f) * 0.5f;
            modelPart2.xRot = -1.8849558f + Mth.cos(g * 0.09f) * 0.15f;
            modelPart.xRot += h * 1.2f - i * 0.4f;
            modelPart2.xRot += h * 2.2f - i * 0.4f;
        }
        AnimationUtils.bobArms(modelPart, modelPart2, g);
    }

    public static void bobArms(ModelPart modelPart, ModelPart modelPart2, float f) {
        modelPart.zRot += Mth.cos(f * 0.09f) * 0.05f + 0.05f;
        modelPart2.zRot -= Mth.cos(f * 0.09f) * 0.05f + 0.05f;
        modelPart.xRot += Mth.sin(f * 0.067f) * 0.05f;
        modelPart2.xRot -= Mth.sin(f * 0.067f) * 0.05f;
    }

    public static void animateZombieArms(ModelPart modelPart, ModelPart modelPart2, boolean bl, float f, float g) {
        float j;
        float h = Mth.sin(f * (float)Math.PI);
        float i = Mth.sin((1.0f - (1.0f - f) * (1.0f - f)) * (float)Math.PI);
        modelPart2.zRot = 0.0f;
        modelPart.zRot = 0.0f;
        modelPart2.yRot = -(0.1f - h * 0.6f);
        modelPart.yRot = 0.1f - h * 0.6f;
        modelPart2.xRot = j = (float)(-Math.PI) / (bl ? 1.5f : 2.25f);
        modelPart.xRot = j;
        modelPart2.xRot += h * 1.2f - i * 0.4f;
        modelPart.xRot += h * 1.2f - i * 0.4f;
        AnimationUtils.bobArms(modelPart2, modelPart, g);
    }

    public static float getSpyglassArmXRot(ModelPart modelPart) {
        return modelPart.xRot * 0.95f - 2.277655f;
    }
}

