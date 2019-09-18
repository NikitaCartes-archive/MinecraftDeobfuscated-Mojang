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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

@Environment(value=EnvType.CLIENT)
public class EnchantTableRenderer
extends BatchedBlockEntityRenderer<EnchantmentTableBlockEntity> {
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("entity/enchanting_table_book");
    private final BookModel bookModel = new BookModel();

    @Override
    protected void renderToBuffer(EnchantmentTableBlockEntity enchantmentTableBlockEntity, double d, double e, double f, float g, int i, RenderType renderType, BufferBuilder bufferBuilder, int j, int k) {
        float l;
        bufferBuilder.pushPose();
        bufferBuilder.translate(0.5, 0.75, 0.5);
        float h = (float)enchantmentTableBlockEntity.time + g;
        bufferBuilder.translate(0.0, 0.1f + Mth.sin(h * 0.1f) * 0.01f, 0.0);
        for (l = enchantmentTableBlockEntity.rot - enchantmentTableBlockEntity.oRot; l >= (float)Math.PI; l -= (float)Math.PI * 2) {
        }
        while (l < (float)(-Math.PI)) {
            l += (float)Math.PI * 2;
        }
        float m = enchantmentTableBlockEntity.oRot + l * g;
        bufferBuilder.multiplyPose(new Quaternion(Vector3f.YP, -m, false));
        bufferBuilder.multiplyPose(new Quaternion(Vector3f.ZP, 80.0f, true));
        float n = Mth.lerp(g, enchantmentTableBlockEntity.oFlip, enchantmentTableBlockEntity.flip);
        float o = Mth.frac(n + 0.25f) * 1.6f - 0.3f;
        float p = Mth.frac(n + 0.75f) * 1.6f - 0.3f;
        float q = Mth.lerp(g, enchantmentTableBlockEntity.oOpen, enchantmentTableBlockEntity.open);
        this.bookModel.setupAnim(h, Mth.clamp(o, 0.0f, 1.0f), Mth.clamp(p, 0.0f, 1.0f), q);
        this.bookModel.render(bufferBuilder, 0.0625f, j, k, this.getSprite(BOOK_LOCATION));
        bufferBuilder.popPose();
    }
}

