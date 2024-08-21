package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
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
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ScreenEffectRenderer {
	private static final ResourceLocation UNDERWATER_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");

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
			double e = player.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F * player.getScale());
			double f = player.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
			mutableBlockPos.set(d, e, f);
			BlockState blockState = player.level().getBlockState(mutableBlockPos);
			if (blockState.getRenderShape() != RenderShape.INVISIBLE && blockState.isViewBlocking(player.level(), mutableBlockPos)) {
				return blockState;
			}
		}

		return null;
	}

	private static void renderTex(TextureAtlasSprite textureAtlasSprite, PoseStack poseStack) {
		RenderSystem.setShaderTexture(0, textureAtlasSprite.atlasLocation());
		RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
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
		BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(m, o).setColor(0.1F, 0.1F, 0.1F, 1.0F);
		bufferBuilder.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(l, o).setColor(0.1F, 0.1F, 0.1F, 1.0F);
		bufferBuilder.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(l, n).setColor(0.1F, 0.1F, 0.1F, 1.0F);
		bufferBuilder.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(m, n).setColor(0.1F, 0.1F, 0.1F, 1.0F);
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
	}

	private static void renderWater(Minecraft minecraft, PoseStack poseStack) {
		RenderSystem.setShader(CoreShaders.POSITION_TEX);
		RenderSystem.setShaderTexture(0, UNDERWATER_LOCATION);
		BlockPos blockPos = BlockPos.containing(minecraft.player.getX(), minecraft.player.getEyeY(), minecraft.player.getZ());
		float f = LightTexture.getBrightness(minecraft.player.level().dimensionType(), minecraft.player.level().getMaxLocalRawBrightness(blockPos));
		RenderSystem.enableBlend();
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
		BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(4.0F + m, 4.0F + n);
		bufferBuilder.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(0.0F + m, 4.0F + n);
		bufferBuilder.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(0.0F + m, 0.0F + n);
		bufferBuilder.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(4.0F + m, 0.0F + n);
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
	}

	private static void renderFire(Minecraft minecraft, PoseStack poseStack) {
		RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
		RenderSystem.depthFunc(519);
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		TextureAtlasSprite textureAtlasSprite = ModelBakery.FIRE_1.sprite();
		RenderSystem.setShaderTexture(0, textureAtlasSprite.atlasLocation());
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
			poseStack.translate((float)(-(r * 2 - 1)) * 0.24F, -0.3F, 0.0F);
			poseStack.mulPose(Axis.YP.rotationDegrees((float)(r * 2 - 1) * 10.0F));
			Matrix4f matrix4f = poseStack.last().pose();
			BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.addVertex(matrix4f, -0.5F, -0.5F, -0.5F).setUv(n, p).setColor(1.0F, 1.0F, 1.0F, 0.9F);
			bufferBuilder.addVertex(matrix4f, 0.5F, -0.5F, -0.5F).setUv(m, p).setColor(1.0F, 1.0F, 1.0F, 0.9F);
			bufferBuilder.addVertex(matrix4f, 0.5F, 0.5F, -0.5F).setUv(m, o).setColor(1.0F, 1.0F, 1.0F, 0.9F);
			bufferBuilder.addVertex(matrix4f, -0.5F, 0.5F, -0.5F).setUv(n, o).setColor(1.0F, 1.0F, 1.0F, 0.9F);
			BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
			poseStack.popPose();
		}

		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.depthFunc(515);
	}
}
