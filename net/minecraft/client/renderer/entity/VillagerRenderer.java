/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerTradeItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.entity.npc.Villager;

@Environment(value=EnvType.CLIENT)
public class VillagerRenderer
extends MobRenderer<Villager, VillagerModel<Villager>> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/villager/villager.png");

    public VillagerRenderer(EntityRenderDispatcher entityRenderDispatcher, ReloadableResourceManager reloadableResourceManager) {
        super(entityRenderDispatcher, new VillagerModel(0.0f), 0.5f);
        this.addLayer(new CustomHeadLayer<Villager, VillagerModel<Villager>>(this));
        this.addLayer(new VillagerProfessionLayer<Villager, VillagerModel<Villager>>(this, reloadableResourceManager, "villager"));
        this.addLayer(new VillagerTradeItemLayer<Villager>(this));
    }

    @Override
    protected ResourceLocation getTextureLocation(Villager villager) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    protected void scale(Villager villager, float f) {
        float g = 0.9375f;
        if (villager.isBaby()) {
            g = (float)((double)g * 0.5);
            this.shadowRadius = 0.25f;
        } else {
            this.shadowRadius = 0.5f;
        }
        GlStateManager.scalef(g, g, g);
    }
}

