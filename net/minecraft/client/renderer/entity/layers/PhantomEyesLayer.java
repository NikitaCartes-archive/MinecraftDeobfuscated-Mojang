/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(value=EnvType.CLIENT)
public class PhantomEyesLayer<T extends Entity>
extends EyesLayer<T, PhantomModel<T>> {
    private static final ResourceLocation PHANTOM_EYES_LOCATION = new ResourceLocation("textures/entity/phantom_eyes.png");

    public PhantomEyesLayer(RenderLayerParent<T, PhantomModel<T>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return PHANTOM_EYES_LOCATION;
    }
}

