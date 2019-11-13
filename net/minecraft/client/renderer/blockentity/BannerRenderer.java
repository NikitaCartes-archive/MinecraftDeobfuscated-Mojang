/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class BannerRenderer
extends BlockEntityRenderer<BannerBlockEntity> {
    private final ModelPart flag = new ModelPart(64, 64, 0, 0);
    private final ModelPart pole;
    private final ModelPart bar;

    public BannerRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
        this.flag.addBox(-10.0f, 0.0f, -2.0f, 20.0f, 40.0f, 1.0f, 0.0f);
        this.pole = new ModelPart(64, 64, 44, 0);
        this.pole.addBox(-1.0f, -30.0f, -1.0f, 2.0f, 42.0f, 2.0f, 0.0f);
        this.bar = new ModelPart(64, 64, 0, 42);
        this.bar.addBox(-10.0f, -32.0f, -1.0f, 20.0f, 2.0f, 2.0f, 0.0f);
    }

    @Override
    public void render(BannerBlockEntity bannerBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        long l;
        if (bannerBlockEntity.getPatterns() == null) {
            return;
        }
        float g = 0.6666667f;
        boolean bl = bannerBlockEntity.getLevel() == null;
        poseStack.pushPose();
        if (bl) {
            l = 0L;
            poseStack.translate(0.5, 0.5, 0.5);
            this.pole.visible = !bannerBlockEntity.onlyRenderPattern();
        } else {
            float h;
            l = bannerBlockEntity.getLevel().getGameTime();
            BlockState blockState = bannerBlockEntity.getBlockState();
            if (blockState.getBlock() instanceof BannerBlock) {
                poseStack.translate(0.5, 0.5, 0.5);
                h = (float)(-blockState.getValue(BannerBlock.ROTATION).intValue() * 360) / 16.0f;
                poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
                this.pole.visible = true;
            } else {
                poseStack.translate(0.5, -0.1666666716337204, 0.5);
                h = -blockState.getValue(WallBannerBlock.FACING).toYRot();
                poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
                poseStack.translate(0.0, -0.3125, -0.4375);
                this.pole.visible = false;
            }
        }
        TextureAtlasSprite textureAtlasSprite = this.getSprite(ModelBakery.BANNER_BASE);
        poseStack.pushPose();
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.blockentitySolid());
        this.pole.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        this.bar.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        if (bannerBlockEntity.onlyRenderPattern()) {
            this.flag.xRot = 0.0f;
        } else {
            BlockPos blockPos = bannerBlockEntity.getBlockPos();
            float k = (float)((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + l) + f;
            this.flag.xRot = (-0.0125f + 0.01f * Mth.cos(k * (float)Math.PI * 0.02f)) * (float)Math.PI;
        }
        this.flag.y = -32.0f;
        this.flag.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        BannerRenderer.renderPatterns(bannerBlockEntity, poseStack, multiBufferSource, i, j, this.flag, true);
        poseStack.popPose();
        poseStack.popPose();
    }

    public static void renderPatterns(BannerBlockEntity bannerBlockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, ModelPart modelPart, boolean bl) {
        List<BannerPattern> list = bannerBlockEntity.getPatterns();
        List<DyeColor> list2 = bannerBlockEntity.getColors();
        TextureAtlas textureAtlas = Minecraft.getInstance().getTextureAtlas();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.blockentityNoOutline());
        for (int k = 0; k < 17 && k < list.size() && k < list2.size(); ++k) {
            BannerPattern bannerPattern = list.get(k);
            DyeColor dyeColor = list2.get(k);
            float[] fs = dyeColor.getTextureDiffuseColors();
            modelPart.render(poseStack, vertexConsumer, i, j, textureAtlas.getSprite(bannerPattern.location(bl)), fs[0], fs[1], fs[2]);
        }
    }
}

