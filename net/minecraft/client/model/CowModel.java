/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class CowModel<T extends Entity>
extends QuadrupedModel<T> {
    public CowModel() {
        super(12, 0.0f);
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0f, -4.0f, -6.0f, 8, 8, 6, 0.0f);
        this.head.setPos(0.0f, 4.0f, -8.0f);
        this.head.texOffs(22, 0).addBox(-5.0f, -5.0f, -4.0f, 1, 3, 1, 0.0f);
        this.head.texOffs(22, 0).addBox(4.0f, -5.0f, -4.0f, 1, 3, 1, 0.0f);
        this.body = new ModelPart(this, 18, 4);
        this.body.addBox(-6.0f, -10.0f, -7.0f, 12, 18, 10, 0.0f);
        this.body.setPos(0.0f, 5.0f, 2.0f);
        this.body.texOffs(52, 0).addBox(-2.0f, 2.0f, -8.0f, 4, 6, 1);
        this.leg0.x -= 1.0f;
        this.leg1.x += 1.0f;
        this.leg0.z += 0.0f;
        this.leg1.z += 0.0f;
        this.leg2.x -= 1.0f;
        this.leg3.x += 1.0f;
        this.leg2.z -= 1.0f;
        this.leg3.z -= 1.0f;
        this.zHeadOffs += 2.0f;
    }

    public ModelPart getHead() {
        return this.head;
    }
}

