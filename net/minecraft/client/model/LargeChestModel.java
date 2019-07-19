/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.ModelPart;

@Environment(value=EnvType.CLIENT)
public class LargeChestModel
extends ChestModel {
    public LargeChestModel() {
        this.lid = new ModelPart(this, 0, 0).setTexSize(128, 64);
        this.lid.addBox(0.0f, -5.0f, -14.0f, 30, 5, 14, 0.0f);
        this.lid.x = 1.0f;
        this.lid.y = 7.0f;
        this.lid.z = 15.0f;
        this.lock = new ModelPart(this, 0, 0).setTexSize(128, 64);
        this.lock.addBox(-1.0f, -2.0f, -15.0f, 2, 4, 1, 0.0f);
        this.lock.x = 16.0f;
        this.lock.y = 7.0f;
        this.lock.z = 15.0f;
        this.bottom = new ModelPart(this, 0, 19).setTexSize(128, 64);
        this.bottom.addBox(0.0f, 0.0f, 0.0f, 30, 10, 14, 0.0f);
        this.bottom.x = 1.0f;
        this.bottom.y = 6.0f;
        this.bottom.z = 1.0f;
    }
}

