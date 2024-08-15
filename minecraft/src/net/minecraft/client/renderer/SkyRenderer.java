package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class SkyRenderer {
	private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
	private static final ResourceLocation MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
	private static final ResourceLocation END_SKY_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");
	private static final float SKY_DISC_RADIUS = 512.0F;
	private final VertexBuffer starBuffer = this.createStarBuffer();
	private final VertexBuffer topSkyBuffer = this.createTopSkyBuffer();
	private final VertexBuffer bottomSkyBuffer = this.createBottomSkyBuffer();

	private VertexBuffer createStarBuffer() {
		VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		vertexBuffer.bind();
		vertexBuffer.upload(this.drawStars(Tesselator.getInstance()));
		VertexBuffer.unbind();
		return vertexBuffer;
	}

	private VertexBuffer createTopSkyBuffer() {
		VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		vertexBuffer.bind();
		vertexBuffer.upload(this.buildSkyDisc(Tesselator.getInstance(), 16.0F));
		VertexBuffer.unbind();
		return vertexBuffer;
	}

	private VertexBuffer createBottomSkyBuffer() {
		VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		vertexBuffer.bind();
		vertexBuffer.upload(this.buildSkyDisc(Tesselator.getInstance(), -16.0F));
		VertexBuffer.unbind();
		return vertexBuffer;
	}

	private MeshData drawStars(Tesselator tesselator) {
		RandomSource randomSource = RandomSource.create(10842L);
		int i = 1500;
		float f = 100.0F;
		BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

		for (int j = 0; j < 1500; j++) {
			float g = randomSource.nextFloat() * 2.0F - 1.0F;
			float h = randomSource.nextFloat() * 2.0F - 1.0F;
			float k = randomSource.nextFloat() * 2.0F - 1.0F;
			float l = 0.15F + randomSource.nextFloat() * 0.1F;
			float m = Mth.lengthSquared(g, h, k);
			if (!(m <= 0.010000001F) && !(m >= 1.0F)) {
				Vector3f vector3f = new Vector3f(g, h, k).normalize(100.0F);
				float n = (float)(randomSource.nextDouble() * (float) Math.PI * 2.0);
				Matrix3f matrix3f = new Matrix3f().rotateTowards(new Vector3f(vector3f).negate(), new Vector3f(0.0F, 1.0F, 0.0F)).rotateZ(-n);
				bufferBuilder.addVertex(new Vector3f(l, -l, 0.0F).mul(matrix3f).add(vector3f));
				bufferBuilder.addVertex(new Vector3f(l, l, 0.0F).mul(matrix3f).add(vector3f));
				bufferBuilder.addVertex(new Vector3f(-l, l, 0.0F).mul(matrix3f).add(vector3f));
				bufferBuilder.addVertex(new Vector3f(-l, -l, 0.0F).mul(matrix3f).add(vector3f));
			}
		}

		return bufferBuilder.buildOrThrow();
	}

	private MeshData buildSkyDisc(Tesselator tesselator, float f) {
		float g = Math.signum(f) * 512.0F;
		BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
		bufferBuilder.addVertex(0.0F, f, 0.0F);

		for (int i = -180; i <= 180; i += 45) {
			bufferBuilder.addVertex(g * Mth.cos((float)i * (float) (Math.PI / 180.0)), f, 512.0F * Mth.sin((float)i * (float) (Math.PI / 180.0)));
		}

		return bufferBuilder.buildOrThrow();
	}

	public void renderSkyDisc(float f, float g, float h) {
		RenderSystem.depthMask(false);
		RenderSystem.setShaderColor(f, g, h, 1.0F);
		this.topSkyBuffer.bind();
		this.topSkyBuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), GameRenderer.getPositionShader());
		VertexBuffer.unbind();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
	}

	public void renderDarkDisc(PoseStack poseStack) {
		RenderSystem.depthMask(false);
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
		poseStack.pushPose();
		poseStack.translate(0.0F, 12.0F, 0.0F);
		this.bottomSkyBuffer.bind();
		this.bottomSkyBuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), GameRenderer.getPositionShader());
		VertexBuffer.unbind();
		poseStack.popPose();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
	}

	public void renderSunMoonAndStars(PoseStack poseStack, Tesselator tesselator, float f, int i, float g, float h, FogParameters fogParameters) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(f * 360.0F));
		this.renderSun(g, tesselator, poseStack);
		this.renderMoon(i, g, tesselator, poseStack);
		if (h > 0.0F) {
			this.renderStars(fogParameters, h, poseStack);
		}

		poseStack.popPose();
	}

	private void renderSun(float f, Tesselator tesselator, PoseStack poseStack) {
		float g = 30.0F;
		float h = 100.0F;
		BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		Matrix4f matrix4f = poseStack.last().pose();
		RenderSystem.depthMask(false);
		RenderSystem.overlayBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f);
		RenderSystem.setShaderTexture(0, SUN_LOCATION);
		RenderSystem.enableBlend();
		bufferBuilder.addVertex(matrix4f, -30.0F, 100.0F, -30.0F).setUv(0.0F, 0.0F);
		bufferBuilder.addVertex(matrix4f, 30.0F, 100.0F, -30.0F).setUv(1.0F, 0.0F);
		bufferBuilder.addVertex(matrix4f, 30.0F, 100.0F, 30.0F).setUv(1.0F, 1.0F);
		bufferBuilder.addVertex(matrix4f, -30.0F, 100.0F, 30.0F).setUv(0.0F, 1.0F);
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(true);
	}

	private void renderMoon(int i, float f, Tesselator tesselator, PoseStack poseStack) {
		float g = 20.0F;
		int j = i % 4;
		int k = i / 4 % 2;
		float h = (float)(j + 0) / 4.0F;
		float l = (float)(k + 0) / 2.0F;
		float m = (float)(j + 1) / 4.0F;
		float n = (float)(k + 1) / 2.0F;
		float o = 100.0F;
		BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		RenderSystem.depthMask(false);
		RenderSystem.overlayBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f);
		RenderSystem.setShaderTexture(0, MOON_LOCATION);
		RenderSystem.enableBlend();
		Matrix4f matrix4f = poseStack.last().pose();
		bufferBuilder.addVertex(matrix4f, -20.0F, -100.0F, 20.0F).setUv(m, n);
		bufferBuilder.addVertex(matrix4f, 20.0F, -100.0F, 20.0F).setUv(h, n);
		bufferBuilder.addVertex(matrix4f, 20.0F, -100.0F, -20.0F).setUv(h, l);
		bufferBuilder.addVertex(matrix4f, -20.0F, -100.0F, -20.0F).setUv(m, l);
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(true);
	}

	private void renderStars(FogParameters fogParameters, float f, PoseStack poseStack) {
		Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();
		matrix4fStack.mul(poseStack.last().pose());
		RenderSystem.depthMask(false);
		RenderSystem.overlayBlendFunc();
		RenderSystem.setShaderColor(f, f, f, f);
		RenderSystem.enableBlend();
		RenderSystem.setShaderFog(FogParameters.NO_FOG);
		this.starBuffer.bind();
		this.starBuffer.drawWithShader(matrix4fStack, RenderSystem.getProjectionMatrix(), GameRenderer.getPositionShader());
		VertexBuffer.unbind();
		RenderSystem.setShaderFog(fogParameters);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(true);
		matrix4fStack.popMatrix();
	}

	public void renderSunriseAndSunset(PoseStack poseStack, Tesselator tesselator, float f, int i) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		poseStack.pushPose();
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		float g = Mth.sin(f) < 0.0F ? 180.0F : 0.0F;
		poseStack.mulPose(Axis.ZP.rotationDegrees(g));
		poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
		Matrix4f matrix4f = poseStack.last().pose();
		BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
		float h = ARGB.from8BitChannel(ARGB.alpha(i));
		bufferBuilder.addVertex(matrix4f, 0.0F, 100.0F, 0.0F).setColor(i);
		int j = ARGB.transparent(i);
		int k = 16;

		for (int l = 0; l <= 16; l++) {
			float m = (float)l * (float) (Math.PI * 2) / 16.0F;
			float n = Mth.sin(m);
			float o = Mth.cos(m);
			bufferBuilder.addVertex(matrix4f, n * 120.0F, o * 120.0F, -o * 40.0F * h).setColor(j);
		}

		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
		poseStack.popPose();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
	}

	public void renderEndSky(PoseStack poseStack) {
		RenderSystem.enableBlend();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
		Tesselator tesselator = Tesselator.getInstance();

		for (int i = 0; i < 6; i++) {
			poseStack.pushPose();
			if (i == 1) {
				poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			}

			if (i == 2) {
				poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			}

			if (i == 3) {
				poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			}

			if (i == 4) {
				poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			}

			if (i == 5) {
				poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
			}

			Matrix4f matrix4f = poseStack.last().pose();
			BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(-14145496);
			bufferBuilder.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(0.0F, 16.0F).setColor(-14145496);
			bufferBuilder.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(16.0F, 16.0F).setColor(-14145496);
			bufferBuilder.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(16.0F, 0.0F).setColor(-14145496);
			BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
			poseStack.popPose();
		}

		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
	}
}
