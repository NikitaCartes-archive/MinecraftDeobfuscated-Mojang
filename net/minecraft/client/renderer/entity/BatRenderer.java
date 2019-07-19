/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;

@Environment(value=EnvType.CLIENT)
public class BatRenderer
extends MobRenderer<Bat, BatModel> {
    private static final ResourceLocation BAT_LOCATION = new ResourceLocation("textures/entity/bat.png");

    public BatRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new BatModel(), 0.25f);
    }

    @Override
    protected ResourceLocation getTextureLocation(Bat bat) {
        return BAT_LOCATION;
    }

    @Override
    protected void scale(Bat bat, float f) {
        GlStateManager.scalef(0.35f, 0.35f, 0.35f);
    }

    @Override
    protected void setupRotations(Bat bat, float f, float g, float h) {
        if (bat.isResting()) {
            GlStateManager.translatef(0.0f, -0.1f, 0.0f);
        } else {
            GlStateManager.translatef(0.0f, Mth.cos(f * 0.3f) * 0.1f, 0.0f);
        }
        super.setupRotations(bat, f, g, h);
    }
}

