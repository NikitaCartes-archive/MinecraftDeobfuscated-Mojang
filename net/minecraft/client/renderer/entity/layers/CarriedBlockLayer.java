/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class CarriedBlockLayer
extends RenderLayer<EnderMan, EndermanModel<EnderMan>> {
    public CarriedBlockLayer(RenderLayerParent<EnderMan, EndermanModel<EnderMan>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(EnderMan enderMan, float f, float g, float h, float i, float j, float k, float l) {
        BlockState blockState = enderMan.getCarriedBlock();
        if (blockState == null) {
            return;
        }
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0.0f, 0.6875f, -0.75f);
        GlStateManager.rotatef(20.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotatef(45.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.translatef(0.25f, 0.1875f, 0.25f);
        float m = 0.5f;
        GlStateManager.scalef(-0.5f, -0.5f, 0.5f);
        int n = enderMan.getLightColor();
        int o = n % 65536;
        int p = n / 65536;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, o, p);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, 1.0f);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

