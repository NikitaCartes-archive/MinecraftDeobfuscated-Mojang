/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class EvokerFangsModel<T extends Entity>
extends ListModel<T> {
    private final ModelPart base = new ModelPart(this, 0, 0);
    private final ModelPart upperJaw;
    private final ModelPart lowerJaw;

    public EvokerFangsModel() {
        this.base.setPos(-5.0f, 22.0f, -5.0f);
        this.base.addBox(0.0f, 0.0f, 0.0f, 10.0f, 12.0f, 10.0f);
        this.upperJaw = new ModelPart(this, 40, 0);
        this.upperJaw.setPos(1.5f, 22.0f, -4.0f);
        this.upperJaw.addBox(0.0f, 0.0f, 0.0f, 4.0f, 14.0f, 8.0f);
        this.lowerJaw = new ModelPart(this, 40, 0);
        this.lowerJaw.setPos(-1.5f, 22.0f, 4.0f);
        this.lowerJaw.addBox(0.0f, 0.0f, 0.0f, 4.0f, 14.0f, 8.0f);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
        float k = f * 2.0f;
        if (k > 1.0f) {
            k = 1.0f;
        }
        k = 1.0f - k * k * k;
        this.upperJaw.zRot = (float)Math.PI - k * 0.35f * (float)Math.PI;
        this.lowerJaw.zRot = (float)Math.PI + k * 0.35f * (float)Math.PI;
        this.lowerJaw.yRot = (float)Math.PI;
        float l = (f + Mth.sin(f * 2.7f)) * 0.6f * 12.0f;
        this.lowerJaw.y = this.upperJaw.y = 24.0f - l;
        this.base.y = this.upperJaw.y;
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.base, this.upperJaw, this.lowerJaw);
    }
}

