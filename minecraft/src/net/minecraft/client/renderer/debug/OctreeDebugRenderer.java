package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Octree;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableInt;

@Environment(EnvType.CLIENT)
public class OctreeDebugRenderer {
	private final Minecraft minecraft;

	public OctreeDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void render(PoseStack poseStack, Frustum frustum, MultiBufferSource multiBufferSource, double d, double e, double f) {
		Octree octree = this.minecraft.levelRenderer.getSectionOcclusionGraph().getOctree();
		MutableInt mutableInt = new MutableInt(0);
		octree.visitNodes((node, bl, i) -> this.renderNode(node, poseStack, multiBufferSource, d, e, f, i, bl, mutableInt), frustum);
	}

	private void renderNode(
		Octree.Node node, PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f, int i, boolean bl, MutableInt mutableInt
	) {
		AABB aABB = node.getAABB();
		double g = aABB.getXsize();
		long l = Math.round(g / 16.0);
		if (l == 1L) {
			mutableInt.add(1);
			double h = aABB.getCenter().x;
			double j = aABB.getCenter().y;
			double k = aABB.getCenter().z;
			DebugRenderer.renderFloatingText(poseStack, multiBufferSource, String.valueOf(mutableInt.getValue()), h, j, k, -1, 0.3F);
		}

		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
		long m = l + 5L;
		ShapeRenderer.renderLineBox(
			poseStack,
			vertexConsumer,
			aABB.deflate(0.1 * (double)i).move(-d, -e, -f),
			getColorComponent(m, 0.3F),
			getColorComponent(m, 0.8F),
			getColorComponent(m, 0.5F),
			bl ? 0.4F : 1.0F
		);
	}

	private static float getColorComponent(long l, float f) {
		float g = 0.1F;
		return Mth.frac(f * (float)l) * 0.9F + 0.1F;
	}
}
