/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Frog;

@Environment(value=EnvType.CLIENT)
public class FrogRenderer
extends MobRenderer<Frog, FrogModel<Frog>> {
    private static final Map<Frog.Variant, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
        for (Frog.Variant variant : Frog.Variant.values()) {
            hashMap.put(variant, new ResourceLocation(String.format("textures/entity/frog/%s_frog.png", variant.getName())));
        }
    });

    public FrogRenderer(EntityRendererProvider.Context context) {
        super(context, new FrogModel(context.bakeLayer(ModelLayers.FROG)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(Frog frog) {
        return TEXTURE_BY_TYPE.get((Object)frog.getVariant());
    }
}

