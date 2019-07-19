/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.dragon.DragonModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Environment(value=EnvType.CLIENT)
public class EnderDragonEyesLayer
extends RenderLayer<EnderDragon, DragonModel> {
    private static final ResourceLocation DRAGON_EYES_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");

    public EnderDragonEyesLayer(RenderLayerParent<EnderDragon, DragonModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(EnderDragon enderDragon, float f, float g, float h, float i, float j, float k, float l) {
        this.bindTexture(DRAGON_EYES_LOCATION);
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        GlStateManager.disableLighting();
        GlStateManager.depthFunc(514);
        int m = 61680;
        int n = 61680;
        boolean o = false;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 61680.0f, 0.0f);
        GlStateManager.enableLighting();
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        gameRenderer.resetFogColor(true);
        ((DragonModel)this.getParentModel()).render(enderDragon, f, g, i, j, k, l);
        gameRenderer.resetFogColor(false);
        this.setLightColor(enderDragon);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.depthFunc(515);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

