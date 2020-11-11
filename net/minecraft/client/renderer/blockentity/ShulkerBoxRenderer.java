/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class ShulkerBoxRenderer
implements BlockEntityRenderer<ShulkerBoxBlockEntity> {
    private final ShulkerModel<?> model;

    public ShulkerBoxRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ShulkerModel(context.getLayer(ModelLayers.SHULKER));
    }

    @Override
    public void render(ShulkerBoxBlockEntity shulkerBoxBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        DyeColor dyeColor;
        BlockState blockState;
        Direction direction = Direction.UP;
        if (shulkerBoxBlockEntity.hasLevel() && (blockState = shulkerBoxBlockEntity.getLevel().getBlockState(shulkerBoxBlockEntity.getBlockPos())).getBlock() instanceof ShulkerBoxBlock) {
            direction = blockState.getValue(ShulkerBoxBlock.FACING);
        }
        Material material = (dyeColor = shulkerBoxBlockEntity.getColor()) == null ? Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION : Sheets.SHULKER_TEXTURE_LOCATION.get(dyeColor.getId());
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        float g = 0.9995f;
        poseStack.scale(0.9995f, 0.9995f, 0.9995f);
        poseStack.mulPose(direction.getRotation());
        poseStack.scale(1.0f, -1.0f, -1.0f);
        poseStack.translate(0.0, -1.0, 0.0);
        ModelPart modelPart = this.model.getLid();
        modelPart.setPos(0.0f, 24.0f - shulkerBoxBlockEntity.getProgress(f) * 0.5f * 16.0f, 0.0f);
        modelPart.yRot = 270.0f * shulkerBoxBlockEntity.getProgress(f) * ((float)Math.PI / 180);
        VertexConsumer vertexConsumer = material.buffer(multiBufferSource, RenderType::entityCutoutNoCull);
        this.model.renderToBuffer(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}

