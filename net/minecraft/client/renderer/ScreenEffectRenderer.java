/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class ScreenEffectRenderer {
    private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png");

    public static void renderScreenEffect(Minecraft minecraft, PoseStack poseStack) {
        RenderSystem.disableAlphaTest();
        if (minecraft.player.isInWall()) {
            BlockState blockState = minecraft.level.getBlockState(new BlockPos(minecraft.player));
            LocalPlayer player = minecraft.player;
            for (int i = 0; i < 8; ++i) {
                double d = player.x + (double)(((float)((i >> 0) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
                double e = player.y + (double)(((float)((i >> 1) % 2) - 0.5f) * 0.1f);
                double f = player.z + (double)(((float)((i >> 2) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
                BlockPos blockPos = new BlockPos(d, e + (double)player.getEyeHeight(), f);
                BlockState blockState2 = minecraft.level.getBlockState(blockPos);
                if (!blockState2.isViewBlocking(minecraft.level, blockPos)) continue;
                blockState = blockState2;
            }
            if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                ScreenEffectRenderer.renderTex(minecraft, minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState), poseStack);
            }
        }
        if (!minecraft.player.isSpectator()) {
            if (minecraft.player.isUnderLiquid(FluidTags.WATER)) {
                ScreenEffectRenderer.renderWater(minecraft, poseStack);
            }
            if (minecraft.player.isOnFire()) {
                ScreenEffectRenderer.renderFire(minecraft, poseStack);
            }
        }
        RenderSystem.enableAlphaTest();
    }

    private static void renderTex(Minecraft minecraft, TextureAtlasSprite textureAtlasSprite, PoseStack poseStack) {
        minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        float f = 0.1f;
        float g = -1.0f;
        float h = 1.0f;
        float i = -1.0f;
        float j = 1.0f;
        float k = -0.5f;
        float l = textureAtlasSprite.getU0();
        float m = textureAtlasSprite.getU1();
        float n = textureAtlasSprite.getV0();
        float o = textureAtlasSprite.getV1();
        Matrix4f matrix4f = poseStack.getPose();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferBuilder.vertex(matrix4f, -1.0f, -1.0f, -0.5f).color(0.1f, 0.1f, 0.1f, 1.0f).uv(m, o).endVertex();
        bufferBuilder.vertex(matrix4f, 1.0f, -1.0f, -0.5f).color(0.1f, 0.1f, 0.1f, 1.0f).uv(l, o).endVertex();
        bufferBuilder.vertex(matrix4f, 1.0f, 1.0f, -0.5f).color(0.1f, 0.1f, 0.1f, 1.0f).uv(l, n).endVertex();
        bufferBuilder.vertex(matrix4f, -1.0f, 1.0f, -0.5f).color(0.1f, 0.1f, 0.1f, 1.0f).uv(m, n).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }

    private static void renderWater(Minecraft minecraft, PoseStack poseStack) {
        minecraft.getTextureManager().bind(UNDERWATER_LOCATION);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        float f = minecraft.player.getBrightness();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float g = 4.0f;
        float h = -1.0f;
        float i = 1.0f;
        float j = -1.0f;
        float k = 1.0f;
        float l = -0.5f;
        float m = -minecraft.player.yRot / 64.0f;
        float n = minecraft.player.xRot / 64.0f;
        Matrix4f matrix4f = poseStack.getPose();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferBuilder.vertex(matrix4f, -1.0f, -1.0f, -0.5f).color(f, f, f, 0.1f).uv(4.0f + m, 4.0f + n).endVertex();
        bufferBuilder.vertex(matrix4f, 1.0f, -1.0f, -0.5f).color(f, f, f, 0.1f).uv(0.0f + m, 4.0f + n).endVertex();
        bufferBuilder.vertex(matrix4f, 1.0f, 1.0f, -0.5f).color(f, f, f, 0.1f).uv(0.0f + m, 0.0f + n).endVertex();
        bufferBuilder.vertex(matrix4f, -1.0f, 1.0f, -0.5f).color(f, f, f, 0.1f).uv(4.0f + m, 0.0f + n).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.disableBlend();
    }

    private static void renderFire(Minecraft minecraft, PoseStack poseStack) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float f = 1.0f;
        for (int i = 0; i < 2; ++i) {
            poseStack.pushPose();
            TextureAtlasSprite textureAtlasSprite = minecraft.getTextureAtlas().getSprite(ModelBakery.FIRE_1);
            minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
            float g = textureAtlasSprite.getU0();
            float h = textureAtlasSprite.getU1();
            float j = textureAtlasSprite.getV0();
            float k = textureAtlasSprite.getV1();
            float l = -0.5f;
            float m = 0.5f;
            float n = -0.5f;
            float o = 0.5f;
            float p = -0.5f;
            poseStack.translate((float)(-(i * 2 - 1)) * 0.24f, -0.3f, 0.0);
            poseStack.mulPose(Vector3f.YP.rotation((float)(i * 2 - 1) * 10.0f, true));
            Matrix4f matrix4f = poseStack.getPose();
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR_TEX);
            bufferBuilder.vertex(matrix4f, -0.5f, -0.5f, -0.5f).color(1.0f, 1.0f, 1.0f, 0.9f).uv(h, k).endVertex();
            bufferBuilder.vertex(matrix4f, 0.5f, -0.5f, -0.5f).color(1.0f, 1.0f, 1.0f, 0.9f).uv(g, k).endVertex();
            bufferBuilder.vertex(matrix4f, 0.5f, 0.5f, -0.5f).color(1.0f, 1.0f, 1.0f, 0.9f).uv(g, j).endVertex();
            bufferBuilder.vertex(matrix4f, -0.5f, 0.5f, -0.5f).color(1.0f, 1.0f, 1.0f, 0.9f).uv(h, j).endVertex();
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);
            poseStack.popPose();
        }
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
    }
}

