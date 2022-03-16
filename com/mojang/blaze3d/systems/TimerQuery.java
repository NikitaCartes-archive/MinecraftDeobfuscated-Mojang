/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.ARBTimerQuery;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32C;

@Environment(value=EnvType.CLIENT)
public class TimerQuery {
    private int nextQueryName;

    public static Optional<TimerQuery> getInstance() {
        return TimerQueryLazyLoader.INSTANCE;
    }

    public void beginProfile() {
        RenderSystem.assertOnRenderThread();
        if (this.nextQueryName != 0) {
            throw new IllegalStateException("Current profile not ended");
        }
        this.nextQueryName = GL32C.glGenQueries();
        GL32C.glBeginQuery(35007, this.nextQueryName);
    }

    public FrameProfile endProfile() {
        RenderSystem.assertOnRenderThread();
        if (this.nextQueryName == 0) {
            throw new IllegalStateException("endProfile called before beginProfile");
        }
        GL32C.glEndQuery(35007);
        FrameProfile frameProfile = new FrameProfile(this.nextQueryName);
        this.nextQueryName = 0;
        return frameProfile;
    }

    @Environment(value=EnvType.CLIENT)
    static class TimerQueryLazyLoader {
        static final Optional<TimerQuery> INSTANCE = Optional.ofNullable(TimerQueryLazyLoader.instantiate());

        private TimerQueryLazyLoader() {
        }

        @Nullable
        private static TimerQuery instantiate() {
            if (!GL.getCapabilities().GL_ARB_timer_query) {
                return null;
            }
            return new TimerQuery();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FrameProfile {
        private static final long NO_RESULT = 0L;
        private static final long CANCELLED_RESULT = -1L;
        private final int queryName;
        private long result;

        FrameProfile(int i) {
            this.queryName = i;
        }

        public void cancel() {
            RenderSystem.assertOnRenderThread();
            if (this.result != 0L) {
                return;
            }
            this.result = -1L;
            GL32C.glDeleteQueries(this.queryName);
        }

        public boolean isDone() {
            RenderSystem.assertOnRenderThread();
            if (this.result != 0L) {
                return true;
            }
            if (1 == GL32C.glGetQueryObjecti(this.queryName, 34919)) {
                this.result = ARBTimerQuery.glGetQueryObjecti64(this.queryName, 34918);
                GL32C.glDeleteQueries(this.queryName);
                return true;
            }
            return false;
        }

        public long get() {
            RenderSystem.assertOnRenderThread();
            if (this.result == 0L) {
                this.result = ARBTimerQuery.glGetQueryObjecti64(this.queryName, 34918);
                GL32C.glDeleteQueries(this.queryName);
            }
            return this.result;
        }
    }
}

