/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Shulker;

@Environment(value=EnvType.CLIENT)
public class ShulkerModel<T extends Shulker>
extends ListModel<T> {
    private final ModelPart base;
    private final ModelPart lid = new ModelPart(64, 64, 0, 0);
    private final ModelPart head;

    public ShulkerModel() {
        super(RenderType::entityCutoutNoCull);
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
    public void setupAnim(T shulker, float f, float g, float h, float i, float j, float k) {
        float l = h - (float)((Shulker)shulker).tickCount;
        float m = (0.5f + ((Shulker)shulker).getClientPeekAmount(l)) * (float)Math.PI;
        float n = -1.0f + Mth.sin(m);
        float o = 0.0f;
        if (m > (float)Math.PI) {
            o = Mth.sin(h * 0.1f) * 0.7f;
        }
        this.lid.setPos(0.0f, 16.0f + Mth.sin(m) * 8.0f + o, 0.0f);
        this.lid.yRot = ((Shulker)shulker).getClientPeekAmount(l) > 0.3f ? n * n * n * n * (float)Math.PI * 0.125f : 0.0f;
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

