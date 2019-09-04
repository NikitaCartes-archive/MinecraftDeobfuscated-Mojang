/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BedModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

@Environment(value=EnvType.CLIENT)
public class BedRenderer
extends BlockEntityRenderer<BedBlockEntity> {
    private static final ResourceLocation[] TEXTURES = (ResourceLocation[])Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map(dyeColor -> new ResourceLocation("textures/entity/bed/" + dyeColor.getName() + ".png")).toArray(ResourceLocation[]::new);
    private final BedModel bedModel = new BedModel();

    @Override
    public void render(BedBlockEntity bedBlockEntity, double d, double e, double f, float g, int i) {
        if (i >= 0) {
            this.bindTexture(BREAKING_LOCATIONS[i]);
            RenderSystem.matrixMode(5890);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(4.0f, 4.0f, 1.0f);
            RenderSystem.translatef(0.0625f, 0.0625f, 0.0625f);
            RenderSystem.matrixMode(5888);
        } else {
            ResourceLocation resourceLocation = TEXTURES[bedBlockEntity.getColor().getId()];
            if (resourceLocation != null) {
                this.bindTexture(resourceLocation);
            }
        }
        if (bedBlockEntity.hasLevel()) {
            BlockState blockState = bedBlockEntity.getBlockState();
            this.renderPiece(blockState.getValue(BedBlock.PART) == BedPart.HEAD, d, e, f, blockState.getValue(BedBlock.FACING));
        } else {
            this.renderPiece(true, d, e, f, Direction.SOUTH);
            this.renderPiece(false, d, e, f - 1.0, Direction.SOUTH);
        }
        if (i >= 0) {
            RenderSystem.matrixMode(5890);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
        }
    }

    private void renderPiece(boolean bl, double d, double e, double f, Direction direction) {
        this.bedModel.preparePiece(bl);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)d, (float)e + 0.5625f, (float)f);
        RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.translatef(0.5f, 0.5f, 0.5f);
        RenderSystem.rotatef(180.0f + direction.toYRot(), 0.0f, 0.0f, 1.0f);
        RenderSystem.translatef(-0.5f, -0.5f, -0.5f);
        RenderSystem.enableRescaleNormal();
        this.bedModel.render();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.popMatrix();
    }
}

