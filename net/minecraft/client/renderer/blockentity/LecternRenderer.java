/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class LecternRenderer
extends BlockEntityRenderer<LecternBlockEntity> {
    private static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private final BookModel bookModel = new BookModel();

    @Override
    public void render(LecternBlockEntity lecternBlockEntity, double d, double e, double f, float g, int i) {
        BlockState blockState = lecternBlockEntity.getBlockState();
        if (!blockState.getValue(LecternBlock.HAS_BOOK).booleanValue()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)d + 0.5f, (float)e + 1.0f + 0.0625f, (float)f + 0.5f);
        float h = blockState.getValue(LecternBlock.FACING).getClockWise().toYRot();
        GlStateManager.rotatef(-h, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotatef(67.5f, 0.0f, 0.0f, 1.0f);
        GlStateManager.translatef(0.0f, -0.125f, 0.0f);
        this.bindTexture(BOOK_LOCATION);
        GlStateManager.enableCull();
        this.bookModel.render(0.0f, 0.1f, 0.9f, 1.2f, 0.0f, 0.0625f);
        GlStateManager.popMatrix();
    }
}

