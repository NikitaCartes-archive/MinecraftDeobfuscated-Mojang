/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class PigModel<T extends Entity>
extends QuadrupedModel<T> {
    public PigModel() {
        this(0.0f);
    }

    public PigModel(float f) {
        super(6, f);
        this.head.texOffs(16, 16).addBox(-2.0f, 0.0f, -9.0f, 4.0f, 3.0f, 1.0f, f);
        this.yHeadOffs = 4.0f;
    }
}

