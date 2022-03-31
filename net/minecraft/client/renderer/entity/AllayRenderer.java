/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.allay.Allay;

@Environment(value=EnvType.CLIENT)
public class AllayRenderer
extends MobRenderer<Allay, AllayModel> {
    private static final ResourceLocation ALLAY_TEXTURE = new ResourceLocation("textures/entity/allay/allay.png");
    private static final int BRIGHTNESS_LEVEL_TRANSITION_DURATION = 60;
    private static final int MIN_BRIGHTNESS_LEVEL = 5;

    public AllayRenderer(EntityRendererProvider.Context context) {
        super(context, new AllayModel(context.bakeLayer(ModelLayers.ALLAY)), 0.4f);
        this.addLayer(new ItemInHandLayer<Allay, AllayModel>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Allay allay) {
        return ALLAY_TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(Allay allay, BlockPos blockPos) {
        long l = allay.getLevel().getGameTime() + (long)Math.abs(allay.getUUID().hashCode());
        float f = Math.abs(l % 120L - 60L);
        float g = f / 60.0f;
        return (int)Mth.lerp(g, 5.0f, 15.0f);
    }
}

