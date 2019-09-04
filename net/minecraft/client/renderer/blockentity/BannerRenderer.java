/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BannerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.banner.BannerTextures;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BannerRenderer
extends BlockEntityRenderer<BannerBlockEntity> {
    private final BannerModel bannerModel = new BannerModel();

    @Override
    public void render(BannerBlockEntity bannerBlockEntity, double d, double e, double f, float g, int i) {
        long l;
        float h = 0.6666667f;
        boolean bl = bannerBlockEntity.getLevel() == null;
        RenderSystem.pushMatrix();
        ModelPart modelPart = this.bannerModel.getPole();
        if (bl) {
            l = 0L;
            RenderSystem.translatef((float)d + 0.5f, (float)e + 0.5f, (float)f + 0.5f);
            modelPart.visible = true;
        } else {
            l = bannerBlockEntity.getLevel().getGameTime();
            BlockState blockState = bannerBlockEntity.getBlockState();
            if (blockState.getBlock() instanceof BannerBlock) {
                RenderSystem.translatef((float)d + 0.5f, (float)e + 0.5f, (float)f + 0.5f);
                RenderSystem.rotatef((float)(-blockState.getValue(BannerBlock.ROTATION).intValue() * 360) / 16.0f, 0.0f, 1.0f, 0.0f);
                modelPart.visible = true;
            } else {
                RenderSystem.translatef((float)d + 0.5f, (float)e - 0.16666667f, (float)f + 0.5f);
                RenderSystem.rotatef(-blockState.getValue(WallBannerBlock.FACING).toYRot(), 0.0f, 1.0f, 0.0f);
                RenderSystem.translatef(0.0f, -0.3125f, -0.4375f);
                modelPart.visible = false;
            }
        }
        BlockPos blockPos = bannerBlockEntity.getBlockPos();
        float j = ((float)Math.floorMod((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + l, 100L) + g) / 100.0f;
        this.bannerModel.getFlag().xRot = (-0.0125f + 0.01f * Mth.cos((float)Math.PI * 2 * j)) * (float)Math.PI;
        RenderSystem.enableRescaleNormal();
        ResourceLocation resourceLocation = this.getTextureLocation(bannerBlockEntity);
        if (resourceLocation != null) {
            this.bindTexture(resourceLocation);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.6666667f, -0.6666667f, -0.6666667f);
            this.bannerModel.render();
            RenderSystem.popMatrix();
        }
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.popMatrix();
    }

    @Nullable
    private ResourceLocation getTextureLocation(BannerBlockEntity bannerBlockEntity) {
        return BannerTextures.BANNER_CACHE.getTextureLocation(bannerBlockEntity.getTextureHashName(), bannerBlockEntity.getPatterns(), bannerBlockEntity.getColors());
    }
}

