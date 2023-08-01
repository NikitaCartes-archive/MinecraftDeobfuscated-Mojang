package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

@Environment(EnvType.CLIENT)
public class GameEventListenerRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final Minecraft minecraft;
	private static final int LISTENER_RENDER_DIST = 32;
	private static final float BOX_HEIGHT = 1.0F;
	private final List<GameEventListenerRenderer.TrackedGameEvent> trackedGameEvents = Lists.<GameEventListenerRenderer.TrackedGameEvent>newArrayList();
	private final List<GameEventListenerRenderer.TrackedListener> trackedListeners = Lists.<GameEventListenerRenderer.TrackedListener>newArrayList();

	public GameEventListenerRenderer(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		Level level = this.minecraft.level;
		if (level == null) {
			this.trackedGameEvents.clear();
			this.trackedListeners.clear();
		} else {
			Vec3 vec3 = new Vec3(d, 0.0, f);
			this.trackedGameEvents.removeIf(GameEventListenerRenderer.TrackedGameEvent::isExpired);
			this.trackedListeners.removeIf(trackedListenerx -> trackedListenerx.isExpired(level, vec3));
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());

			for (GameEventListenerRenderer.TrackedListener trackedListener : this.trackedListeners) {
				trackedListener.getPosition(level).ifPresent(vec3x -> {
					double gx = vec3x.x() - (double)trackedListener.getListenerRadius();
					double hx = vec3x.y() - (double)trackedListener.getListenerRadius();
					double ix = vec3x.z() - (double)trackedListener.getListenerRadius();
					double jx = vec3x.x() + (double)trackedListener.getListenerRadius();
					double kx = vec3x.y() + (double)trackedListener.getListenerRadius();
					double lx = vec3x.z() + (double)trackedListener.getListenerRadius();
					LevelRenderer.renderVoxelShape(poseStack, vertexConsumer, Shapes.create(new AABB(gx, hx, ix, jx, kx, lx)), -d, -e, -f, 1.0F, 1.0F, 0.0F, 0.35F, true);
				});
			}

			VertexConsumer vertexConsumer2 = multiBufferSource.getBuffer(RenderType.debugFilledBox());

			for (GameEventListenerRenderer.TrackedListener trackedListener2 : this.trackedListeners) {
				trackedListener2.getPosition(level)
					.ifPresent(
						vec3x -> LevelRenderer.addChainedFilledBoxVertices(
								poseStack,
								vertexConsumer2,
								vec3x.x() - 0.25 - d,
								vec3x.y() - e,
								vec3x.z() - 0.25 - f,
								vec3x.x() + 0.25 - d,
								vec3x.y() - e + 1.0,
								vec3x.z() + 0.25 - f,
								1.0F,
								1.0F,
								0.0F,
								0.35F
							)
					);
			}

			for (GameEventListenerRenderer.TrackedListener trackedListener2 : this.trackedListeners) {
				trackedListener2.getPosition(level)
					.ifPresent(
						vec3x -> {
							DebugRenderer.renderFloatingText(poseStack, multiBufferSource, "Listener Origin", vec3x.x(), vec3x.y() + 1.8F, vec3x.z(), -1, 0.025F);
							DebugRenderer.renderFloatingText(
								poseStack, multiBufferSource, BlockPos.containing(vec3x).toString(), vec3x.x(), vec3x.y() + 1.5, vec3x.z(), -6959665, 0.025F
							);
						}
					);
			}

			for (GameEventListenerRenderer.TrackedGameEvent trackedGameEvent : this.trackedGameEvents) {
				Vec3 vec32 = trackedGameEvent.position;
				double g = 0.2F;
				double h = vec32.x - 0.2F;
				double i = vec32.y - 0.2F;
				double j = vec32.z - 0.2F;
				double k = vec32.x + 0.2F;
				double l = vec32.y + 0.2F + 0.5;
				double m = vec32.z + 0.2F;
				renderFilledBox(poseStack, multiBufferSource, new AABB(h, i, j, k, l, m), 1.0F, 1.0F, 1.0F, 0.2F);
				DebugRenderer.renderFloatingText(
					poseStack, multiBufferSource, trackedGameEvent.gameEvent.location().toString(), vec32.x, vec32.y + 0.85F, vec32.z, -7564911, 0.0075F
				);
			}
		}
	}

	private static void renderFilledBox(PoseStack poseStack, MultiBufferSource multiBufferSource, AABB aABB, float f, float g, float h, float i) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		if (camera.isInitialized()) {
			Vec3 vec3 = camera.getPosition().reverse();
			DebugRenderer.renderFilledBox(poseStack, multiBufferSource, aABB.move(vec3), f, g, h, i);
		}
	}

	public void trackGameEvent(ResourceKey<GameEvent> resourceKey, Vec3 vec3) {
		this.trackedGameEvents.add(new GameEventListenerRenderer.TrackedGameEvent(Util.getMillis(), resourceKey, vec3));
	}

	public void trackListener(PositionSource positionSource, int i) {
		this.trackedListeners.add(new GameEventListenerRenderer.TrackedListener(positionSource, i));
	}

	@Environment(EnvType.CLIENT)
	static record TrackedGameEvent(long timeStamp, ResourceKey<GameEvent> gameEvent, Vec3 position) {

		public boolean isExpired() {
			return Util.getMillis() - this.timeStamp > 3000L;
		}
	}

	@Environment(EnvType.CLIENT)
	static class TrackedListener implements GameEventListener {
		public final PositionSource listenerSource;
		public final int listenerRange;

		public TrackedListener(PositionSource positionSource, int i) {
			this.listenerSource = positionSource;
			this.listenerRange = i;
		}

		public boolean isExpired(Level level, Vec3 vec3) {
			return this.listenerSource.getPosition(level).filter(vec32 -> vec32.distanceToSqr(vec3) <= 1024.0).isPresent();
		}

		public Optional<Vec3> getPosition(Level level) {
			return this.listenerSource.getPosition(level);
		}

		@Override
		public PositionSource getListenerSource() {
			return this.listenerSource;
		}

		@Override
		public int getListenerRadius() {
			return this.listenerRange;
		}

		@Override
		public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 vec3) {
			return false;
		}
	}
}
