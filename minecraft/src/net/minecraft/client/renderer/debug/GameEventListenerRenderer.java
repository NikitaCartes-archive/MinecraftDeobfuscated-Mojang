package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
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
			BlockPos blockPos = new BlockPos(d, 0.0, f);
			this.trackedGameEvents.removeIf(GameEventListenerRenderer.TrackedGameEvent::isExpired);
			this.trackedListeners.removeIf(trackedListener -> trackedListener.isExpired(level, blockPos));
			RenderSystem.pushMatrix();
			RenderSystem.disableTexture();
			RenderSystem.enableDepthTest();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());

			for (GameEventListenerRenderer.TrackedListener trackedListener : this.trackedListeners) {
				trackedListener.getPosition(level)
					.ifPresent(
						blockPosx -> {
							int ix = blockPosx.getX() - trackedListener.getListenerRadius();
							int jx = blockPosx.getY() - trackedListener.getListenerRadius();
							int kx = blockPosx.getZ() - trackedListener.getListenerRadius();
							int lx = blockPosx.getX() + trackedListener.getListenerRadius();
							int mx = blockPosx.getY() + trackedListener.getListenerRadius();
							int n = blockPosx.getZ() + trackedListener.getListenerRadius();
							Vector3f vector3f = new Vector3f(1.0F, 1.0F, 0.0F);
							LevelRenderer.renderVoxelShape(
								poseStack,
								vertexConsumer,
								Shapes.create(new AABB((double)ix, (double)jx, (double)kx, (double)lx, (double)mx, (double)n)),
								-d,
								-e,
								-f,
								vector3f.x(),
								vector3f.y(),
								vector3f.z(),
								0.35F
							);
						}
					);
			}

			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

			for (GameEventListenerRenderer.TrackedListener trackedListener2 : this.trackedListeners) {
				trackedListener2.getPosition(level)
					.ifPresent(
						blockPosx -> {
							Vector3f vector3f = new Vector3f(1.0F, 1.0F, 0.0F);
							LevelRenderer.addChainedFilledBoxVertices(
								bufferBuilder,
								(double)((float)blockPosx.getX() - 0.25F) - d,
								(double)blockPosx.getY() - e,
								(double)((float)blockPosx.getZ() - 0.25F) - f,
								(double)((float)blockPosx.getX() + 0.25F) - d,
								(double)blockPosx.getY() - e + 1.0,
								(double)((float)blockPosx.getZ() + 0.25F) - f,
								vector3f.x(),
								vector3f.y(),
								vector3f.z(),
								0.35F
							);
						}
					);
			}

			tesselator.end();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.lineWidth(2.0F);
			RenderSystem.depthMask(false);

			for (GameEventListenerRenderer.TrackedListener trackedListener2 : this.trackedListeners) {
				trackedListener2.getPosition(level)
					.ifPresent(
						blockPosx -> {
							DebugRenderer.renderFloatingText(
								"Listener Origin", (double)blockPosx.getX(), (double)((float)blockPosx.getY() + 1.8F), (double)blockPosx.getZ(), -1, 0.025F
							);
							DebugRenderer.renderFloatingText(
								new BlockPos(blockPosx).toString(), (double)blockPosx.getX(), (double)((float)blockPosx.getY() + 1.5F), (double)blockPosx.getZ(), -6959665, 0.025F
							);
						}
					);
			}

			for (GameEventListenerRenderer.TrackedGameEvent trackedGameEvent : this.trackedGameEvents) {
				Vec3 vec3 = trackedGameEvent.position;
				double g = 0.2F;
				double h = vec3.x - 0.2F;
				double i = vec3.y - 0.2F;
				double j = vec3.z - 0.2F;
				double k = vec3.x + 0.2F;
				double l = vec3.y + 0.2F + 0.5;
				double m = vec3.z + 0.2F;
				renderTransparentFilledBox(new AABB(h, i, j, k, l, m), 1.0F, 1.0F, 1.0F, 0.2F);
				DebugRenderer.renderFloatingText(trackedGameEvent.gameEvent.getName(), vec3.x, vec3.y + 0.85F, vec3.z, -7564911, 0.0075F);
			}

			RenderSystem.depthMask(true);
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();
			RenderSystem.popMatrix();
		}
	}

	private static void renderTransparentFilledBox(AABB aABB, float f, float g, float h, float i) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		if (camera.isInitialized()) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Vec3 vec3 = camera.getPosition().reverse();
			DebugRenderer.renderFilledBox(aABB.move(vec3), f, g, h, i);
		}
	}

	public void trackGameEvent(GameEvent gameEvent, BlockPos blockPos) {
		this.trackedGameEvents.add(new GameEventListenerRenderer.TrackedGameEvent(Util.getMillis(), gameEvent, Vec3.atBottomCenterOf(blockPos)));
	}

	public void trackListener(PositionSource positionSource, int i) {
		this.trackedListeners.add(new GameEventListenerRenderer.TrackedListener(positionSource, i));
	}

	@Environment(EnvType.CLIENT)
	static class TrackedGameEvent {
		public final long timeStamp;
		public final GameEvent gameEvent;
		public final Vec3 position;

		public TrackedGameEvent(long l, GameEvent gameEvent, Vec3 vec3) {
			this.timeStamp = l;
			this.gameEvent = gameEvent;
			this.position = vec3;
		}

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

		public boolean isExpired(Level level, BlockPos blockPos) {
			Optional<BlockPos> optional = this.listenerSource.getPosition(level);
			return !optional.isPresent() || ((BlockPos)optional.get()).distSqr(blockPos) <= 1024.0;
		}

		public Optional<BlockPos> getPosition(Level level) {
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
		public boolean handleGameEvent(Level level, GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos) {
			return false;
		}
	}
}
