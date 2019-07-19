/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class ShulkerBoxRenderer
extends BlockEntityRenderer<ShulkerBoxBlockEntity> {
    private final ShulkerModel<?> model;

    public ShulkerBoxRenderer(ShulkerModel<?> shulkerModel) {
        this.model = shulkerModel;
    }

    @Override
    public void render(ShulkerBoxBlockEntity shulkerBoxBlockEntity, double d, double e, double f, float g, int i) {
        BlockState blockState;
        Direction direction = Direction.UP;
        if (shulkerBoxBlockEntity.hasLevel() && (blockState = this.getLevel().getBlockState(shulkerBoxBlockEntity.getBlockPos())).getBlock() instanceof ShulkerBoxBlock) {
            direction = blockState.getValue(ShulkerBoxBlock.FACING);
        }
        GlStateManager.enableDepthTest();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        GlStateManager.disableCull();
        if (i >= 0) {
            this.bindTexture(BREAKING_LOCATIONS[i]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(4.0f, 4.0f, 1.0f);
            GlStateManager.translatef(0.0625f, 0.0625f, 0.0625f);
            GlStateManager.matrixMode(5888);
        } else {
            DyeColor dyeColor = shulkerBoxBlockEntity.getColor();
            if (dyeColor == null) {
                this.bindTexture(ShulkerRenderer.DEFAULT_TEXTURE_LOCATION);
            } else {
                this.bindTexture(ShulkerRenderer.TEXTURE_LOCATION[dyeColor.getId()]);
            }
        }
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        if (i < 0) {
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
        GlStateManager.translatef((float)d + 0.5f, (float)e + 1.5f, (float)f + 0.5f);
        GlStateManager.scalef(1.0f, -1.0f, -1.0f);
        GlStateManager.translatef(0.0f, 1.0f, 0.0f);
        float h = 0.9995f;
        GlStateManager.scalef(0.9995f, 0.9995f, 0.9995f);
        GlStateManager.translatef(0.0f, -1.0f, 0.0f);
        switch (direction) {
            case DOWN: {
                GlStateManager.translatef(0.0f, 2.0f, 0.0f);
                GlStateManager.rotatef(180.0f, 1.0f, 0.0f, 0.0f);
                break;
            }
            case UP: {
                break;
            }
            case NORTH: {
                GlStateManager.translatef(0.0f, 1.0f, 1.0f);
                GlStateManager.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                GlStateManager.rotatef(180.0f, 0.0f, 0.0f, 1.0f);
                break;
            }
            case SOUTH: {
                GlStateManager.translatef(0.0f, 1.0f, -1.0f);
                GlStateManager.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                break;
            }
            case WEST: {
                GlStateManager.translatef(-1.0f, 1.0f, 0.0f);
                GlStateManager.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                GlStateManager.rotatef(-90.0f, 0.0f, 0.0f, 1.0f);
                break;
            }
            case EAST: {
                GlStateManager.translatef(1.0f, 1.0f, 0.0f);
                GlStateManager.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                GlStateManager.rotatef(90.0f, 0.0f, 0.0f, 1.0f);
            }
        }
        this.model.getBase().render(0.0625f);
        GlStateManager.translatef(0.0f, -shulkerBoxBlockEntity.getProgress(g) * 0.5f, 0.0f);
        GlStateManager.rotatef(270.0f * shulkerBoxBlockEntity.getProgress(g), 0.0f, 1.0f, 0.0f);
        this.model.getLid().render(0.0625f);
        GlStateManager.enableCull();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (i >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}

