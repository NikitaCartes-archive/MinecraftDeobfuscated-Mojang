/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.stats;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public final class NetworkPacketSummary {
    private final PacketCountAndSize totalPacketCountAndSize;
    private final List<Pair<PacketIdentification, PacketCountAndSize>> largestSizeContributors;
    private final Duration recordingDuration;

    public NetworkPacketSummary(Duration duration, List<Pair<PacketIdentification, PacketCountAndSize>> list) {
        this.recordingDuration = duration;
        this.totalPacketCountAndSize = list.stream().map(Pair::getSecond).reduce(PacketCountAndSize::add).orElseGet(() -> new PacketCountAndSize(0L, 0L));
        this.largestSizeContributors = list.stream().sorted(Comparator.comparing(Pair::getSecond, PacketCountAndSize.SIZE_THEN_COUNT)).limit(10L).toList();
    }

    public double getCountsPerSecond() {
        return (double)this.totalPacketCountAndSize.totalCount / (double)this.recordingDuration.getSeconds();
    }

    public double getSizePerSecond() {
        return (double)this.totalPacketCountAndSize.totalCount / (double)this.recordingDuration.getSeconds();
    }

    public long getTotalCount() {
        return this.totalPacketCountAndSize.totalCount;
    }

    public long getTotalSize() {
        return this.totalPacketCountAndSize.totalSize;
    }

    public List<Pair<PacketIdentification, PacketCountAndSize>> largestSizeContributors() {
        return this.largestSizeContributors;
    }

    public record PacketCountAndSize(long totalCount, long totalSize) {
        static final Comparator<PacketCountAndSize> SIZE_THEN_COUNT = Comparator.comparing(PacketCountAndSize::totalSize).thenComparing(PacketCountAndSize::totalCount).reversed();

        PacketCountAndSize add(PacketCountAndSize packetCountAndSize) {
            return new PacketCountAndSize(this.totalCount + packetCountAndSize.totalCount, this.totalSize + packetCountAndSize.totalSize);
        }
    }

    public record PacketIdentification(PacketFlow direction, int protocolId, int packetId) {
        private static final Map<PacketIdentification, String> PACKET_NAME_BY_ID;

        public String packetName() {
            return PACKET_NAME_BY_ID.getOrDefault(this, "unknown");
        }

        public static PacketIdentification from(RecordedEvent recordedEvent) {
            return new PacketIdentification(recordedEvent.getEventType().getName().equals("minecraft.PacketSent") ? PacketFlow.CLIENTBOUND : PacketFlow.SERVERBOUND, recordedEvent.getInt("protocolId"), recordedEvent.getInt("packetId"));
        }

        static {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            for (ConnectionProtocol connectionProtocol : ConnectionProtocol.values()) {
                for (PacketFlow packetFlow : PacketFlow.values()) {
                    Int2ObjectMap<Class<Packet<?>>> int2ObjectMap = connectionProtocol.getPacketsByIds(packetFlow);
                    int2ObjectMap.forEach((BiConsumer<Integer, Class<Packet<?>>>)((BiConsumer<Integer, Class>)(integer, class_) -> builder.put(new PacketIdentification(packetFlow, connectionProtocol.getId(), (int)integer), class_.getSimpleName())));
                }
            }
            PACKET_NAME_BY_ID = builder.build();
        }
    }
}

