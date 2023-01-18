/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol;

import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.Nullable;

public interface BundlerInfo {
    public static final AttributeKey<Provider> BUNDLER_PROVIDER = AttributeKey.valueOf("bundler");
    public static final int BUNDLE_SIZE_LIMIT = 4096;
    public static final BundlerInfo EMPTY = new BundlerInfo(){

        @Override
        public void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
            consumer.accept(packet);
        }

        @Override
        @Nullable
        public Bundler startPacketBundling(Packet<?> packet) {
            return null;
        }
    };

    public static <T extends PacketListener, P extends BundlePacket<T>> BundlerInfo createForPacket(final Class<P> class_, final Function<Iterable<Packet<T>>, P> function, final BundleDelimiterPacket<T> bundleDelimiterPacket) {
        return new BundlerInfo(){

            @Override
            public void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
                if (packet.getClass() == class_) {
                    BundlePacket bundlePacket = (BundlePacket)packet;
                    consumer.accept(bundleDelimiterPacket);
                    bundlePacket.subPackets().forEach(consumer);
                    consumer.accept(bundleDelimiterPacket);
                } else {
                    consumer.accept(packet);
                }
            }

            @Override
            @Nullable
            public Bundler startPacketBundling(Packet<?> packet) {
                if (packet == bundleDelimiterPacket) {
                    return new Bundler(){
                        private final List<Packet<T>> bundlePackets = new ArrayList();

                        @Override
                        @Nullable
                        public Packet<?> addPacket(Packet<?> packet) {
                            if (packet == bundleDelimiterPacket) {
                                return (Packet)function.apply(this.bundlePackets);
                            }
                            Packet<?> packet2 = packet;
                            if (this.bundlePackets.size() >= 4096) {
                                throw new IllegalStateException("Too many packets in a bundle");
                            }
                            this.bundlePackets.add(packet2);
                            return null;
                        }
                    };
                }
                return null;
            }
        };
    }

    public void unbundlePacket(Packet<?> var1, Consumer<Packet<?>> var2);

    @Nullable
    public Bundler startPacketBundling(Packet<?> var1);

    public static interface Provider {
        public BundlerInfo getBundlerInfo(PacketFlow var1);
    }

    public static interface Bundler {
        @Nullable
        public Packet<?> addPacket(Packet<?> var1);
    }
}

