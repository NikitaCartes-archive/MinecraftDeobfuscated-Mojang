/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class MushroomCowMushroomLayer<T extends MushroomCow>
extends RenderLayer<T, CowModel<T>> {
    public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(T mushroomCow, float f, float g, float h, float i, float j, float k, float l) {
        if (((AgableMob)mushroomCow).isBaby() || ((Entity)mushroomCow).isInvisible()) {
            return;
        }
        BlockState blockState = ((MushroomCow)mushroomCow).getMushroomType().getBlockState();
        this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        GlStateManager.pushMatrix();
        GlStateManager.scalef(1.0f, -1.0f, 1.0f);
        GlStateManager.translatef(0.2f, 0.35f, 0.5f);
        GlStateManager.rotatef(42.0f, 0.0f, 1.0f, 0.0f);
        BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        GlStateManager.pushMatrix();
        GlStateManager.translatef(-0.5f, -0.5f, 0.5f);
        blockRenderDispatcher.renderSingleBlock(blockState, 1.0f);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0.1f, 0.0f, -0.6f);
        GlStateManager.rotatef(42.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.translatef(-0.5f, -0.5f, 0.5f);
        blockRenderDispatcher.renderSingleBlock(blockState, 1.0f);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        ((CowModel)this.getParentModel()).getHead().translateTo(0.0625f);
        GlStateManager.scalef(1.0f, -1.0f, 1.0f);
        GlStateManager.translatef(0.0f, 0.7f, -0.2f);
        GlStateManager.rotatef(12.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.translatef(-0.5f, -0.5f, 0.5f);
        blockRenderDispatcher.renderSingleBlock(blockState, 1.0f);
        GlStateManager.popMatrix();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.disableCull();
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}

