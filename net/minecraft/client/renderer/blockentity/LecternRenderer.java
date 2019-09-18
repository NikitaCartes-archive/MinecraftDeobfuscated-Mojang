/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BatchedBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class LecternRenderer
extends BatchedBlockEntityRenderer<LecternBlockEntity> {
    private final BookModel bookModel = new BookModel();

    @Override
    protected void renderToBuffer(LecternBlockEntity lecternBlockEntity, double d, double e, double f, float g, int i, RenderType renderType, BufferBuilder bufferBuilder, int j, int k) {
        BlockState blockState = lecternBlockEntity.getBlockState();
        if (!blockState.getValue(LecternBlock.HAS_BOOK).booleanValue()) {
            return;
        }
        bufferBuilder.pushPose();
        bufferBuilder.translate(0.5, 1.0625, 0.5);
        float h = blockState.getValue(LecternBlock.FACING).getClockWise().toYRot();
        bufferBuilder.multiplyPose(new Quaternion(Vector3f.YP, -h, true));
        bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 67.5f, true));
        bufferBuilder.translate(0.0, -0.125, 0.0);
        this.bookModel.setupAnim(0.0f, 0.1f, 0.9f, 1.2f);
        this.bookModel.render(bufferBuilder, 0.0625f, j, k, this.getSprite(EnchantTableRenderer.BOOK_LOCATION));
        bufferBuilder.popPose();
    }
}

