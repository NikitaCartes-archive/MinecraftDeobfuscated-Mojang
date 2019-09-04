/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
        RenderSystem.enableCull();
        RenderSystem.cullFace(GlStateManager.CullFace.FRONT);
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.0f, -1.0f, 1.0f);
        RenderSystem.translatef(0.2f, 0.35f, 0.5f);
        RenderSystem.rotatef(42.0f, 0.0f, 1.0f, 0.0f);
        BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        RenderSystem.pushMatrix();
        RenderSystem.translatef(-0.5f, -0.5f, 0.5f);
        blockRenderDispatcher.renderSingleBlock(blockState, 1.0f);
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.1f, 0.0f, -0.6f);
        RenderSystem.rotatef(42.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.translatef(-0.5f, -0.5f, 0.5f);
        blockRenderDispatcher.renderSingleBlock(blockState, 1.0f);
        RenderSystem.popMatrix();
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
        ((CowModel)this.getParentModel()).getHead().translateTo(0.0625f);
        RenderSystem.scalef(1.0f, -1.0f, 1.0f);
        RenderSystem.translatef(0.0f, 0.7f, -0.2f);
        RenderSystem.rotatef(12.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.translatef(-0.5f, -0.5f, 0.5f);
        blockRenderDispatcher.renderSingleBlock(blockState, 1.0f);
        RenderSystem.popMatrix();
        RenderSystem.cullFace(GlStateManager.CullFace.BACK);
        RenderSystem.disableCull();
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}

