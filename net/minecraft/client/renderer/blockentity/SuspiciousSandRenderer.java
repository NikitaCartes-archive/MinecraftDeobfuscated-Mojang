/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SuspiciousSandBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@Environment(value=EnvType.CLIENT)
public class SuspiciousSandRenderer
implements BlockEntityRenderer<SuspiciousSandBlockEntity> {
    private final ItemRenderer itemRenderer;

    public SuspiciousSandRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(SuspiciousSandBlockEntity suspiciousSandBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        if (suspiciousSandBlockEntity.getLevel() == null) {
            return;
        }
        int k = suspiciousSandBlockEntity.getBlockState().getValue(BlockStateProperties.DUSTED);
        if (k <= 0) {
            return;
        }
        Direction direction = suspiciousSandBlockEntity.getHitDirection();
        if (direction == null) {
            return;
        }
        ItemStack itemStack = suspiciousSandBlockEntity.getItem();
        if (itemStack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.5f, 0.0f);
        float[] fs = this.translations(direction, k);
        poseStack.translate(fs[0], fs[1], fs[2]);
        poseStack.mulPose(Axis.YP.rotationDegrees(75.0f));
        boolean bl = direction == Direction.EAST || direction == Direction.WEST;
        poseStack.mulPose(Axis.YP.rotationDegrees((bl ? 90 : 0) + 11));
        poseStack.scale(0.5f, 0.5f, 0.5f);
        int l = LevelRenderer.getLightColor(suspiciousSandBlockEntity.getLevel(), suspiciousSandBlockEntity.getBlockState(), suspiciousSandBlockEntity.getBlockPos().relative(direction));
        this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, l, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, suspiciousSandBlockEntity.getLevel(), 0);
        poseStack.popPose();
    }

    private float[] translations(Direction direction, int i) {
        float[] fs = new float[]{0.5f, 0.0f, 0.5f};
        float f = (float)i / 10.0f * 0.75f;
        switch (direction) {
            case EAST: {
                fs[0] = 0.73f + f;
                break;
            }
            case WEST: {
                fs[0] = 0.25f - f;
                break;
            }
            case UP: {
                fs[1] = 0.25f + f;
                break;
            }
            case DOWN: {
                fs[1] = -0.23f - f;
                break;
            }
            case NORTH: {
                fs[2] = 0.25f - f;
                break;
            }
            case SOUTH: {
                fs[2] = 0.73f + f;
            }
        }
        return fs;
    }
}

