/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Blaze;

@Environment(value=EnvType.CLIENT)
public class BlazeRenderer
extends MobRenderer<Blaze, BlazeModel<Blaze>> {
    private static final ResourceLocation BLAZE_LOCATION = new ResourceLocation("textures/entity/blaze.png");

    public BlazeRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new BlazeModel(), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(Blaze blaze) {
        return BLAZE_LOCATION;
    }
}

