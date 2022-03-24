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
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ScreenEffectRenderer {
    private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png");

    public static void renderScreenEffect(Minecraft minecraft, PoseStack poseStack) {
        BlockState blockState;
        LocalPlayer player = minecraft.player;
        if (!player.noPhysics && (blockState = ScreenEffectRenderer.getViewBlockingState(player)) != null) {
            ScreenEffectRenderer.renderTex(minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState), poseStack);
        }
        if (!minecraft.player.isSpectator()) {
            if (minecraft.player.isEyeInFluid(FluidTags.WATER)) {
                ScreenEffectRenderer.renderWater(minecraft, poseStack);
            }
            if (minecraft.player.isOnFire()) {
                ScreenEffectRenderer.renderFire(minecraft, poseStack);
            }
        }
    }

    @Nullable
    private static BlockState getViewBlockingState(Player player) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 8; ++i) {
            double d = player.getX() + (double)(((float)((i >> 0) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
            double e = player.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5f) * 0.1f);
            double f = player.getZ() + (double)(((float)((i >> 2) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
            mutableBlockPos.set(d, e, f);
            BlockState blockState = player.level.getBlockState(mutableBlockPos);
            if (blockState.getRenderShape() == RenderShape.INVISIBLE || !blockState.isViewBlocking(player.level, mutableBlockPos)) continue;
            return blockState;
        }
        return null;
    }

    private static void renderTex(TextureAtlasSprite textureAtlasSprite, PoseStack poseStack) {
        RenderSystem.setShaderTexture(0, textureAtlasSprite.atlas().location());
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
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
        Matrix4f matrix4f = poseStack.last().pose();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferBuilder.vertex(matrix4f, -1.0f, -1.0f, -0.5f).color(0.1f, 0.1f, 0.1f, 1.0f).uv(m, o).endVertex();
        bufferBuilder.vertex(matrix4f, 1.0f, -1.0f, -0.5f).color(0.1f, 0.1f, 0.1f, 1.0f).uv(l, o).endVertex();
        bufferBuilder.vertex(matrix4f, 1.0f, 1.0f, -0.5f).color(0.1f, 0.1f, 0.1f, 1.0f).uv(l, n).endVertex();
        bufferBuilder.vertex(matrix4f, -1.0f, 1.0f, -0.5f).color(0.1f, 0.1f, 0.1f, 1.0f).uv(m, n).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }

    private static void renderWater(Minecraft minecraft, PoseStack poseStack) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableTexture();
        RenderSystem.setShaderTexture(0, UNDERWATER_LOCATION);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        BlockPos blockPos = new BlockPos(minecraft.player.getX(), minecraft.player.getEyeY(), minecraft.player.getZ());
        float f = LightTexture.getBrightness(minecraft.player.level.dimensionType(), minecraft.player.level.getMaxLocalRawBrightness(blockPos));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(f, f, f, 0.1f);
        float g = 4.0f;
        float h = -1.0f;
        float i = 1.0f;
        float j = -1.0f;
        float k = 1.0f;
        float l = -0.5f;
        float m = -minecraft.player.getYRot() / 64.0f;
        float n = minecraft.player.getXRot() / 64.0f;
        Matrix4f matrix4f = poseStack.last().pose();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, -1.0f, -1.0f, -0.5f).uv(4.0f + m, 4.0f + n).endVertex();
        bufferBuilder.vertex(matrix4f, 1.0f, -1.0f, -0.5f).uv(0.0f + m, 4.0f + n).endVertex();
        bufferBuilder.vertex(matrix4f, 1.0f, 1.0f, -0.5f).uv(0.0f + m, 0.0f + n).endVertex();
        bufferBuilder.vertex(matrix4f, -1.0f, 1.0f, -0.5f).uv(4.0f + m, 0.0f + n).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.disableBlend();
    }

    private static void renderFire(Minecraft minecraft, PoseStack poseStack) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.depthFunc(519);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        TextureAtlasSprite textureAtlasSprite = ModelBakery.FIRE_1.sprite();
        RenderSystem.setShaderTexture(0, textureAtlasSprite.atlas().location());
        float f = textureAtlasSprite.getU0();
        float g = textureAtlasSprite.getU1();
        float h = (f + g) / 2.0f;
        float i = textureAtlasSprite.getV0();
        float j = textureAtlasSprite.getV1();
        float k = (i + j) / 2.0f;
        float l = textureAtlasSprite.uvShrinkRatio();
        float m = Mth.lerp(l, f, h);
        float n = Mth.lerp(l, g, h);
        float o = Mth.lerp(l, i, k);
        float p = Mth.lerp(l, j, k);
        float q = 1.0f;
        for (int r = 0; r < 2; ++r) {
            poseStack.pushPose();
            float s = -0.5f;
            float t = 0.5f;
            float u = -0.5f;
            float v = 0.5f;
            float w = -0.5f;
            poseStack.translate((float)(-(r * 2 - 1)) * 0.24f, -0.3f, 0.0);
            poseStack.mulPose(Vector3f.YP.rotationDegrees((float)(r * 2 - 1) * 10.0f));
            Matrix4f matrix4f = poseStack.last().pose();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            bufferBuilder.vertex(matrix4f, -0.5f, -0.5f, -0.5f).color(1.0f, 1.0f, 1.0f, 0.9f).uv(n, p).endVertex();
            bufferBuilder.vertex(matrix4f, 0.5f, -0.5f, -0.5f).color(1.0f, 1.0f, 1.0f, 0.9f).uv(m, p).endVertex();
            bufferBuilder.vertex(matrix4f, 0.5f, 0.5f, -0.5f).color(1.0f, 1.0f, 1.0f, 0.9f).uv(m, o).endVertex();
            bufferBuilder.vertex(matrix4f, -0.5f, 0.5f, -0.5f).color(1.0f, 1.0f, 1.0f, 0.9f).uv(n, o).endVertex();
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);
            poseStack.popPose();
        }
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(515);
    }
}

