/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;

@Environment(value=EnvType.CLIENT)
public class IronGolemFlowerLayer
extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
    public IronGolemFlowerLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(IronGolem ironGolem, float f, float g, float h, float i, float j, float k, float l) {
        if (ironGolem.getOfferFlowerTick() == 0) {
            return;
        }
        RenderSystem.enableRescaleNormal();
        RenderSystem.pushMatrix();
        RenderSystem.rotatef(5.0f + 180.0f * ((IronGolemModel)this.getParentModel()).getFlowerHoldingArm().xRot / (float)Math.PI, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.translatef(-0.9375f, -0.625f, -0.9375f);
        float m = 0.5f;
        RenderSystem.scalef(0.5f, -0.5f, 0.5f);
        int n = ironGolem.getLightColor();
        int o = n % 65536;
        int p = n / 65536;
        RenderSystem.glMultiTexCoord2f(33985, o, p);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.POPPY.defaultBlockState(), 1.0f);
        RenderSystem.popMatrix();
        RenderSystem.disableRescaleNormal();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

