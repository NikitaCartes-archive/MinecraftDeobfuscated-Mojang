/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BatchedBlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class ShulkerBoxRenderer
extends BatchedBlockEntityRenderer<ShulkerBoxBlockEntity> {
    private final ShulkerModel<?> model;

    public ShulkerBoxRenderer(ShulkerModel<?> shulkerModel) {
        this.model = shulkerModel;
    }

    @Override
    protected void renderToBuffer(ShulkerBoxBlockEntity shulkerBoxBlockEntity, double d, double e, double f, float g, int i, RenderType renderType, BufferBuilder bufferBuilder, int j, int k) {
        DyeColor dyeColor;
        BlockState blockState;
        Direction direction = Direction.UP;
        if (shulkerBoxBlockEntity.hasLevel() && (blockState = this.getLevel().getBlockState(shulkerBoxBlockEntity.getBlockPos())).getBlock() instanceof ShulkerBoxBlock) {
            direction = blockState.getValue(ShulkerBoxBlock.FACING);
        }
        ResourceLocation resourceLocation = i >= 0 ? ModelBakery.DESTROY_STAGES.get(i) : ((dyeColor = shulkerBoxBlockEntity.getColor()) == null ? ModelBakery.DEFAULT_SHULKER_TEXTURE_LOCATION : ModelBakery.SHULKER_TEXTURE_LOCATION.get(dyeColor.getId()));
        TextureAtlasSprite textureAtlasSprite = this.getSprite(resourceLocation);
        bufferBuilder.pushPose();
        bufferBuilder.translate(0.5, 1.5, 0.5);
        bufferBuilder.scale(1.0f, -1.0f, -1.0f);
        bufferBuilder.translate(0.0, 1.0, 0.0);
        float h = 0.9995f;
        bufferBuilder.scale(0.9995f, 0.9995f, 0.9995f);
        bufferBuilder.translate(0.0, -1.0, 0.0);
        switch (direction) {
            case DOWN: {
                bufferBuilder.translate(0.0, 2.0, 0.0);
                bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 180.0f, true));
                break;
            }
            case UP: {
                break;
            }
            case NORTH: {
                bufferBuilder.translate(0.0, 1.0, 1.0);
                bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 90.0f, true));
                bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 180.0f, true));
                break;
            }
            case SOUTH: {
                bufferBuilder.translate(0.0, 1.0, -1.0);
                bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 90.0f, true));
                break;
            }
            case WEST: {
                bufferBuilder.translate(-1.0, 1.0, 0.0);
                bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 90.0f, true));
                bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, -90.0f, true));
                break;
            }
            case EAST: {
                bufferBuilder.translate(1.0, 1.0, 0.0);
                bufferBuilder.multiplyPose(new Quaternion(Vector3f.XP, 90.0f, true));
                bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 90.0f, true));
            }
        }
        this.model.getBase().render(bufferBuilder, 0.0625f, j, k, textureAtlasSprite);
        bufferBuilder.translate(0.0, -shulkerBoxBlockEntity.getProgress(g) * 0.5f, 0.0);
        bufferBuilder.multiplyPose(new Quaternion(Vector3f.YP, 270.0f * shulkerBoxBlockEntity.getProgress(g), true));
        this.model.getLid().render(bufferBuilder, 0.0625f, j, k, textureAtlasSprite);
        bufferBuilder.popPose();
    }
}

