package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.net.SocketAddress;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public interface JvmProfiler {
	JvmProfiler INSTANCE = (JvmProfiler)(Runtime.class.getModule().getLayer().findModule("jdk.jfr").isPresent()
		? JfrProfiler.getInstance()
		: new JvmProfiler.NoOpProfiler());

	boolean start(Environment environment);

	Path stop();

	boolean isRunning();

	boolean isAvailable();

	void onServerTick(float f);

	void onPacketReceived(ConnectionProtocol connectionProtocol, int i, SocketAddress socketAddress, int j);

	void onPacketSent(ConnectionProtocol connectionProtocol, int i, SocketAddress socketAddress, int j);

	@Nullable
	ProfiledDuration onWorldLoadedStarted();

	@Nullable
	ProfiledDuration onChunkGenerate(ChunkPos chunkPos, ResourceKey<Level> resourceKey, String string);

	public static class NoOpProfiler implements JvmProfiler {
		private static final Logger LOGGER = LogUtils.getLogger();
		static final ProfiledDuration noOpCommit = () -> {
		};

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
		public void onPacketReceived(ConnectionProtocol connectionProtocol, int i, SocketAddress socketAddress, int j) {
		}

		@Override
		public void onPacketSent(ConnectionProtocol connectionProtocol, int i, SocketAddress socketAddress, int j) {
		}

		@Override
		public void onServerTick(float f) {
		}

		@Override
		public ProfiledDuration onWorldLoadedStarted() {
			return noOpCommit;
		}

		@Nullable
		@Override
		public ProfiledDuration onChunkGenerate(ChunkPos chunkPos, ResourceKey<Level> resourceKey, String string) {
			return null;
		}
	}
}
