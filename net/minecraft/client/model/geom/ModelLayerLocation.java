/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.geom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public final class ModelLayerLocation {
    private final ResourceLocation model;
    private final String layer;

    public ModelLayerLocation(ResourceLocation resourceLocation, String string) {
        this.model = resourceLocation;
        this.layer = string;
    }

    public ResourceLocation getModel() {
        return this.model;
    }

    public String getLayer() {
        return this.layer;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ModelLayerLocation) {
            ModelLayerLocation modelLayerLocation = (ModelLayerLocation)object;
            return this.model.equals(modelLayerLocation.model) && this.layer.equals(modelLayerLocation.layer);
        }
        return false;
    }

    public int hashCode() {
        int i = this.model.hashCode();
        i = 31 * i + this.layer.hashCode();
        return i;
    }

    public String toString() {
        return this.model + "#" + this.layer;
    }
}

