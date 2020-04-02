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
import net.minecraft.world.entity.monster.Zoglin;

@Environment(value=EnvType.CLIENT)
public class ZoglinRenderer
extends MobRenderer<Zoglin, HoglinModel<Zoglin>> {
    private static final ResourceLocation ZOGLIN_LOCATION = new ResourceLocation("textures/entity/hoglin/zoglin.png");

    public ZoglinRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new HoglinModel(), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(Zoglin zoglin) {
        return ZOGLIN_LOCATION;
    }
}

