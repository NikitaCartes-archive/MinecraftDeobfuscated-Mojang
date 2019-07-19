package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class WorldGenAttemptRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private final List<BlockPos> toRender = Lists.<BlockPos>newArrayList();
	private final List<Float> scales = Lists.<Float>newArrayList();
	private final List<Float> alphas = Lists.<Float>newArrayList();
	private final List<Float> reds = Lists.<Float>newArrayList();
	private final List<Float> greens = Lists.<Float>newArrayList();
	private final List<Float> blues = Lists.<Float>newArrayList();

	public WorldGenAttemptRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void addPos(BlockPos blockPos, float f, float g, float h, float i, float j) {
		this.toRender.add(blockPos);
		this.scales.add(f);
		this.alphas.add(j);
		this.reds.add(g);
		this.greens.add(h);
		this.blues.add(i);
	}

	@Override
	public void render(long l) {
		Camera camera = this.minecraft.gameRenderer.getMainCamera();
		double d = camera.getPosition().x;
		double e = camera.getPosition().y;
		double f = camera.getPosition().z;
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		GlStateManager.disableTexture();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);

		for (int i = 0; i < this.toRender.size(); i++) {
			BlockPos blockPos = (BlockPos)this.toRender.get(i);
			Float float_ = (Float)this.scales.get(i);
			float g = float_ / 2.0F;
			LevelRenderer.addChainedFilledBoxVertices(
				bufferBuilder,
				(double)((float)blockPos.getX() + 0.5F - g) - d,
				(double)((float)blockPos.getY() + 0.5F - g) - e,
				(double)((float)blockPos.getZ() + 0.5F - g) - f,
				(double)((float)blockPos.getX() + 0.5F + g) - d,
				(double)((float)blockPos.getY() + 0.5F + g) - e,
				(double)((float)blockPos.getZ() + 0.5F + g) - f,
				(Float)this.reds.get(i),
				(Float)this.greens.get(i),
				(Float)this.blues.get(i),
				(Float)this.alphas.get(i)
			);
		}

		tesselator.end();
		GlStateManager.enableTexture();
		GlStateManager.popMatrix();
	}
}
