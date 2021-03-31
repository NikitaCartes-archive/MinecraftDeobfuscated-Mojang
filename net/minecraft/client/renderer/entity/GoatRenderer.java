/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.goat.Goat;

@Environment(value=EnvType.CLIENT)
public class GoatRenderer
extends MobRenderer<Goat, GoatModel<Goat>> {
    private static final ResourceLocation GOAT_LOCATION = new ResourceLocation("textures/entity/goat/goat.png");

    public GoatRenderer(EntityRendererProvider.Context context) {
        super(context, new GoatModel(context.bakeLayer(ModelLayers.GOAT)), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(Goat goat) {
        return GOAT_LOCATION;
    }
}

