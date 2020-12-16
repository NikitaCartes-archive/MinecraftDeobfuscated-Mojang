/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

@Environment(value=EnvType.CLIENT)
public class AxolotlRenderer
extends MobRenderer<Axolotl, AxolotlModel<Axolotl>> {
    private static final Map<Axolotl.Variant, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
        for (Axolotl.Variant variant : Axolotl.Variant.BY_ID) {
            hashMap.put(variant, new ResourceLocation(String.format("textures/entity/axolotl/axolotl_%s.png", variant.getName())));
        }
    });

    public AxolotlRenderer(EntityRendererProvider.Context context) {
        super(context, new AxolotlModel(context.bakeLayer(ModelLayers.AXOLOTL)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(Axolotl axolotl) {
        return TEXTURE_BY_TYPE.get((Object)axolotl.getVariant());
    }
}

