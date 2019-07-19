/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public class HumanoidHeadModel
extends SkullModel {
    private final ModelPart hat = new ModelPart(this, 32, 0);

    public HumanoidHeadModel() {
        super(0, 0, 64, 64);
        this.hat.addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8, 0.25f);
        this.hat.setPos(0.0f, 0.0f, 0.0f);
    }

    @Override
    public void render(float f, float g, float h, float i, float j, float k) {
        super.render(f, g, h, i, j, k);
        this.hat.yRot = this.head.yRot;
        this.hat.xRot = this.head.xRot;
        this.hat.render(k);
    }
}

