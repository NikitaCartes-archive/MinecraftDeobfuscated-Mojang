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
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

@Environment(value=EnvType.CLIENT)
public class VillagerRenderer
extends MobRenderer<Villager, VillagerModel<Villager>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/villager/villager.png");

    public VillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
        this.addLayer(new CustomHeadLayer<Villager, VillagerModel<Villager>>(this, context.getModelSet()));
        this.addLayer(new VillagerProfessionLayer<Villager, VillagerModel<Villager>>(this, context.getResourceManager(), "villager"));
        this.addLayer(new CrossedArmsItemLayer<Villager, VillagerModel<Villager>>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Villager villager) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    protected void scale(Villager villager, PoseStack poseStack, float f) {
        float g = 0.9375f;
        if (villager.isBaby()) {
            g = (float)((double)g * 0.5);
            this.shadowRadius = 0.25f;
        } else {
            this.shadowRadius = 0.5f;
        }
        poseStack.scale(g, g, g);
    }
}

