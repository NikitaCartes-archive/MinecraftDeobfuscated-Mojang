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
import net.minecraft.world.entity.monster.Shulker;

@Environment(value=EnvType.CLIENT)
public class ShulkerModel<T extends Shulker>
extends ListModel<T> {
    private final ModelPart base;
    private final ModelPart lid = new ModelPart(64, 64, 0, 0);
    private final ModelPart head;

    public ShulkerModel() {
        this.base = new ModelPart(64, 64, 0, 28);
        this.head = new ModelPart(64, 64, 0, 52);
        this.lid.addBox(-8.0f, -16.0f, -8.0f, 16.0f, 12.0f, 16.0f);
        this.lid.setPos(0.0f, 24.0f, 0.0f);
        this.base.addBox(-8.0f, -8.0f, -8.0f, 16.0f, 8.0f, 16.0f);
        this.base.setPos(0.0f, 24.0f, 0.0f);
        this.head.addBox(-3.0f, 0.0f, -3.0f, 6.0f, 6.0f, 6.0f);
        this.head.setPos(0.0f, 12.0f, 0.0f);
    }

    @Override
    public void setupAnim(T shulker, float f, float g, float h, float i, float j) {
        float k = h - (float)((Shulker)shulker).tickCount;
        float l = (0.5f + ((Shulker)shulker).getClientPeekAmount(k)) * (float)Math.PI;
        float m = -1.0f + Mth.sin(l);
        float n = 0.0f;
        if (l > (float)Math.PI) {
            n = Mth.sin(h * 0.1f) * 0.7f;
        }
        this.lid.setPos(0.0f, 16.0f + Mth.sin(l) * 8.0f + n, 0.0f);
        this.lid.yRot = ((Shulker)shulker).getClientPeekAmount(k) > 0.3f ? m * m * m * m * (float)Math.PI * 0.125f : 0.0f;
        this.head.xRot = j * ((float)Math.PI / 180);
        this.head.yRot = i * ((float)Math.PI / 180);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.base, this.lid);
    }

    public ModelPart getBase() {
        return this.base;
    }

    public ModelPart getLid() {
        return this.lid;
    }

    public ModelPart getHead() {
        return this.head;
    }
}

