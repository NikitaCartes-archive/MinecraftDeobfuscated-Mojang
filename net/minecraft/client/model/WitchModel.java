/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class WitchModel<T extends Entity>
extends VillagerModel<T> {
    private boolean holdingItem;
    private final ModelPart mole = new ModelPart(this).setTexSize(64, 128);

    public WitchModel(float f) {
        super(f, 64, 128);
        this.mole.setPos(0.0f, -2.0f, 0.0f);
        this.mole.texOffs(0, 0).addBox(0.0f, 3.0f, -6.75f, 1, 1, 1, -0.25f);
        this.nose.addChild(this.mole);
        this.head.removeChild(this.hat);
        this.hat = new ModelPart(this).setTexSize(64, 128);
        this.hat.setPos(-5.0f, -10.03125f, -5.0f);
        this.hat.texOffs(0, 64).addBox(0.0f, 0.0f, 0.0f, 10, 2, 10);
        this.head.addChild(this.hat);
        ModelPart modelPart = new ModelPart(this).setTexSize(64, 128);
        modelPart.setPos(1.75f, -4.0f, 2.0f);
        modelPart.texOffs(0, 76).addBox(0.0f, 0.0f, 0.0f, 7, 4, 7);
        modelPart.xRot = -0.05235988f;
        modelPart.zRot = 0.02617994f;
        this.hat.addChild(modelPart);
        ModelPart modelPart2 = new ModelPart(this).setTexSize(64, 128);
        modelPart2.setPos(1.75f, -4.0f, 2.0f);
        modelPart2.texOffs(0, 87).addBox(0.0f, 0.0f, 0.0f, 4, 4, 4);
        modelPart2.xRot = -0.10471976f;
        modelPart2.zRot = 0.05235988f;
        modelPart.addChild(modelPart2);
        ModelPart modelPart3 = new ModelPart(this).setTexSize(64, 128);
        modelPart3.setPos(1.75f, -2.0f, 2.0f);
        modelPart3.texOffs(0, 95).addBox(0.0f, 0.0f, 0.0f, 1, 2, 1, 0.25f);
        modelPart3.xRot = -0.20943952f;
        modelPart3.zRot = 0.10471976f;
        modelPart2.addChild(modelPart3);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(entity, f, g, h, i, j, k);
        this.nose.translateX = 0.0f;
        this.nose.translateY = 0.0f;
        this.nose.translateZ = 0.0f;
        float l = 0.01f * (float)(((Entity)entity).getId() % 10);
        this.nose.xRot = Mth.sin((float)((Entity)entity).tickCount * l) * 4.5f * ((float)Math.PI / 180);
        this.nose.yRot = 0.0f;
        this.nose.zRot = Mth.cos((float)((Entity)entity).tickCount * l) * 2.5f * ((float)Math.PI / 180);
        if (this.holdingItem) {
            this.nose.xRot = -0.9f;
            this.nose.translateZ = -0.09375f;
            this.nose.translateY = 0.1875f;
        }
    }

    public ModelPart getNose() {
        return this.nose;
    }

    public void setHoldingItem(boolean bl) {
        this.holdingItem = bl;
    }
}

