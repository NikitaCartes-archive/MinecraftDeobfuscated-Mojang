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
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

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
        BlockPos blockPos2 = new BlockPos(d, 0.0, f);
        this.trackedGameEvents.removeIf(TrackedGameEvent::isExpired);
        this.trackedListeners.removeIf(trackedListener -> trackedListener.isExpired(level, blockPos2));
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        for (TrackedListener trackedListener2 : this.trackedListeners) {
            trackedListener2.getPosition(level).ifPresent(blockPos -> {
                int i = blockPos.getX() - trackedListener2.getListenerRadius();
                int j = blockPos.getY() - trackedListener2.getListenerRadius();
                int k = blockPos.getZ() - trackedListener2.getListenerRadius();
                int l = blockPos.getX() + trackedListener2.getListenerRadius();
                int m = blockPos.getY() + trackedListener2.getListenerRadius();
                int n = blockPos.getZ() + trackedListener2.getListenerRadius();
                Vector3f vector3f = new Vector3f(1.0f, 1.0f, 0.0f);
                LevelRenderer.renderVoxelShape(poseStack, vertexConsumer, Shapes.create(new AABB(i, j, k, l, m, n)), -d, -e, -f, vector3f.x(), vector3f.y(), vector3f.z(), 0.35f);
            });
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (TrackedListener trackedListener2 : this.trackedListeners) {
            trackedListener2.getPosition(level).ifPresent(blockPos -> {
                Vector3f vector3f = new Vector3f(1.0f, 1.0f, 0.0f);
                LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, (double)((float)blockPos.getX() - 0.25f) - d, (double)blockPos.getY() - e, (double)((float)blockPos.getZ() - 0.25f) - f, (double)((float)blockPos.getX() + 0.25f) - d, (double)blockPos.getY() - e + 1.0, (double)((float)blockPos.getZ() + 0.25f) - f, vector3f.x(), vector3f.y(), vector3f.z(), 0.35f);
            });
        }
        tesselator.end();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        RenderSystem.depthMask(false);
        for (TrackedListener trackedListener2 : this.trackedListeners) {
            trackedListener2.getPosition(level).ifPresent(blockPos -> {
                DebugRenderer.renderFloatingText("Listener Origin", blockPos.getX(), (float)blockPos.getY() + 1.8f, blockPos.getZ(), -1, 0.025f);
                DebugRenderer.renderFloatingText(new BlockPos((Vec3i)blockPos).toString(), blockPos.getX(), (float)blockPos.getY() + 1.5f, blockPos.getZ(), -6959665, 0.025f);
            });
        }
        for (TrackedGameEvent trackedGameEvent : this.trackedGameEvents) {
            Vec3 vec3 = trackedGameEvent.position;
            double g = 0.2f;
            double h = vec3.x - (double)0.2f;
            double i = vec3.y - (double)0.2f;
            double j = vec3.z - (double)0.2f;
            double k = vec3.x + (double)0.2f;
            double l = vec3.y + (double)0.2f + 0.5;
            double m = vec3.z + (double)0.2f;
            GameEventListenerRenderer.renderTransparentFilledBox(new AABB(h, i, j, k, l, m), 1.0f, 1.0f, 1.0f, 0.2f);
            DebugRenderer.renderFloatingText(trackedGameEvent.gameEvent.getName(), vec3.x, vec3.y + (double)0.85f, vec3.z, -7564911, 0.0075f);
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

    public void trackGameEvent(GameEvent gameEvent, BlockPos blockPos) {
        this.trackedGameEvents.add(new TrackedGameEvent(Util.getMillis(), gameEvent, Vec3.atBottomCenterOf(blockPos)));
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

        public boolean isExpired(Level level, BlockPos blockPos) {
            Optional<BlockPos> optional = this.listenerSource.getPosition(level);
            return !optional.isPresent() || optional.get().distSqr(blockPos) <= 1024.0;
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

    @Environment(value=EnvType.CLIENT)
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
}

