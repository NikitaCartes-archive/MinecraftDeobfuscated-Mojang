/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

@Environment(value=EnvType.CLIENT)
public class GameEventListenerRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int LISTENER_RENDER_DIST = 32;
    private static final float BOX_HEIGHT = 1.0f;
    private final List<TrackedGameEvent> trackedGameEvents = Lists.newArrayList();
    private final List<TrackedListener> trackedListeners = Lists.newArrayList();

    public GameEventListenerRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        ClientLevel level = this.minecraft.level;
        if (level == null) {
            this.trackedGameEvents.clear();
            this.trackedListeners.clear();
            return;
        }
        Vec3 vec32 = new Vec3(d, 0.0, f);
        this.trackedGameEvents.removeIf(TrackedGameEvent::isExpired);
        this.trackedListeners.removeIf(trackedListener -> trackedListener.isExpired(level, vec32));
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        for (TrackedListener trackedListener2 : this.trackedListeners) {
            trackedListener2.getPosition(level).ifPresent(vec3 -> {
                double g = vec3.x() - (double)trackedListener2.getListenerRadius();
                double h = vec3.y() - (double)trackedListener2.getListenerRadius();
                double i = vec3.z() - (double)trackedListener2.getListenerRadius();
                double j = vec3.x() + (double)trackedListener2.getListenerRadius();
                double k = vec3.y() + (double)trackedListener2.getListenerRadius();
                double l = vec3.z() + (double)trackedListener2.getListenerRadius();
                Vector3f vector3f = new Vector3f(1.0f, 1.0f, 0.0f);
                LevelRenderer.renderVoxelShape(poseStack, vertexConsumer, Shapes.create(new AABB(g, h, i, j, k, l)), -d, -e, -f, vector3f.x(), vector3f.y(), vector3f.z(), 0.35f);
            });
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (TrackedListener trackedListener2 : this.trackedListeners) {
            trackedListener2.getPosition(level).ifPresent(vec3 -> {
                Vector3f vector3f = new Vector3f(1.0f, 1.0f, 0.0f);
                LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, vec3.x() - 0.25 - d, vec3.y() - e, vec3.z() - 0.25 - f, vec3.x() + 0.25 - d, vec3.y() - e + 1.0, vec3.z() + 0.25 - f, vector3f.x(), vector3f.y(), vector3f.z(), 0.35f);
            });
        }
        tesselator.end();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        RenderSystem.depthMask(false);
        for (TrackedListener trackedListener2 : this.trackedListeners) {
            trackedListener2.getPosition(level).ifPresent(vec3 -> {
                DebugRenderer.renderFloatingText("Listener Origin", vec3.x(), vec3.y() + (double)1.8f, vec3.z(), -1, 0.025f);
                DebugRenderer.renderFloatingText(new BlockPos((Vec3)vec3).toString(), vec3.x(), vec3.y() + 1.5, vec3.z(), -6959665, 0.025f);
            });
        }
        for (TrackedGameEvent trackedGameEvent : this.trackedGameEvents) {
            Vec3 vec322 = trackedGameEvent.position;
            double g = 0.2f;
            double h = vec322.x - (double)0.2f;
            double i = vec322.y - (double)0.2f;
            double j = vec322.z - (double)0.2f;
            double k = vec322.x + (double)0.2f;
            double l = vec322.y + (double)0.2f + 0.5;
            double m = vec322.z + (double)0.2f;
            GameEventListenerRenderer.renderTransparentFilledBox(new AABB(h, i, j, k, l, m), 1.0f, 1.0f, 1.0f, 0.2f);
            DebugRenderer.renderFloatingText(trackedGameEvent.gameEvent.getName(), vec322.x, vec322.y + (double)0.85f, vec322.z, -7564911, 0.0075f);
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void renderTransparentFilledBox(AABB aABB, float f, float g, float h, float i) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Vec3 vec3 = camera.getPosition().reverse();
        DebugRenderer.renderFilledBox(aABB.move(vec3), f, g, h, i);
    }

    public void trackGameEvent(GameEvent gameEvent, Vec3 vec3) {
        this.trackedGameEvents.add(new TrackedGameEvent(Util.getMillis(), gameEvent, vec3));
    }

    public void trackListener(PositionSource positionSource, int i) {
        this.trackedListeners.add(new TrackedListener(positionSource, i));
    }

    @Environment(value=EnvType.CLIENT)
    static class TrackedListener
    implements GameEventListener {
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

    @Environment(value=EnvType.CLIENT)
    record TrackedGameEvent(long timeStamp, GameEvent gameEvent, Vec3 position) {
        public boolean isExpired() {
            return Util.getMillis() - this.timeStamp > 3000L;
        }
    }
}

