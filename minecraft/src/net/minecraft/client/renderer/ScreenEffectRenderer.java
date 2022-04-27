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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class ScreenEffectRenderer {
	private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png");

	public static void renderScreenEffect(Minecraft minecraft, PoseStack poseStack) {
		Player player = minecraft.player;
		if (!player.noPhysics) {
			BlockState blockState = getViewBlockingState(player);
			if (blockState != null) {
				renderTex(minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState), poseStack);
			}
		}

		if (!minecraft.player.isSpectator()) {
			if (minecraft.player.isEyeInFluid(FluidTags.WATER)) {
				renderWater(minecraft, poseStack);
			}

			if (minecraft.player.isOnFire()) {
				renderFire(minecraft, poseStack);
			}
		}
	}

	@Nullable
	private static BlockState getViewBlockingState(Player player) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i < 8; i++) {
			double d = player.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
			double e = player.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
			double f = player.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
			mutableBlockPos.set(d, e, f);
			BlockState blockState = player.level.getBlockState(mutableBlockPos);
			if (blockState.getRenderShape() != RenderShape.INVISIBLE && blockState.isViewBlocking(player.level, mutableBlockPos)) {
				return blockState;
			}
		}

		return null;
	}

	private static void renderTex(TextureAtlasSprite textureAtlasSprite, PoseStack poseStack) {
		RenderSystem.setShaderTexture(0, textureAtlasSprite.atlas().location());
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		float f = 0.1F;
		float g = -1.0F;
		float h = 1.0F;
		float i = -1.0F;
		float j = 1.0F;
		float k = -0.5F;
		float l = textureAtlasSprite.getU0();
		float m = textureAtlasSprite.getU1();
		float n = textureAtlasSprite.getV0();
		float o = textureAtlasSprite.getV1();
		Matrix4f matrix4f = poseStack.last().pose();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		bufferBuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(m, o).endVertex();
		bufferBuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(l, o).endVertex();
		bufferBuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(l, n).endVertex();
		bufferBuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(m, n).endVertex();
		BufferUploader.drawWithShader(bufferBuilder.end());
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
		RenderSystem.setShaderColor(f, f, f, 0.1F);
		float g = 4.0F;
		float h = -1.0F;
		float i = 1.0F;
		float j = -1.0F;
		float k = 1.0F;
		float l = -0.5F;
		float m = -minecraft.player.getYRot() / 64.0F;
		float n = minecraft.player.getXRot() / 64.0F;
		Matrix4f matrix4f = poseStack.last().pose();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).uv(4.0F + m, 4.0F + n).endVertex();
		bufferBuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).uv(0.0F + m, 4.0F + n).endVertex();
		bufferBuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).uv(0.0F + m, 0.0F + n).endVertex();
		bufferBuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).uv(4.0F + m, 0.0F + n).endVertex();
		BufferUploader.drawWithShader(bufferBuilder.end());
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
		float h = (f + g) / 2.0F;
		float i = textureAtlasSprite.getV0();
		float j = textureAtlasSprite.getV1();
		float k = (i + j) / 2.0F;
		float l = textureAtlasSprite.uvShrinkRatio();
		float m = Mth.lerp(l, f, h);
		float n = Mth.lerp(l, g, h);
		float o = Mth.lerp(l, i, k);
		float p = Mth.lerp(l, j, k);
		float q = 1.0F;

		for (int r = 0; r < 2; r++) {
			poseStack.pushPose();
			float s = -0.5F;
			float t = 0.5F;
			float u = -0.5F;
			float v = 0.5F;
			float w = -0.5F;
			poseStack.translate((double)((float)(-(r * 2 - 1)) * 0.24F), -0.3F, 0.0);
			poseStack.mulPose(Vector3f.YP.rotationDegrees((float)(r * 2 - 1) * 10.0F));
			Matrix4f matrix4f = poseStack.last().pose();
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
			bufferBuilder.vertex(matrix4f, -0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(n, p).endVertex();
			bufferBuilder.vertex(matrix4f, 0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(m, p).endVertex();
			bufferBuilder.vertex(matrix4f, 0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(m, o).endVertex();
			bufferBuilder.vertex(matrix4f, -0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(n, o).endVertex();
			BufferUploader.drawWithShader(bufferBuilder.end());
			poseStack.popPose();
		}

		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.depthFunc(515);
	}
}
