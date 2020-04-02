package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
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
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		double g = (double)Util.getNanos();
		if (g - this.lastUpdateTime > 1.0E8) {
			this.lastUpdateTime = g;
			Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
			this.shapes = (List<VoxelShape>)entity.level.getCollisions(entity, entity.getBoundingBox().inflate(6.0), entityx -> true).collect(Collectors.toList());
		}

		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());

		for (VoxelShape voxelShape : this.shapes) {
			LevelRenderer.renderVoxelShape(poseStack, vertexConsumer, voxelShape, -d, -e, -f, 1.0F, 1.0F, 1.0F, 1.0F);
		}
	}
}
