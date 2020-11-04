/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AreaEffectCloud;

@Environment(value=EnvType.CLIENT)
public class AreaEffectCloudRenderer
extends EntityRenderer<AreaEffectCloud> {
    public AreaEffectCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(AreaEffectCloud areaEffectCloud) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

