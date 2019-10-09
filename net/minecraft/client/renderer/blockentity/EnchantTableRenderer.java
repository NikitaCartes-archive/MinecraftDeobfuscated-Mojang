/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

@Environment(value=EnvType.CLIENT)
public class EnchantTableRenderer
extends BlockEntityRenderer<EnchantmentTableBlockEntity> {
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("entity/enchanting_table_book");
    private final BookModel bookModel = new BookModel();

    public EnchantTableRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
    }

    @Override
    public void render(EnchantmentTableBlockEntity enchantmentTableBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        float k;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.75, 0.5);
        float h = (float)enchantmentTableBlockEntity.time + g;
        poseStack.translate(0.0, 0.1f + Mth.sin(h * 0.1f) * 0.01f, 0.0);
        for (k = enchantmentTableBlockEntity.rot - enchantmentTableBlockEntity.oRot; k >= (float)Math.PI; k -= (float)Math.PI * 2) {
        }
        while (k < (float)(-Math.PI)) {
            k += (float)Math.PI * 2;
        }
        float l = enchantmentTableBlockEntity.oRot + k * g;
        poseStack.mulPose(Vector3f.YP.rotation(-l));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(80.0f));
        float m = Mth.lerp(g, enchantmentTableBlockEntity.oFlip, enchantmentTableBlockEntity.flip);
        float n = Mth.frac(m + 0.25f) * 1.6f - 0.3f;
        float o = Mth.frac(m + 0.75f) * 1.6f - 0.3f;
        float p = Mth.lerp(g, enchantmentTableBlockEntity.oOpen, enchantmentTableBlockEntity.open);
        this.bookModel.setupAnim(h, Mth.clamp(n, 0.0f, 1.0f), Mth.clamp(o, 0.0f, 1.0f), p);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        this.bookModel.render(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, this.getSprite(BOOK_LOCATION));
        poseStack.popPose();
    }
}

