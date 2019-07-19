/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public interface RenderLayerParent<T extends Entity, M extends EntityModel<T>> {
    public M getModel();

    public void bindTexture(ResourceLocation var1);

    public void setLightColor(T var1);
}

