package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class GameTestDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final float PADDING = 0.02F;
	private final Map<BlockPos, GameTestDebugRenderer.Marker> markers = Maps.<BlockPos, GameTestDebugRenderer.Marker>newHashMap();

	public void addMarker(BlockPos blockPos, int i, String string, int j) {
		this.markers.put(blockPos, new GameTestDebugRenderer.Marker(i, string, Util.getMillis() + (long)j));
	}

	@Override
	public void clear() {
		this.markers.clear();
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		long l = Util.getMillis();
		this.markers.entrySet().removeIf(entry -> l > ((GameTestDebugRenderer.Marker)entry.getValue()).removeAtTime);
		this.markers.forEach((blockPos, marker) -> this.renderMarker(poseStack, multiBufferSource, blockPos, marker));
	}

	private void renderMarker(PoseStack poseStack, MultiBufferSource multiBufferSource, BlockPos blockPos, GameTestDebugRenderer.Marker marker) {
		DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos, 0.02F, marker.getR(), marker.getG(), marker.getB(), marker.getA() * 0.75F);
		if (!marker.text.isEmpty()) {
			double d = (double)blockPos.getX() + 0.5;
			double e = (double)blockPos.getY() + 1.2;
			double f = (double)blockPos.getZ() + 0.5;
			DebugRenderer.renderFloatingText(poseStack, multiBufferSource, marker.text, d, e, f, -1, 0.01F, true, 0.0F, true);
		}
	}

	@Environment(EnvType.CLIENT)
	static class Marker {
		public int color;
		public String text;
		public long removeAtTime;

		public Marker(int i, String string, long l) {
			this.color = i;
			this.text = string;
			this.removeAtTime = l;
		}

		public float getR() {
			return (float)(this.color >> 16 & 0xFF) / 255.0F;
		}

		public float getG() {
			return (float)(this.color >> 8 & 0xFF) / 255.0F;
		}

		public float getB() {
			return (float)(this.color & 0xFF) / 255.0F;
		}

		public float getA() {
			return (float)(this.color >> 24 & 0xFF) / 255.0F;
		}
	}
}
