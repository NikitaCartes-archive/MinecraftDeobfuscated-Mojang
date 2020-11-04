/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.DolphinCarryingItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;

@Environment(value=EnvType.CLIENT)
public class DolphinRenderer
extends MobRenderer<Dolphin, DolphinModel<Dolphin>> {
    private static final ResourceLocation DOLPHIN_LOCATION = new ResourceLocation("textures/entity/dolphin.png");

    public DolphinRenderer(EntityRendererProvider.Context context) {
        super(context, new DolphinModel(context.getLayer(ModelLayers.DOLPHIN)), 0.7f);
        this.addLayer(new DolphinCarryingItemLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Dolphin dolphin) {
        return DOLPHIN_LOCATION;
    }
}

