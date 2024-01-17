package net.minecraft.network.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.PacketListener;

public interface BundlerInfo {
	int BUNDLE_SIZE_LIMIT = 4096;

	static <T extends PacketListener, P extends BundlePacket<? super T>> BundlerInfo createForPacket(
		PacketType<P> packetType, Function<Iterable<Packet<? super T>>, P> function, BundleDelimiterPacket<? super T> bundleDelimiterPacket
	) {
		return new BundlerInfo() {
			@Override
			public void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
				if (packet.type() == packetType) {
					P bundlePacket = (P)packet;
					consumer.accept(bundleDelimiterPacket);
					bundlePacket.subPackets().forEach(consumer);
					consumer.accept(bundleDelimiterPacket);
				} else {
					consumer.accept(packet);
				}
			}

			@Nullable
			@Override
			public BundlerInfo.Bundler startPacketBundling(Packet<?> packet) {
				return packet == bundleDelimiterPacket ? new BundlerInfo.Bundler() {
					private final List<Packet<? super T>> bundlePackets = new ArrayList();

					@Nullable
					@Override
					public Packet<?> addPacket(Packet<?> packet) {
						if (packet == bundleDelimiterPacket) {
							return (Packet<?>)function.apply(this.bundlePackets);
						} else if (this.bundlePackets.size() >= 4096) {
							throw new IllegalStateException("Too many packets in a bundle");
						} else {
							this.bundlePackets.add(packet);
							return null;
						}
					}
				} : null;
			}
		};
	}

	void unbundlePacket(Packet<?> packet, Consumer<Packet<?>> consumer);

	@Nullable
	BundlerInfo.Bundler startPacketBundling(Packet<?> packet);

	public interface Bundler {
		@Nullable
		Packet<?> addPacket(Packet<?> packet);
	}
}
