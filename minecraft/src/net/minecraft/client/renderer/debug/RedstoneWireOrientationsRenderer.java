package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.network.protocol.common.custom.RedstoneWireOrientationsDebugPayload;
import net.minecraft.world.level.redstone.Orientation;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class RedstoneWireOrientationsRenderer implements DebugRenderer.SimpleDebugRenderer {
	public static final int TIMEOUT = 200;
	private final Minecraft minecraft;
	private final List<RedstoneWireOrientationsDebugPayload> updatedWires = Lists.<RedstoneWireOrientationsDebugPayload>newArrayList();

	RedstoneWireOrientationsRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void addWireOrientations(RedstoneWireOrientationsDebugPayload redstoneWireOrientationsDebugPayload) {
		this.updatedWires.add(redstoneWireOrientationsDebugPayload);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
		long l = this.minecraft.level.getGameTime();
		Iterator<RedstoneWireOrientationsDebugPayload> iterator = this.updatedWires.iterator();

		while (iterator.hasNext()) {
			RedstoneWireOrientationsDebugPayload redstoneWireOrientationsDebugPayload = (RedstoneWireOrientationsDebugPayload)iterator.next();
			long m = l - redstoneWireOrientationsDebugPayload.time();
			if (m > 200L) {
				iterator.remove();
			} else {
				for (RedstoneWireOrientationsDebugPayload.Wire wire : redstoneWireOrientationsDebugPayload.wires()) {
					Vector3f vector3f = wire.pos().getBottomCenter().subtract(d, e - 0.1, f).toVector3f();
					Orientation orientation = wire.orientation();
					ShapeRenderer.renderVector(poseStack, vertexConsumer, vector3f, orientation.getFront().getUnitVec3().scale(0.5), -16776961);
					ShapeRenderer.renderVector(poseStack, vertexConsumer, vector3f, orientation.getUp().getUnitVec3().scale(0.4), -65536);
					ShapeRenderer.renderVector(poseStack, vertexConsumer, vector3f, orientation.getSide().getUnitVec3().scale(0.3), -256);
				}
			}
		}
	}
}
