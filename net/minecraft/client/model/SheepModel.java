/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Sheep;

@Environment(value=EnvType.CLIENT)
public class SheepModel<T extends Sheep>
extends QuadrupedModel<T> {
    private float headXRot;

    public SheepModel() {
        super(12, 0.0f);
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-3.0f, -4.0f, -6.0f, 6, 6, 8, 0.0f);
        this.head.setPos(0.0f, 6.0f, -8.0f);
        this.body = new ModelPart(this, 28, 8);
        this.body.addBox(-4.0f, -10.0f, -7.0f, 8, 16, 6, 0.0f);
        this.body.setPos(0.0f, 5.0f, 2.0f);
    }

    @Override
    public void prepareMobModel(T sheep, float f, float g, float h) {
        super.prepareMobModel(sheep, f, g, h);
        this.head.y = 6.0f + ((Sheep)sheep).getHeadEatPositionScale(h) * 9.0f;
        this.headXRot = ((Sheep)sheep).getHeadEatAngleScale(h);
    }

    @Override
    public void setupAnim(T sheep, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(sheep, f, g, h, i, j, k);
        this.head.xRot = this.headXRot;
    }
}

