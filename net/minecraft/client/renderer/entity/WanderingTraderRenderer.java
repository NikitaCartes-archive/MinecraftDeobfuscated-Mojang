/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.WanderingTrader;

@Environment(value=EnvType.CLIENT)
public class WanderingTraderRenderer
extends MobRenderer<WanderingTrader, VillagerModel<WanderingTrader>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/wandering_trader.png");

    public WanderingTraderRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.getLayer(ModelLayers.WANDERING_TRADER)), 0.5f);
        this.addLayer(new CustomHeadLayer<WanderingTrader, VillagerModel<WanderingTrader>>(this, context.getModelSet()));
        this.addLayer(new CrossedArmsItemLayer<WanderingTrader, VillagerModel<WanderingTrader>>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(WanderingTrader wanderingTrader) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    protected void scale(WanderingTrader wanderingTrader, PoseStack poseStack, float f) {
        float g = 0.9375f;
        poseStack.scale(0.9375f, 0.9375f, 0.9375f);
    }
}

