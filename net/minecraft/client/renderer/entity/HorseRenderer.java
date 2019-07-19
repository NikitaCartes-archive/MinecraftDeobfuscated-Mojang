/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.texture.LayeredTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;

@Environment(value=EnvType.CLIENT)
public final class HorseRenderer
extends AbstractHorseRenderer<Horse, HorseModel<Horse>> {
    private static final Map<String, ResourceLocation> LAYERED_LOCATION_CACHE = Maps.newHashMap();

    public HorseRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new HorseModel(0.0f), 1.1f);
        this.addLayer(new HorseArmorLayer(this));
    }

    @Override
    protected ResourceLocation getTextureLocation(Horse horse) {
        String string = horse.getLayeredTextureHashName();
        ResourceLocation resourceLocation = LAYERED_LOCATION_CACHE.get(string);
        if (resourceLocation == null) {
            resourceLocation = new ResourceLocation(string);
            Minecraft.getInstance().getTextureManager().register(resourceLocation, new LayeredTexture(horse.getLayeredTextureLayers()));
            LAYERED_LOCATION_CACHE.put(string, resourceLocation);
        }
        return resourceLocation;
    }
}

