/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

@Environment(value=EnvType.CLIENT)
public class SnowGolemHeadLayer
extends RenderLayer<SnowGolem, SnowGolemModel<SnowGolem>> {
    public SnowGolemHeadLayer(RenderLayerParent<SnowGolem, SnowGolemModel<SnowGolem>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(SnowGolem snowGolem, float f, float g, float h, float i, float j, float k, float l) {
        if (snowGolem.isInvisible() || !snowGolem.hasPumpkin()) {
            return;
        }
        GlStateManager.pushMatrix();
        ((SnowGolemModel)this.getParentModel()).getHead().translateTo(0.0625f);
        float m = 0.625f;
        GlStateManager.translatef(0.0f, -0.34375f, 0.0f);
        GlStateManager.rotatef(180.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.scalef(0.625f, -0.625f, -0.625f);
        Minecraft.getInstance().getItemInHandRenderer().renderItem(snowGolem, new ItemStack(Blocks.CARVED_PUMPKIN), ItemTransforms.TransformType.HEAD);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return true;
    }
}

