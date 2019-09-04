/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
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
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        if (i >= 0) {
            this.bindTexture(BREAKING_LOCATIONS[i]);
            RenderSystem.matrixMode(5890);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(4.0f, 4.0f, 1.0f);
            RenderSystem.translatef(0.0625f, 0.0625f, 0.0625f);
            RenderSystem.matrixMode(5888);
        } else {
            DyeColor dyeColor = shulkerBoxBlockEntity.getColor();
            if (dyeColor == null) {
                this.bindTexture(ShulkerRenderer.DEFAULT_TEXTURE_LOCATION);
            } else {
                this.bindTexture(ShulkerRenderer.TEXTURE_LOCATION[dyeColor.getId()]);
            }
        }
        RenderSystem.pushMatrix();
        RenderSystem.enableRescaleNormal();
        if (i < 0) {
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        }
        RenderSystem.translatef((float)d + 0.5f, (float)e + 1.5f, (float)f + 0.5f);
        RenderSystem.scalef(1.0f, -1.0f, -1.0f);
        RenderSystem.translatef(0.0f, 1.0f, 0.0f);
        float h = 0.9995f;
        RenderSystem.scalef(0.9995f, 0.9995f, 0.9995f);
        RenderSystem.translatef(0.0f, -1.0f, 0.0f);
        switch (direction) {
            case DOWN: {
                RenderSystem.translatef(0.0f, 2.0f, 0.0f);
                RenderSystem.rotatef(180.0f, 1.0f, 0.0f, 0.0f);
                break;
            }
            case UP: {
                break;
            }
            case NORTH: {
                RenderSystem.translatef(0.0f, 1.0f, 1.0f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.rotatef(180.0f, 0.0f, 0.0f, 1.0f);
                break;
            }
            case SOUTH: {
                RenderSystem.translatef(0.0f, 1.0f, -1.0f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                break;
            }
            case WEST: {
                RenderSystem.translatef(-1.0f, 1.0f, 0.0f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.rotatef(-90.0f, 0.0f, 0.0f, 1.0f);
                break;
            }
            case EAST: {
                RenderSystem.translatef(1.0f, 1.0f, 0.0f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.rotatef(90.0f, 0.0f, 0.0f, 1.0f);
            }
        }
        this.model.getBase().render(0.0625f);
        RenderSystem.translatef(0.0f, -shulkerBoxBlockEntity.getProgress(g) * 0.5f, 0.0f);
        RenderSystem.rotatef(270.0f * shulkerBoxBlockEntity.getProgress(g), 0.0f, 1.0f, 0.0f);
        this.model.getLid().render(0.0625f);
        RenderSystem.enableCull();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (i >= 0) {
            RenderSystem.matrixMode(5890);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
        }
    }
}

