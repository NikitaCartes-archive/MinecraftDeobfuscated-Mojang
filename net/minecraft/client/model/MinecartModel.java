/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class MinecartModel<T extends Entity>
extends EntityModel<T> {
    private final ModelPart[] cubes = new ModelPart[7];

    public MinecartModel() {
        this.cubes[0] = new ModelPart(this, 0, 10);
        this.cubes[1] = new ModelPart(this, 0, 0);
        this.cubes[2] = new ModelPart(this, 0, 0);
        this.cubes[3] = new ModelPart(this, 0, 0);
        this.cubes[4] = new ModelPart(this, 0, 0);
        this.cubes[5] = new ModelPart(this, 44, 10);
        int i = 20;
        int j = 8;
        int k = 16;
        int l = 4;
        this.cubes[0].addBox(-10.0f, -8.0f, -1.0f, 20, 16, 2, 0.0f);
        this.cubes[0].setPos(0.0f, 4.0f, 0.0f);
        this.cubes[5].addBox(-9.0f, -7.0f, -1.0f, 18, 14, 1, 0.0f);
        this.cubes[5].setPos(0.0f, 4.0f, 0.0f);
        this.cubes[1].addBox(-8.0f, -9.0f, -1.0f, 16, 8, 2, 0.0f);
        this.cubes[1].setPos(-9.0f, 4.0f, 0.0f);
        this.cubes[2].addBox(-8.0f, -9.0f, -1.0f, 16, 8, 2, 0.0f);
        this.cubes[2].setPos(9.0f, 4.0f, 0.0f);
        this.cubes[3].addBox(-8.0f, -9.0f, -1.0f, 16, 8, 2, 0.0f);
        this.cubes[3].setPos(0.0f, 4.0f, -7.0f);
        this.cubes[4].addBox(-8.0f, -9.0f, -1.0f, 16, 8, 2, 0.0f);
        this.cubes[4].setPos(0.0f, 4.0f, 7.0f);
        this.cubes[0].xRot = 1.5707964f;
        this.cubes[1].yRot = 4.712389f;
        this.cubes[2].yRot = 1.5707964f;
        this.cubes[3].yRot = (float)Math.PI;
        this.cubes[5].xRot = -1.5707964f;
    }

    @Override
    public void render(T entity, float f, float g, float h, float i, float j, float k) {
        this.cubes[5].y = 4.0f - h;
        for (int l = 0; l < 6; ++l) {
            this.cubes[l].render(k);
        }
    }
}

