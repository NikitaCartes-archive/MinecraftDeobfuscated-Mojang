/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.PigSaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;

@Environment(value=EnvType.CLIENT)
public class PigRenderer
extends MobRenderer<Pig, PigModel<Pig>> {
    private static final ResourceLocation PIG_LOCATION = new ResourceLocation("textures/entity/pig/pig.png");

    public PigRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new PigModel(), 0.7f);
        this.addLayer(new PigSaddleLayer(this));
    }

    @Override
    protected ResourceLocation getTextureLocation(Pig pig) {
        return PIG_LOCATION;
    }
}

