/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
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
extends BlockEntityRenderer<ShulkerBoxBlockEntity> {
    private final ShulkerModel<?> model;

    public ShulkerBoxRenderer(ShulkerModel<?> shulkerModel, BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
        this.model = shulkerModel;
    }

    @Override
    public void render(ShulkerBoxBlockEntity shulkerBoxBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        DyeColor dyeColor;
        BlockState blockState;
        Direction direction = Direction.UP;
        if (shulkerBoxBlockEntity.hasLevel() && (blockState = shulkerBoxBlockEntity.getLevel().getBlockState(shulkerBoxBlockEntity.getBlockPos())).getBlock() instanceof ShulkerBoxBlock) {
            direction = blockState.getValue(ShulkerBoxBlock.FACING);
        }
        ResourceLocation resourceLocation = (dyeColor = shulkerBoxBlockEntity.getColor()) == null ? ModelBakery.DEFAULT_SHULKER_TEXTURE_LOCATION : ModelBakery.SHULKER_TEXTURE_LOCATION.get(dyeColor.getId());
        TextureAtlasSprite textureAtlasSprite = this.getSprite(resourceLocation);
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.scale(1.0f, -1.0f, -1.0f);
        poseStack.translate(0.0, 1.0, 0.0);
        float h = 0.9995f;
        poseStack.scale(0.9995f, 0.9995f, 0.9995f);
        poseStack.mulPose(direction.getRotation());
        poseStack.translate(0.0, -1.0, 0.0);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        this.model.getBase().render(poseStack, vertexConsumer, 0.0625f, i, j, textureAtlasSprite);
        poseStack.translate(0.0, -shulkerBoxBlockEntity.getProgress(g) * 0.5f, 0.0);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(270.0f * shulkerBoxBlockEntity.getProgress(g)));
        this.model.getLid().render(poseStack, vertexConsumer, 0.0625f, i, j, textureAtlasSprite);
        poseStack.popPose();
    }
}

