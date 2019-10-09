/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

@Environment(value=EnvType.CLIENT)
public class ChestedHorseModel<T extends AbstractChestedHorse>
extends HorseModel<T> {
    private final ModelPart boxL = new ModelPart(this, 26, 21);
    private final ModelPart boxR;

    public ChestedHorseModel(float f) {
        super(RenderType::entitySolid, f);
        this.boxL.addBox(-4.0f, 0.0f, -2.0f, 8.0f, 8.0f, 3.0f);
        this.boxR = new ModelPart(this, 26, 21);
        this.boxR.addBox(-4.0f, 0.0f, -2.0f, 8.0f, 8.0f, 3.0f);
        this.boxL.yRot = -1.5707964f;
        this.boxR.yRot = 1.5707964f;
        this.boxL.setPos(6.0f, -8.0f, 0.0f);
        this.boxR.setPos(-6.0f, -8.0f, 0.0f);
        this.body.addChild(this.boxL);
        this.body.addChild(this.boxR);
    }

    @Override
    protected void addEarModels(ModelPart modelPart) {
        ModelPart modelPart2 = new ModelPart(this, 0, 12);
        modelPart2.addBox(-1.0f, -7.0f, 0.0f, 2.0f, 7.0f, 1.0f);
        modelPart2.setPos(1.25f, -10.0f, 4.0f);
        ModelPart modelPart3 = new ModelPart(this, 0, 12);
        modelPart3.addBox(-1.0f, -7.0f, 0.0f, 2.0f, 7.0f, 1.0f);
        modelPart3.setPos(-1.25f, -10.0f, 4.0f);
        modelPart2.xRot = 0.2617994f;
        modelPart2.zRot = 0.2617994f;
        modelPart3.xRot = 0.2617994f;
        modelPart3.zRot = -0.2617994f;
        modelPart.addChild(modelPart2);
        modelPart.addChild(modelPart3);
    }

    @Override
    public void setupAnim(T abstractChestedHorse, float f, float g, float h, float i, float j, float k) {
        super.setupAnim(abstractChestedHorse, f, g, h, i, j, k);
        if (((AbstractChestedHorse)abstractChestedHorse).hasChest()) {
            this.boxL.visible = true;
            this.boxR.visible = true;
        } else {
            this.boxL.visible = false;
            this.boxR.visible = false;
        }
    }
}

