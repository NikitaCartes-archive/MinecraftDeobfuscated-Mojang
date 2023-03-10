/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.audio;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.blaze3d.audio.OpenAlUtil;
import com.mojang.logging.LogUtils;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.openal.SOFTHRTF;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Library {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int NO_DEVICE = 0;
    private static final int DEFAULT_CHANNEL_COUNT = 30;
    private long currentDevice;
    private long context;
    private boolean supportsDisconnections;
    @Nullable
    private String defaultDeviceName;
    private static final ChannelPool EMPTY = new ChannelPool(){

        @Override
        @Nullable
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
    private ChannelPool staticChannels = EMPTY;
    private ChannelPool streamingChannels = EMPTY;
    private final Listener listener = new Listener();

    public Library() {
        this.defaultDeviceName = Library.getDefaultDeviceName();
    }

    public void init(@Nullable String string, boolean bl) {
        this.currentDevice = Library.openDeviceOrFallback(string);
        this.supportsDisconnections = ALC10.alcIsExtensionPresent(this.currentDevice, "ALC_EXT_disconnect");
        ALCCapabilities aLCCapabilities = ALC.createCapabilities(this.currentDevice);
        if (OpenAlUtil.checkALCError(this.currentDevice, "Get capabilities")) {
            throw new IllegalStateException("Failed to get OpenAL capabilities");
        }
        if (!aLCCapabilities.OpenALC11) {
            throw new IllegalStateException("OpenAL 1.1 not supported");
        }
        this.setHrtf(aLCCapabilities.ALC_SOFT_HRTF && bl);
        this.context = ALC10.alcCreateContext(this.currentDevice, (IntBuffer)null);
        ALC10.alcMakeContextCurrent(this.context);
        int i = this.getChannelCount();
        int j = Mth.clamp((int)Mth.sqrt(i), 2, 8);
        int k = Mth.clamp(i - j, 8, 255);
        this.staticChannels = new CountingChannelPool(k);
        this.streamingChannels = new CountingChannelPool(j);
        ALCapabilities aLCapabilities = AL.createCapabilities(aLCCapabilities);
        OpenAlUtil.checkALError("Initialization");
        if (!aLCapabilities.AL_EXT_source_distance_model) {
            throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
        }
        AL10.alEnable(512);
        if (!aLCapabilities.AL_EXT_LINEAR_DISTANCE) {
            throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
        }
        OpenAlUtil.checkALError("Enable per-source distance models");
        LOGGER.info("OpenAL initialized on device {}", (Object)this.getCurrentDeviceName());
    }

    private void setHrtf(boolean bl) {
        int i = ALC10.alcGetInteger(this.currentDevice, 6548);
        if (i > 0) {
            try (MemoryStack memoryStack = MemoryStack.stackPush();){
                IntBuffer intBuffer = memoryStack.callocInt(10).put(6546).put(bl ? 1 : 0).put(6550).put(0).put(0).flip();
                if (!SOFTHRTF.alcResetDeviceSOFT(this.currentDevice, intBuffer)) {
                    LOGGER.warn("Failed to reset device: {}", (Object)ALC10.alcGetString(this.currentDevice, ALC10.alcGetError(this.currentDevice)));
                }
            }
        }
    }

    private int getChannelCount() {
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            int i = ALC10.alcGetInteger(this.currentDevice, 4098);
            if (OpenAlUtil.checkALCError(this.currentDevice, "Get attributes size")) {
                throw new IllegalStateException("Failed to get OpenAL attributes");
            }
            IntBuffer intBuffer = memoryStack.mallocInt(i);
            ALC10.alcGetIntegerv(this.currentDevice, 4099, intBuffer);
            if (OpenAlUtil.checkALCError(this.currentDevice, "Get attributes")) {
                throw new IllegalStateException("Failed to get OpenAL attributes");
            }
            int j = 0;
            while (j < i) {
                int k;
                if ((k = intBuffer.get(j++)) == 0) {
                    break;
                }
                int l = intBuffer.get(j++);
                if (k != 4112) continue;
                int n = l;
                return n;
            }
        }
        return 30;
    }

    @Nullable
    public static String getDefaultDeviceName() {
        if (!ALC10.alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT")) {
            return null;
        }
        ALUtil.getStringList(0L, 4115);
        return ALC10.alcGetString(0L, 4114);
    }

    public String getCurrentDeviceName() {
        String string = ALC10.alcGetString(this.currentDevice, 4115);
        if (string == null) {
            string = ALC10.alcGetString(this.currentDevice, 4101);
        }
        if (string == null) {
            string = "Unknown";
        }
        return string;
    }

    public synchronized boolean hasDefaultDeviceChanged() {
        String string = Library.getDefaultDeviceName();
        if (Objects.equals(this.defaultDeviceName, string)) {
            return false;
        }
        this.defaultDeviceName = string;
        return true;
    }

    private static long openDeviceOrFallback(@Nullable String string) {
        OptionalLong optionalLong = OptionalLong.empty();
        if (string != null) {
            optionalLong = Library.tryOpenDevice(string);
        }
        if (optionalLong.isEmpty()) {
            optionalLong = Library.tryOpenDevice(Library.getDefaultDeviceName());
        }
        if (optionalLong.isEmpty()) {
            optionalLong = Library.tryOpenDevice(null);
        }
        if (optionalLong.isEmpty()) {
            throw new IllegalStateException("Failed to open OpenAL device");
        }
        return optionalLong.getAsLong();
    }

    private static OptionalLong tryOpenDevice(@Nullable String string) {
        long l = ALC10.alcOpenDevice(string);
        if (l != 0L && !OpenAlUtil.checkALCError(l, "Open device")) {
            return OptionalLong.of(l);
        }
        return OptionalLong.empty();
    }

    public void cleanup() {
        this.staticChannels.cleanup();
        this.streamingChannels.cleanup();
        ALC10.alcDestroyContext(this.context);
        if (this.currentDevice != 0L) {
            ALC10.alcCloseDevice(this.currentDevice);
        }
    }

    public Listener getListener() {
        return this.listener;
    }

    @Nullable
    public Channel acquireChannel(Pool pool) {
        return (pool == Pool.STREAMING ? this.streamingChannels : this.staticChannels).acquire();
    }

    public void releaseChannel(Channel channel) {
        if (!this.staticChannels.release(channel) && !this.streamingChannels.release(channel)) {
            throw new IllegalStateException("Tried to release unknown channel");
        }
    }

    public String getDebugString() {
        return String.format(Locale.ROOT, "Sounds: %d/%d + %d/%d", this.staticChannels.getUsedCount(), this.staticChannels.getMaxCount(), this.streamingChannels.getUsedCount(), this.streamingChannels.getMaxCount());
    }

    public List<String> getAvailableSoundDevices() {
        List<String> list = ALUtil.getStringList(0L, 4115);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    public boolean isCurrentDeviceDisconnected() {
        return this.supportsDisconnections && ALC11.alcGetInteger(this.currentDevice, 787) == 0;
    }

    @Environment(value=EnvType.CLIENT)
    static interface ChannelPool {
        @Nullable
        public Channel acquire();

        public boolean release(Channel var1);

        public void cleanup();

        public int getMaxCount();

        public int getUsedCount();
    }

    @Environment(value=EnvType.CLIENT)
    static class CountingChannelPool
    implements ChannelPool {
        private final int limit;
        private final Set<Channel> activeChannels = Sets.newIdentityHashSet();

        public CountingChannelPool(int i) {
            this.limit = i;
        }

        @Override
        @Nullable
        public Channel acquire() {
            if (this.activeChannels.size() >= this.limit) {
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    LOGGER.warn("Maximum sound pool size {} reached", (Object)this.limit);
                }
                return null;
            }
            Channel channel = Channel.create();
            if (channel != null) {
                this.activeChannels.add(channel);
            }
            return channel;
        }

        @Override
        public boolean release(Channel channel) {
            if (!this.activeChannels.remove(channel)) {
                return false;
            }
            channel.destroy();
            return true;
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

    @Environment(value=EnvType.CLIENT)
    public static enum Pool {
        STATIC,
        STREAMING;

    }
}

