/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;

@Environment(value=EnvType.CLIENT)
public class ShulkerHeadLayer
extends RenderLayer<Shulker, ShulkerModel<Shulker>> {
    public ShulkerHeadLayer(RenderLayerParent<Shulker, ShulkerModel<Shulker>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(Shulker shulker, float f, float g, float h, float i, float j, float k, float l) {
        RenderSystem.pushMatrix();
        switch (shulker.getAttachFace()) {
            case DOWN: {
                break;
            }
            case EAST: {
                RenderSystem.rotatef(90.0f, 0.0f, 0.0f, 1.0f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.translatef(1.0f, -1.0f, 0.0f);
                RenderSystem.rotatef(180.0f, 0.0f, 1.0f, 0.0f);
                break;
            }
            case WEST: {
                RenderSystem.rotatef(-90.0f, 0.0f, 0.0f, 1.0f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.translatef(-1.0f, -1.0f, 0.0f);
                RenderSystem.rotatef(180.0f, 0.0f, 1.0f, 0.0f);
                break;
            }
            case NORTH: {
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.translatef(0.0f, -1.0f, -1.0f);
                break;
            }
            case SOUTH: {
                RenderSystem.rotatef(180.0f, 0.0f, 0.0f, 1.0f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.translatef(0.0f, -1.0f, 1.0f);
                break;
            }
            case UP: {
                RenderSystem.rotatef(180.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.translatef(0.0f, -2.0f, 0.0f);
            }
        }
        ModelPart modelPart = ((ShulkerModel)this.getParentModel()).getHead();
        modelPart.yRot = j * ((float)Math.PI / 180);
        modelPart.xRot = k * ((float)Math.PI / 180);
        DyeColor dyeColor = shulker.getColor();
        if (dyeColor == null) {
            this.bindTexture(ShulkerRenderer.DEFAULT_TEXTURE_LOCATION);
        } else {
            this.bindTexture(ShulkerRenderer.TEXTURE_LOCATION[dyeColor.getId()]);
        }
        modelPart.render(l);
        RenderSystem.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

