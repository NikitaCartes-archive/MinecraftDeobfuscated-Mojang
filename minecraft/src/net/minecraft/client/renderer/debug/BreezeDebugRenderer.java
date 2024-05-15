package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class BreezeDebugRenderer {
	private static final int JUMP_TARGET_LINE_COLOR = FastColor.ARGB32.color(255, 255, 100, 255);
	private static final int TARGET_LINE_COLOR = FastColor.ARGB32.color(255, 100, 255, 255);
	private static final int INNER_CIRCLE_COLOR = FastColor.ARGB32.color(255, 0, 255, 0);
	private static final int MIDDLE_CIRCLE_COLOR = FastColor.ARGB32.color(255, 255, 165, 0);
	private static final int OUTER_CIRCLE_COLOR = FastColor.ARGB32.color(255, 255, 0, 0);
	private static final int CIRCLE_VERTICES = 20;
	private static final float SEGMENT_SIZE_RADIANS = (float) (Math.PI / 10);
	private final Minecraft minecraft;
	private final Map<Integer, BreezeDebugPayload.BreezeInfo> perEntity = new HashMap();

	public BreezeDebugRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		LocalPlayer localPlayer = this.minecraft.player;
		localPlayer.level()
			.getEntities(EntityType.BREEZE, localPlayer.getBoundingBox().inflate(100.0), breeze -> true)
			.forEach(
				breeze -> {
					Optional<BreezeDebugPayload.BreezeInfo> optional = Optional.ofNullable((BreezeDebugPayload.BreezeInfo)this.perEntity.get(breeze.getId()));
					optional.map(BreezeDebugPayload.BreezeInfo::attackTarget)
						.map(integer -> localPlayer.level().getEntity(integer))
						.map(entity -> entity.getPosition(this.minecraft.getTimer().getGameTimeDeltaPartialTick(true)))
						.ifPresent(vec3 -> {
							drawLine(poseStack, multiBufferSource, d, e, f, breeze.position(), vec3, TARGET_LINE_COLOR);
							Vec3 vec32 = vec3.add(0.0, 0.01F, 0.0);
							drawCircle(poseStack.last().pose(), d, e, f, multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0)), vec32, 4.0F, INNER_CIRCLE_COLOR);
							drawCircle(poseStack.last().pose(), d, e, f, multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0)), vec32, 8.0F, MIDDLE_CIRCLE_COLOR);
							drawCircle(poseStack.last().pose(), d, e, f, multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0)), vec32, 20.0F, OUTER_CIRCLE_COLOR);
						});
					optional.map(BreezeDebugPayload.BreezeInfo::jumpTarget)
						.ifPresent(
							blockPos -> {
								drawLine(poseStack, multiBufferSource, d, e, f, breeze.position(), blockPos.getCenter(), JUMP_TARGET_LINE_COLOR);
								DebugRenderer.renderFilledBox(
									poseStack, multiBufferSource, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(blockPos)).move(-d, -e, -f), 1.0F, 0.0F, 0.0F, 1.0F
								);
							}
						);
				}
			);
	}

	private static void drawLine(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f, Vec3 vec3, Vec3 vec32, int i) {
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0));
		vertexConsumer.vertex(poseStack.last(), (float)(vec3.x - d), (float)(vec3.y - e), (float)(vec3.z - f)).color(i).endVertex();
		vertexConsumer.vertex(poseStack.last(), (float)(vec32.x - d), (float)(vec32.y - e), (float)(vec32.z - f)).color(i).endVertex();
	}

	private static void drawCircle(Matrix4f matrix4f, double d, double e, double f, VertexConsumer vertexConsumer, Vec3 vec3, float g, int i) {
		for (int j = 0; j < 20; j++) {
			drawCircleVertex(j, matrix4f, d, e, f, vertexConsumer, vec3, g, i);
		}

		drawCircleVertex(0, matrix4f, d, e, f, vertexConsumer, vec3, g, i);
	}

	private static void drawCircleVertex(int i, Matrix4f matrix4f, double d, double e, double f, VertexConsumer vertexConsumer, Vec3 vec3, float g, int j) {
		float h = (float)i * (float) (Math.PI / 10);
		Vec3 vec32 = vec3.add((double)g * Math.cos((double)h), 0.0, (double)g * Math.sin((double)h));
		vertexConsumer.vertex(matrix4f, (float)(vec32.x - d), (float)(vec32.y - e), (float)(vec32.z - f)).color(j).endVertex();
	}

	public void clear() {
		this.perEntity.clear();
	}

	public void add(BreezeDebugPayload.BreezeInfo breezeInfo) {
		this.perEntity.put(breezeInfo.id(), breezeInfo);
	}
}
