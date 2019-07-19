/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public abstract class EntityModel<T extends Entity>
extends Model {
    public float attackTime;
    public boolean riding;
    public boolean young = true;

    public void render(T entity, float f, float g, float h, float i, float j, float k) {
    }

    public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
    }

    public void prepareMobModel(T entity, float f, float g, float h) {
    }

    public void copyPropertiesTo(EntityModel<T> entityModel) {
        entityModel.attackTime = this.attackTime;
        entityModel.riding = this.riding;
        entityModel.young = this.young;
    }
}

