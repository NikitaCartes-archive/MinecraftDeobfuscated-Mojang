package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
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
	private static final long TIMEOUT = 5000L;
	private static final float MAX_RENDER_DIST = 80.0F;
	private static final boolean SHOW_OPEN_CLOSED = true;
	private static final boolean SHOW_OPEN_CLOSED_COST_MALUS = false;
	private static final boolean SHOW_OPEN_CLOSED_NODE_TYPE_WITH_TEXT = false;
	private static final boolean SHOW_OPEN_CLOSED_NODE_TYPE_WITH_BOX = true;
	private static final boolean SHOW_GROUND_LABELS = true;
	private static final float TEXT_SCALE = 0.02F;

	public void addPath(int i, Path path, float f) {
		this.pathMap.put(i, path);
		this.creationMap.put(i, Util.getMillis());
		this.pathMaxDist.put(i, f);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		if (!this.pathMap.isEmpty()) {
			long l = Util.getMillis();

			for (Integer integer : this.pathMap.keySet()) {
				Path path = (Path)this.pathMap.get(integer);
				float g = (Float)this.pathMaxDist.get(integer);
				renderPath(path, g, true, true, d, e, f);
			}

			for (Integer integer2 : (Integer[])this.creationMap.keySet().toArray(new Integer[0])) {
				if (l - (Long)this.creationMap.get(integer2) > 5000L) {
					this.pathMap.remove(integer2);
					this.creationMap.remove(integer2);
				}
			}
		}
	}

	public static void renderPath(Path path, float f, boolean bl, boolean bl2, double d, double e, double g) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 0.75F);
		RenderSystem.disableTexture();
		RenderSystem.lineWidth(6.0F);
		doRenderPath(path, f, bl, bl2, d, e, g);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
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

			for (int i = 0; i < path.getNodeCount(); i++) {
				Node node = path.getNode(i);
				if (distanceToCamera(node.asBlockPos(), d, e, g) <= 80.0F) {
					float h = i == path.getNextNodeIndex() ? 1.0F : 0.0F;
					float j = i == path.getNextNodeIndex() ? 0.0F : 1.0F;
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
			for (int ix = 0; ix < path.getNodeCount(); ix++) {
				Node node = path.getNode(ix);
				if (distanceToCamera(node.asBlockPos(), d, e, g) <= 80.0F) {
					DebugRenderer.renderFloatingText(
						String.format("%s", node.type), (double)node.x + 0.5, (double)node.y + 0.75, (double)node.z + 0.5, -1, 0.02F, true, 0.0F, true
					);
					DebugRenderer.renderFloatingText(
						String.format(Locale.ROOT, "%.2f", node.costMalus), (double)node.x + 0.5, (double)node.y + 0.25, (double)node.z + 0.5, -1, 0.02F, true, 0.0F, true
					);
				}
			}
		}
	}

	public static void renderPathLine(Path path, double d, double e, double f) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferBuilder.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

		for (int i = 0; i < path.getNodeCount(); i++) {
			Node node = path.getNode(i);
			if (!(distanceToCamera(node.asBlockPos(), d, e, f) > 80.0F)) {
				float g = (float)i / (float)path.getNodeCount() * 0.33F;
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
