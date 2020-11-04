/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.model.geom;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

@Environment(value=EnvType.CLIENT)
public class EntityModelSet
implements ResourceManagerReloadListener {
    private Map<ModelLayerLocation, ModelPart> roots = ImmutableMap.of();

    public ModelPart getLayer(ModelLayerLocation modelLayerLocation) {
        ModelPart modelPart = this.roots.get(modelLayerLocation);
        if (modelPart == null) {
            throw new IllegalArgumentException("No model for layer " + modelLayerLocation);
        }
        return modelPart;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.roots = ImmutableMap.copyOf(LayerDefinitions.createRoots());
    }
}

