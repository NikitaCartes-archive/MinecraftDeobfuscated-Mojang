package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(EnvType.CLIENT)
public class CollisionBoxRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private double lastUpdateTime = Double.MIN_VALUE;
	private List<VoxelShape> shapes = Collections.emptyList();

	public CollisionBoxRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(long l) {
		Camera camera = this.minecraft.gameRenderer.getMainCamera();
		double d = (double)Util.getNanos();
		if (d - this.lastUpdateTime > 1.0E8) {
			this.lastUpdateTime = d;
			this.shapes = (List<VoxelShape>)camera.getEntity()
				.level
				.getCollisions(camera.getEntity(), camera.getEntity().getBoundingBox().inflate(6.0), Collections.emptySet())
				.collect(Collectors.toList());
		}

		double e = camera.getPosition().x;
		double f = camera.getPosition().y;
		double g = camera.getPosition().z;
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		RenderSystem.lineWidth(2.0F);
		RenderSystem.disableTexture();
		RenderSystem.depthMask(false);

		for (VoxelShape voxelShape : this.shapes) {
			LevelRenderer.renderVoxelShape(voxelShape, -e, -f, -g, 1.0F, 1.0F, 1.0F, 1.0F);
		}

		RenderSystem.depthMask(true);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
}
