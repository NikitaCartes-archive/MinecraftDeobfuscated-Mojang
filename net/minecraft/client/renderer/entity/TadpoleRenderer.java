/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.TadpoleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Tadpole;

@Environment(value=EnvType.CLIENT)
public class TadpoleRenderer
extends MobRenderer<Tadpole, TadpoleModel<Tadpole>> {
    private static final ResourceLocation TADPOLE_TEXTURE = new ResourceLocation("textures/entity/tadpole/tadpole.png");

    public TadpoleRenderer(EntityRendererProvider.Context context) {
        super(context, new TadpoleModel(context.bakeLayer(ModelLayers.TADPOLE)), 0.14f);
    }

    @Override
    public ResourceLocation getTextureLocation(Tadpole tadpole) {
        return TADPOLE_TEXTURE;
    }
}

