/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

@Environment(value=EnvType.CLIENT)
public class EnchantTableRenderer
implements BlockEntityRenderer<EnchantmentTableBlockEntity> {
    public static final Material BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/enchanting_table_book"));
    private final BookModel bookModel;

    public EnchantTableRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(EnchantmentTableBlockEntity enchantmentTableBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        float h;
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.75f, 0.5f);
        float g = (float)enchantmentTableBlockEntity.time + f;
        poseStack.translate(0.0f, 0.1f + Mth.sin(g * 0.1f) * 0.01f, 0.0f);
        for (h = enchantmentTableBlockEntity.rot - enchantmentTableBlockEntity.oRot; h >= (float)Math.PI; h -= (float)Math.PI * 2) {
        }
        while (h < (float)(-Math.PI)) {
            h += (float)Math.PI * 2;
        }
        float k = enchantmentTableBlockEntity.oRot + h * f;
        poseStack.mulPose(Axis.YP.rotation(-k));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0f));
        float l = Mth.lerp(f, enchantmentTableBlockEntity.oFlip, enchantmentTableBlockEntity.flip);
        float m = Mth.frac(l + 0.25f) * 1.6f - 0.3f;
        float n = Mth.frac(l + 0.75f) * 1.6f - 0.3f;
        float o = Mth.lerp(f, enchantmentTableBlockEntity.oOpen, enchantmentTableBlockEntity.open);
        this.bookModel.setupAnim(g, Mth.clamp(m, 0.0f, 1.0f), Mth.clamp(n, 0.0f, 1.0f), o);
        VertexConsumer vertexConsumer = BOOK_LOCATION.buffer(multiBufferSource, RenderType::entitySolid);
        this.bookModel.render(poseStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }
}

