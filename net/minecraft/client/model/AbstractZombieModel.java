/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractZombieModel<T extends Monster>
extends HumanoidModel<T> {
    protected AbstractZombieModel(float f, float g, int i, int j) {
        super(f, g, i, j);
    }

    @Override
    public void setupAnim(T monster, float f, float g, float h, float i, float j) {
        float m;
        super.setupAnim(monster, f, g, h, i, j);
        boolean bl = this.isAggressive(monster);
        float k = Mth.sin(this.attackTime * (float)Math.PI);
        float l = Mth.sin((1.0f - (1.0f - this.attackTime) * (1.0f - this.attackTime)) * (float)Math.PI);
        this.rightArm.zRot = 0.0f;
        this.leftArm.zRot = 0.0f;
        this.rightArm.yRot = -(0.1f - k * 0.6f);
        this.leftArm.yRot = 0.1f - k * 0.6f;
        this.rightArm.xRot = m = (float)(-Math.PI) / (bl ? 1.5f : 2.25f);
        this.leftArm.xRot = m;
        this.rightArm.xRot += k * 1.2f - l * 0.4f;
        this.leftArm.xRot += k * 1.2f - l * 0.4f;
        this.rightArm.zRot += Mth.cos(h * 0.09f) * 0.05f + 0.05f;
        this.leftArm.zRot -= Mth.cos(h * 0.09f) * 0.05f + 0.05f;
        this.rightArm.xRot += Mth.sin(h * 0.067f) * 0.05f;
        this.leftArm.xRot -= Mth.sin(h * 0.067f) * 0.05f;
    }

    public abstract boolean isAggressive(T var1);
}

