/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ravager;

@Environment(value=EnvType.CLIENT)
public class RavagerRenderer
extends MobRenderer<Ravager, RavagerModel> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/ravager.png");

    public RavagerRenderer(EntityRendererProvider.Context context) {
        super(context, new RavagerModel(context.getLayer(ModelLayers.RAVAGER)), 1.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(Ravager ravager) {
        return TEXTURE_LOCATION;
    }
}

