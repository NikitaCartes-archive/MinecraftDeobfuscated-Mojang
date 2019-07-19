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
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

@Environment(value=EnvType.CLIENT)
public class EnchantTableRenderer
extends BlockEntityRenderer<EnchantmentTableBlockEntity> {
    private static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private final BookModel bookModel = new BookModel();

    @Override
    public void render(EnchantmentTableBlockEntity enchantmentTableBlockEntity, double d, double e, double f, float g, int i) {
        float j;
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)d + 0.5f, (float)e + 0.75f, (float)f + 0.5f);
        float h = (float)enchantmentTableBlockEntity.time + g;
        GlStateManager.translatef(0.0f, 0.1f + Mth.sin(h * 0.1f) * 0.01f, 0.0f);
        for (j = enchantmentTableBlockEntity.rot - enchantmentTableBlockEntity.oRot; j >= (float)Math.PI; j -= (float)Math.PI * 2) {
        }
        while (j < (float)(-Math.PI)) {
            j += (float)Math.PI * 2;
        }
        float k = enchantmentTableBlockEntity.oRot + j * g;
        GlStateManager.rotatef(-k * 57.295776f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotatef(80.0f, 0.0f, 0.0f, 1.0f);
        this.bindTexture(BOOK_LOCATION);
        float l = Mth.lerp(g, enchantmentTableBlockEntity.oFlip, enchantmentTableBlockEntity.flip) + 0.25f;
        float m = Mth.lerp(g, enchantmentTableBlockEntity.oFlip, enchantmentTableBlockEntity.flip) + 0.75f;
        l = (l - (float)Mth.fastFloor(l)) * 1.6f - 0.3f;
        m = (m - (float)Mth.fastFloor(m)) * 1.6f - 0.3f;
        if (l < 0.0f) {
            l = 0.0f;
        }
        if (m < 0.0f) {
            m = 0.0f;
        }
        if (l > 1.0f) {
            l = 1.0f;
        }
        if (m > 1.0f) {
            m = 1.0f;
        }
        float n = Mth.lerp(g, enchantmentTableBlockEntity.oOpen, enchantmentTableBlockEntity.open);
        GlStateManager.enableCull();
        this.bookModel.render(h, l, m, n, 0.0f, 0.0625f);
        GlStateManager.popMatrix();
    }
}

