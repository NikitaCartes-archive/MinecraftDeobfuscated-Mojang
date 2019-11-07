/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public abstract class EntityModel<T extends Entity>
extends Model {
    public float attackTime;
    public boolean riding;
    public boolean young = true;

    protected EntityModel() {
        this(RenderType::entityCutoutNoCull);
    }

    protected EntityModel(Function<ResourceLocation, RenderType> function) {
        super(function);
    }

    public abstract void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6);

    public void prepareMobModel(T entity, float f, float g, float h) {
    }

    public void copyPropertiesTo(EntityModel<T> entityModel) {
        entityModel.attackTime = this.attackTime;
        entityModel.riding = this.riding;
        entityModel.young = this.young;
    }
}

