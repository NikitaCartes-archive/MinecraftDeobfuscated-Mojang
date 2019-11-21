package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class PathfindingRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Map<Integer, Path> pathMap = Maps.<Integer, Path>newHashMap();
	private final Map<Integer, Float> pathMaxDist = Maps.<Integer, Float>newHashMap();
	private final Map<Integer, Long> creationMap = Maps.<Integer, Long>newHashMap();

	public void addPath(int i, Path path, float f) {
		this.pathMap.put(i, path);
		this.creationMap.put(i, Util.getMillis());
		this.pathMaxDist.put(i, f);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f, long l) {
		if (!this.pathMap.isEmpty()) {
			long m = Util.getMillis();

			for (Integer integer : this.pathMap.keySet()) {
				Path path = (Path)this.pathMap.get(integer);
				float g = (Float)this.pathMaxDist.get(integer);
				renderPath(path, g, true, true, d, e, f);
			}

			for (Integer integer2 : (Integer[])this.creationMap.keySet().toArray(new Integer[0])) {
				if (m - (Long)this.creationMap.get(integer2) > 5000L) {
					this.pathMap.remove(integer2);
					this.creationMap.remove(integer2);
				}
			}
		}
	}

	public static void renderPath(Path path, float f, boolean bl, boolean bl2, double d, double e, double g) {
		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(0.0F, 1.0F, 0.0F, 0.75F);
		RenderSystem.disableTexture();
		RenderSystem.lineWidth(6.0F);
		doRenderPath(path, f, bl, bl2, d, e, g);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
		RenderSystem.popMatrix();
	}

	private static void doRenderPath(Path path, float f, boolean bl, boolean bl2, double d, double e, double g) {
		renderPathLine(path, d, e, g);
		BlockPos blockPos = path.getTarget();
		if (distanceToCamera(blockPos, d, e, g) <= 80.0F) {
			DebugRenderer.renderFilledBox(
				new AABB(
						(double)((float)blockPos.getX() + 0.25F),
						(double)((float)blockPos.getY() + 0.25F),
						(double)blockPos.getZ() + 0.25,
						(double)((float)blockPos.getX() + 0.75F),
						(double)((float)blockPos.getY() + 0.75F),
						(double)((float)blockPos.getZ() + 0.75F)
					)
					.move(-d, -e, -g),
				0.0F,
				1.0F,
				0.0F,
				0.5F
			);

			for (int i = 0; i < path.getSize(); i++) {
				Node node = path.get(i);
				if (distanceToCamera(node.asBlockPos(), d, e, g) <= 80.0F) {
					float h = i == path.getIndex() ? 1.0F : 0.0F;
					float j = i == path.getIndex() ? 0.0F : 1.0F;
					DebugRenderer.renderFilledBox(
						new AABB(
								(double)((float)node.x + 0.5F - f),
								(double)((float)node.y + 0.01F * (float)i),
								(double)((float)node.z + 0.5F - f),
								(double)((float)node.x + 0.5F + f),
								(double)((float)node.y + 0.25F + 0.01F * (float)i),
								(double)((float)node.z + 0.5F + f)
							)
							.move(-d, -e, -g),
						h,
						0.0F,
						j,
						0.5F
					);
				}
			}
		}

		if (bl) {
			for (Node node2 : path.getClosedSet()) {
				if (distanceToCamera(node2.asBlockPos(), d, e, g) <= 80.0F) {
					DebugRenderer.renderFilledBox(
						new AABB(
								(double)((float)node2.x + 0.5F - f / 2.0F),
								(double)((float)node2.y + 0.01F),
								(double)((float)node2.z + 0.5F - f / 2.0F),
								(double)((float)node2.x + 0.5F + f / 2.0F),
								(double)node2.y + 0.1,
								(double)((float)node2.z + 0.5F + f / 2.0F)
							)
							.move(-d, -e, -g),
						1.0F,
						0.8F,
						0.8F,
						0.5F
					);
				}
			}

			for (Node node2x : path.getOpenSet()) {
				if (distanceToCamera(node2x.asBlockPos(), d, e, g) <= 80.0F) {
					DebugRenderer.renderFilledBox(
						new AABB(
								(double)((float)node2x.x + 0.5F - f / 2.0F),
								(double)((float)node2x.y + 0.01F),
								(double)((float)node2x.z + 0.5F - f / 2.0F),
								(double)((float)node2x.x + 0.5F + f / 2.0F),
								(double)node2x.y + 0.1,
								(double)((float)node2x.z + 0.5F + f / 2.0F)
							)
							.move(-d, -e, -g),
						0.8F,
						1.0F,
						1.0F,
						0.5F
					);
				}
			}
		}

		if (bl2) {
			for (int ix = 0; ix < path.getSize(); ix++) {
				Node node = path.get(ix);
				if (distanceToCamera(node.asBlockPos(), d, e, g) <= 80.0F) {
					DebugRenderer.renderFloatingText(String.format("%s", node.type), (double)node.x + 0.5, (double)node.y + 0.75, (double)node.z + 0.5, -1);
					DebugRenderer.renderFloatingText(String.format(Locale.ROOT, "%.2f", node.costMalus), (double)node.x + 0.5, (double)node.y + 0.25, (double)node.z + 0.5, -1);
				}
			}
		}
	}

	public static void renderPathLine(Path path, double d, double e, double f) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);

		for (int i = 0; i < path.getSize(); i++) {
			Node node = path.get(i);
			if (!(distanceToCamera(node.asBlockPos(), d, e, f) > 80.0F)) {
				float g = (float)i / (float)path.getSize() * 0.33F;
				int j = i == 0 ? 0 : Mth.hsvToRgb(g, 0.9F, 0.9F);
				int k = j >> 16 & 0xFF;
				int l = j >> 8 & 0xFF;
				int m = j & 0xFF;
				bufferBuilder.vertex((double)node.x - d + 0.5, (double)node.y - e + 0.5, (double)node.z - f + 0.5).color(k, l, m, 255).endVertex();
			}
		}

		tesselator.end();
	}

	private static float distanceToCamera(BlockPos blockPos, double d, double e, double f) {
		return (float)(Math.abs((double)blockPos.getX() - d) + Math.abs((double)blockPos.getY() - e) + Math.abs((double)blockPos.getZ() - f));
	}
}
