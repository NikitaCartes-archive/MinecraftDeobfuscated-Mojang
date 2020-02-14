/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

@Environment(value=EnvType.CLIENT)
public class HoglinRenderer
extends MobRenderer<Hoglin, HoglinModel> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/hoglin/hoglin.png");

    public HoglinRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new HoglinModel(), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(Hoglin hoglin) {
        return TEXTURE_LOCATION;
    }
}

