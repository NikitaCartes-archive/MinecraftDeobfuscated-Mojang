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
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BannerRenderer
extends BlockEntityRenderer<BannerBlockEntity> {
    private static final Logger LOGGER = LogManager.getLogger();
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
    public void render(BannerBlockEntity bannerBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        long l;
        float h = 0.6666667f;
        boolean bl = bannerBlockEntity.getLevel() == null;
        poseStack.pushPose();
        if (bl) {
            l = 0L;
            poseStack.translate(0.5, 0.5, f + 0.5);
            this.pole.visible = !bannerBlockEntity.onlyRenderPattern();
        } else {
            l = bannerBlockEntity.getLevel().getGameTime();
            BlockState blockState = bannerBlockEntity.getBlockState();
            if (blockState.getBlock() instanceof BannerBlock) {
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(Vector3f.YP.rotation((float)(-blockState.getValue(BannerBlock.ROTATION).intValue() * 360) / 16.0f, true));
                this.pole.visible = true;
            } else {
                poseStack.translate(0.5, -0.1666666716337204, 0.5);
                poseStack.mulPose(Vector3f.YP.rotation(-blockState.getValue(WallBannerBlock.FACING).toYRot(), true));
                poseStack.translate(0.0, -0.3125, -0.4375);
                this.pole.visible = false;
            }
        }
        TextureAtlasSprite textureAtlasSprite = this.getSprite(ModelBakery.BANNER_BASE);
        poseStack.pushPose();
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        float j = 0.0625f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.SOLID);
        this.pole.render(poseStack, vertexConsumer, 0.0625f, i, textureAtlasSprite);
        this.bar.render(poseStack, vertexConsumer, 0.0625f, i, textureAtlasSprite);
        if (bannerBlockEntity.onlyRenderPattern()) {
            this.flag.xRot = 0.0f;
        } else {
            BlockPos blockPos = bannerBlockEntity.getBlockPos();
            float k = (float)((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + l) + g;
            this.flag.xRot = (-0.0125f + 0.01f * Mth.cos(k * (float)Math.PI * 0.02f)) * (float)Math.PI;
        }
        this.flag.y = -32.0f;
        this.flag.render(poseStack, vertexConsumer, 0.0625f, i, textureAtlasSprite);
        List<BannerPattern> list = bannerBlockEntity.getPatterns();
        List<DyeColor> list2 = bannerBlockEntity.getColors();
        VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.TRANSLUCENT_NO_CRUMBLING);
        if (list == null) {
            LOGGER.error("patterns are null");
        } else if (list2 == null) {
            LOGGER.error("colors are null");
        } else {
            for (int m = 0; m < 17 && m < list.size() && m < list2.size(); ++m) {
                BannerPattern bannerPattern = list.get(m);
                DyeColor dyeColor = list2.get(m);
                float[] fs = dyeColor.getTextureDiffuseColors();
                this.flag.render(poseStack, vertexConsumer2, 0.0625f, i, this.getSprite(bannerPattern.location()), fs[0], fs[1], fs[2]);
            }
        }
        poseStack.popPose();
        poseStack.popPose();
    }
}

