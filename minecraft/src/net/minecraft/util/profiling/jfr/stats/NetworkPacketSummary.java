package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import jdk.jfr.consumer.RecordedEvent;

public final class NetworkPacketSummary {
	private final NetworkPacketSummary.PacketCountAndSize totalPacketCountAndSize;
	private final List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> largestSizeContributors;
	private final Duration recordingDuration;

	public NetworkPacketSummary(Duration duration, List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> list) {
		this.recordingDuration = duration;
		this.totalPacketCountAndSize = (NetworkPacketSummary.PacketCountAndSize)list.stream()
			.map(Pair::getSecond)
			.reduce(NetworkPacketSummary.PacketCountAndSize::add)
			.orElseGet(() -> new NetworkPacketSummary.PacketCountAndSize(0L, 0L));
		this.largestSizeContributors = list.stream()
			.sorted(Comparator.comparing(Pair::getSecond, NetworkPacketSummary.PacketCountAndSize.SIZE_THEN_COUNT))
			.limit(10L)
			.toList();
	}

	public double getCountsPerSecond() {
		return (double)this.totalPacketCountAndSize.totalCount / (double)this.recordingDuration.getSeconds();
	}

	public double getSizePerSecond() {
		return (double)this.totalPacketCountAndSize.totalSize / (double)this.recordingDuration.getSeconds();
	}

	public long getTotalCount() {
		return this.totalPacketCountAndSize.totalCount;
	}

	public long getTotalSize() {
		return this.totalPacketCountAndSize.totalSize;
	}

	public List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> largestSizeContributors() {
		return this.largestSizeContributors;
	}

	public static record PacketCountAndSize(long totalCount, long totalSize) {
		static final Comparator<NetworkPacketSummary.PacketCountAndSize> SIZE_THEN_COUNT = Comparator.comparing(NetworkPacketSummary.PacketCountAndSize::totalSize)
			.thenComparing(NetworkPacketSummary.PacketCountAndSize::totalCount)
			.reversed();

		NetworkPacketSummary.PacketCountAndSize add(NetworkPacketSummary.PacketCountAndSize packetCountAndSize) {
			return new NetworkPacketSummary.PacketCountAndSize(this.totalCount + packetCountAndSize.totalCount, this.totalSize + packetCountAndSize.totalSize);
		}
	}

	public static record PacketIdentification(String direction, String protocolId, String packetId) {
		public static NetworkPacketSummary.PacketIdentification from(RecordedEvent recordedEvent) {
			return new NetworkPacketSummary.PacketIdentification(
				recordedEvent.getString("packetDirection"), recordedEvent.getString("protocolId"), recordedEvent.getString("packetId")
			);
		}
	}
}
