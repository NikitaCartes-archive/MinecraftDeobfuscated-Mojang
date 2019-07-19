/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;

@Environment(value=EnvType.CLIENT)
public class PhantomRenderer
extends MobRenderer<Phantom, PhantomModel<Phantom>> {
    private static final ResourceLocation PHANTOM_LOCATION = new ResourceLocation("textures/entity/phantom.png");

    public PhantomRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new PhantomModel(), 0.75f);
        this.addLayer(new PhantomEyesLayer<Phantom>(this));
    }

    @Override
    protected ResourceLocation getTextureLocation(Phantom phantom) {
        return PHANTOM_LOCATION;
    }

    @Override
    protected void scale(Phantom phantom, float f) {
        int i = phantom.getPhantomSize();
        float g = 1.0f + 0.15f * (float)i;
        GlStateManager.scalef(g, g, g);
        GlStateManager.translatef(0.0f, 1.3125f, 0.1875f);
    }

    @Override
    protected void setupRotations(Phantom phantom, float f, float g, float h) {
        super.setupRotations(phantom, f, g, h);
        GlStateManager.rotatef(phantom.xRot, 1.0f, 0.0f, 0.0f);
    }
}

