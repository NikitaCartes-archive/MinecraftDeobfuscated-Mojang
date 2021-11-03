/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr;

import java.net.SocketAddress;
import java.nio.file.Path;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JfrProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public interface JvmProfiler {
    public static final JvmProfiler INSTANCE = Runtime.class.getModule().getLayer().findModule("jdk.jfr").isPresent() ? JfrProfiler.getInstance() : new NoOpProfiler();

    public boolean start(Environment var1);

    public Path stop();

    public boolean isRunning();

    public boolean isAvailable();

    public void onServerTick(float var1);

    public void onPacketReceived(int var1, int var2, SocketAddress var3, int var4);

    public void onPacketSent(int var1, int var2, SocketAddress var3, int var4);

    @Nullable
    public ProfiledDuration onWorldLoadedStarted();

    @Nullable
    public ProfiledDuration onChunkGenerate(ChunkPos var1, ResourceKey<Level> var2, String var3);

    public static class NoOpProfiler
    implements JvmProfiler {
        static final Logger LOGGER = LogManager.getLogger();
        static final ProfiledDuration noOpCommit = () -> {};

        @Override
        public boolean start(Environment environment) {
            LOGGER.warn("Attempted to start Flight Recorder, but it's not supported on this JVM");
            return false;
        }

        @Override
        public Path stop() {
            throw new IllegalStateException("Attempted to stop Flight Recorder, but it's not supported on this JVM");
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public void onPacketReceived(int i, int j, SocketAddress socketAddress, int k) {
        }

        @Override
        public void onPacketSent(int i, int j, SocketAddress socketAddress, int k) {
        }

        @Override
        public void onServerTick(float f) {
        }

        @Override
        public ProfiledDuration onWorldLoadedStarted() {
            return noOpCommit;
        }

        @Override
        @Nullable
        public ProfiledDuration onChunkGenerate(ChunkPos chunkPos, ResourceKey<Level> resourceKey, String string) {
            return null;
        }
    }
}

