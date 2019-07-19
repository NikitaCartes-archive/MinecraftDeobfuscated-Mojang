/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public abstract class RenderLayer<T extends Entity, M extends EntityModel<T>> {
    private final RenderLayerParent<T, M> renderer;

    public RenderLayer(RenderLayerParent<T, M> renderLayerParent) {
        this.renderer = renderLayerParent;
    }

    public M getParentModel() {
        return this.renderer.getModel();
    }

    public void bindTexture(ResourceLocation resourceLocation) {
        this.renderer.bindTexture(resourceLocation);
    }

    public void setLightColor(T entity) {
        this.renderer.setLightColor(entity);
    }

    public abstract void render(T var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8);

    public abstract boolean colorsOnDamage();
}

