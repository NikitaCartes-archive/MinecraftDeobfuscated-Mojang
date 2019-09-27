/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.MushroomCowMushroomLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MushroomCow;

@Environment(value=EnvType.CLIENT)
public class MushroomCowRenderer
extends MobRenderer<MushroomCow, CowModel<MushroomCow>> {
    private static final Map<MushroomCow.MushroomType, ResourceLocation> TEXTURES = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(MushroomCow.MushroomType.BROWN, new ResourceLocation("textures/entity/cow/brown_mooshroom.png"));
        hashMap.put(MushroomCow.MushroomType.RED, new ResourceLocation("textures/entity/cow/red_mooshroom.png"));
    });

    public MushroomCowRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new CowModel(), 0.7f);
        this.addLayer(new MushroomCowMushroomLayer<MushroomCow>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MushroomCow mushroomCow) {
        return TEXTURES.get((Object)mushroomCow.getMushroomType());
    }
}

