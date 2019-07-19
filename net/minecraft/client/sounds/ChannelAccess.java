/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.sounds;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class ChannelAccess {
    private final Set<ChannelHandle> channels = Sets.newIdentityHashSet();
    private final Library library;
    private final Executor executor;

    public ChannelAccess(Library library, Executor executor) {
        this.library = library;
        this.executor = executor;
    }

    public ChannelHandle createHandle(Library.Pool pool) {
        ChannelHandle channelHandle = new ChannelHandle();
        this.executor.execute(() -> {
            Channel channel = this.library.acquireChannel(pool);
            if (channel != null) {
                channelHandle.channel = channel;
                this.channels.add(channelHandle);
            }
        });
        return channelHandle;
    }

    public void executeOnChannels(Consumer<Stream<Channel>> consumer) {
        this.executor.execute(() -> consumer.accept(this.channels.stream().map(channelHandle -> ((ChannelHandle)channelHandle).channel).filter(Objects::nonNull)));
    }

    public void scheduleTick() {
        this.executor.execute(() -> {
            Iterator<ChannelHandle> iterator = this.channels.iterator();
            while (iterator.hasNext()) {
                ChannelHandle channelHandle = iterator.next();
                channelHandle.channel.updateStream();
                if (!channelHandle.channel.stopped()) continue;
                channelHandle.release();
                iterator.remove();
            }
        });
    }

    public void clear() {
        this.channels.forEach(ChannelHandle::release);
        this.channels.clear();
    }

    @Environment(value=EnvType.CLIENT)
    public class ChannelHandle {
        private Channel channel;
        private boolean stopped;

        public boolean isStopped() {
            return this.stopped;
        }

        public void execute(Consumer<Channel> consumer) {
            ChannelAccess.this.executor.execute(() -> {
                if (this.channel != null) {
                    consumer.accept(this.channel);
                }
            });
        }

        public void release() {
            this.stopped = true;
            ChannelAccess.this.library.releaseChannel(this.channel);
            this.channel = null;
        }
    }
}

