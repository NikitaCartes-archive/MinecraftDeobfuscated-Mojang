package com.mojang.blaze3d.audio;

import com.google.common.collect.Sets;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.system.MemoryStack;

@Environment(EnvType.CLIENT)
public class Library {
	static final Logger LOGGER = LogManager.getLogger();
	private static final int DEFAULT_CHANNEL_COUNT = 30;
	private long device;
	private long context;
	private static final Library.ChannelPool EMPTY = new Library.ChannelPool() {
		@Nullable
		@Override
		public Channel acquire() {
			return null;
		}

		@Override
		public boolean release(Channel channel) {
			return false;
		}

		@Override
		public void cleanup() {
		}

		@Override
		public int getMaxCount() {
			return 0;
		}

		@Override
		public int getUsedCount() {
			return 0;
		}
	};
	private Library.ChannelPool staticChannels = EMPTY;
	private Library.ChannelPool streamingChannels = EMPTY;
	private final Listener listener = new Listener();

	public void init(@Nullable String string) {
		this.device = openDeviceOrFallback(string);
		ALCCapabilities aLCCapabilities = ALC.createCapabilities(this.device);
		if (OpenAlUtil.checkALCError(this.device, "Get capabilities")) {
			throw new IllegalStateException("Failed to get OpenAL capabilities");
		} else if (!aLCCapabilities.OpenALC11) {
			throw new IllegalStateException("OpenAL 1.1 not supported");
		} else {
			this.context = ALC10.alcCreateContext(this.device, (IntBuffer)null);
			ALC10.alcMakeContextCurrent(this.context);
			int i = this.getChannelCount();
			int j = Mth.clamp((int)Mth.sqrt((float)i), 2, 8);
			int k = Mth.clamp(i - j, 8, 255);
			this.staticChannels = new Library.CountingChannelPool(k);
			this.streamingChannels = new Library.CountingChannelPool(j);
			ALCapabilities aLCapabilities = AL.createCapabilities(aLCCapabilities);
			OpenAlUtil.checkALError("Initialization");
			if (!aLCapabilities.AL_EXT_source_distance_model) {
				throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
			} else {
				AL10.alEnable(512);
				if (!aLCapabilities.AL_EXT_LINEAR_DISTANCE) {
					throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
				} else {
					OpenAlUtil.checkALError("Enable per-source distance models");
					LOGGER.info("OpenAL initialized.");
				}
			}
		}
	}

	private int getChannelCount() {
		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			int i = ALC10.alcGetInteger(this.device, 4098);
			if (OpenAlUtil.checkALCError(this.device, "Get attributes size")) {
				throw new IllegalStateException("Failed to get OpenAL attributes");
			}

			IntBuffer intBuffer = memoryStack.mallocInt(i);
			ALC10.alcGetIntegerv(this.device, 4099, intBuffer);
			if (OpenAlUtil.checkALCError(this.device, "Get attributes")) {
				throw new IllegalStateException("Failed to get OpenAL attributes");
			}

			int j = 0;

			while (j < i) {
				int k = intBuffer.get(j++);
				if (k == 0) {
					break;
				}

				int l = intBuffer.get(j++);
				if (k == 4112) {
					return l;
				}
			}
		}

		return 30;
	}

	@Nullable
	public static String getDefaultDeviceName() {
		return !ALC10.alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT") ? null : ALC10.alcGetString(0L, 4115);
	}

	private static long openDeviceOrFallback(@Nullable String string) {
		OptionalLong optionalLong = OptionalLong.empty();
		if (string != null) {
			optionalLong = tryOpenDevice(string);
		}

		if (optionalLong.isEmpty()) {
			optionalLong = tryOpenDevice(getDefaultDeviceName());
		}

		if (optionalLong.isEmpty()) {
			optionalLong = tryOpenDevice(null);
		}

		if (optionalLong.isEmpty()) {
			throw new IllegalStateException("Failed to open OpenAL device");
		} else {
			return optionalLong.getAsLong();
		}
	}

	private static OptionalLong tryOpenDevice(@Nullable String string) {
		long l = ALC10.alcOpenDevice(string);
		return l != 0L && !OpenAlUtil.checkALCError(l, "Open device") ? OptionalLong.of(l) : OptionalLong.empty();
	}

	public void cleanup() {
		this.staticChannels.cleanup();
		this.streamingChannels.cleanup();
		ALC10.alcDestroyContext(this.context);
		if (this.device != 0L) {
			ALC10.alcCloseDevice(this.device);
		}
	}

	public Listener getListener() {
		return this.listener;
	}

	@Nullable
	public Channel acquireChannel(Library.Pool pool) {
		return (pool == Library.Pool.STREAMING ? this.streamingChannels : this.staticChannels).acquire();
	}

	public void releaseChannel(Channel channel) {
		if (!this.staticChannels.release(channel) && !this.streamingChannels.release(channel)) {
			throw new IllegalStateException("Tried to release unknown channel");
		}
	}

	public String getDebugString() {
		return String.format(
			"Sounds: %d/%d + %d/%d",
			this.staticChannels.getUsedCount(),
			this.staticChannels.getMaxCount(),
			this.streamingChannels.getUsedCount(),
			this.streamingChannels.getMaxCount()
		);
	}

	public List<String> getAvailableSoundDevices() {
		List<String> list = ALUtil.getStringList(0L, 4115);
		return list == null ? Collections.emptyList() : list;
	}

	@Environment(EnvType.CLIENT)
	interface ChannelPool {
		@Nullable
		Channel acquire();

		boolean release(Channel channel);

		void cleanup();

		int getMaxCount();

		int getUsedCount();
	}

	@Environment(EnvType.CLIENT)
	static class CountingChannelPool implements Library.ChannelPool {
		private final int limit;
		private final Set<Channel> activeChannels = Sets.newIdentityHashSet();

		public CountingChannelPool(int i) {
			this.limit = i;
		}

		@Nullable
		@Override
		public Channel acquire() {
			if (this.activeChannels.size() >= this.limit) {
				Library.LOGGER.warn("Maximum sound pool size {} reached", this.limit);
				return null;
			} else {
				Channel channel = Channel.create();
				if (channel != null) {
					this.activeChannels.add(channel);
				}

				return channel;
			}
		}

		@Override
		public boolean release(Channel channel) {
			if (!this.activeChannels.remove(channel)) {
				return false;
			} else {
				channel.destroy();
				return true;
			}
		}

		@Override
		public void cleanup() {
			this.activeChannels.forEach(Channel::destroy);
			this.activeChannels.clear();
		}

		@Override
		public int getMaxCount() {
			return this.limit;
		}

		@Override
		public int getUsedCount() {
			return this.activeChannels.size();
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Pool {
		STATIC,
		STREAMING;
	}
}
