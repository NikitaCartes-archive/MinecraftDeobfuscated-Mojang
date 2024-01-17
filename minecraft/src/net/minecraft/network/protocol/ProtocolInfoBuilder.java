package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.codec.StreamCodec;

public class ProtocolInfoBuilder<T extends PacketListener, B extends ByteBuf> {
	private final ConnectionProtocol protocol;
	private final PacketFlow flow;
	private final List<ProtocolInfoBuilder.CodecEntry<T, ?, B>> codecs = new ArrayList();
	@Nullable
	private BundlerInfo bundlerInfo;

	public ProtocolInfoBuilder(ConnectionProtocol connectionProtocol, PacketFlow packetFlow) {
		this.protocol = connectionProtocol;
		this.flow = packetFlow;
	}

	public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B> addPacket(PacketType<P> packetType, StreamCodec<? super B, P> streamCodec) {
		this.codecs.add(new ProtocolInfoBuilder.CodecEntry<>(packetType, streamCodec));
		return this;
	}

	public <P extends BundlePacket<? super T>, D extends BundleDelimiterPacket<? super T>> ProtocolInfoBuilder<T, B> withBundlePacket(
		PacketType<P> packetType, Function<Iterable<Packet<? super T>>, P> function, D bundleDelimiterPacket
	) {
		StreamCodec<ByteBuf, D> streamCodec = StreamCodec.unit(bundleDelimiterPacket);
		PacketType<D> packetType2 = (PacketType<D>)bundleDelimiterPacket.type();
		this.codecs.add(new ProtocolInfoBuilder.CodecEntry<>(packetType2, streamCodec));
		this.bundlerInfo = BundlerInfo.createForPacket(packetType, function, bundleDelimiterPacket);
		return this;
	}

	private StreamCodec<ByteBuf, Packet<? super T>> buildPacketCodec(Function<ByteBuf, B> function, List<ProtocolInfoBuilder.CodecEntry<T, ?, B>> list) {
		ProtocolCodecBuilder<ByteBuf, T> protocolCodecBuilder = new ProtocolCodecBuilder<>(this.flow);

		for (ProtocolInfoBuilder.CodecEntry<T, ?, B> codecEntry : list) {
			codecEntry.addToBuilder(protocolCodecBuilder, function);
		}

		return protocolCodecBuilder.build();
	}

	public ProtocolInfo<T> build(Function<ByteBuf, B> function) {
		return new ProtocolInfoBuilder.Implementation<>(this.protocol, this.flow, this.buildPacketCodec(function, this.codecs), this.bundlerInfo);
	}

	public ProtocolInfo.Unbound<T, B> buildUnbound() {
		List<ProtocolInfoBuilder.CodecEntry<T, ?, B>> list = List.copyOf(this.codecs);
		BundlerInfo bundlerInfo = this.bundlerInfo;
		return function -> new ProtocolInfoBuilder.Implementation<>(this.protocol, this.flow, this.buildPacketCodec(function, list), bundlerInfo);
	}

	private static <L extends PacketListener> ProtocolInfo<L> protocol(
		ConnectionProtocol connectionProtocol, PacketFlow packetFlow, Consumer<ProtocolInfoBuilder<L, FriendlyByteBuf>> consumer
	) {
		ProtocolInfoBuilder<L, FriendlyByteBuf> protocolInfoBuilder = new ProtocolInfoBuilder<>(connectionProtocol, packetFlow);
		consumer.accept(protocolInfoBuilder);
		return protocolInfoBuilder.build(FriendlyByteBuf::new);
	}

	public static <T extends ServerboundPacketListener> ProtocolInfo<T> serverboundProtocol(
		ConnectionProtocol connectionProtocol, Consumer<ProtocolInfoBuilder<T, FriendlyByteBuf>> consumer
	) {
		return protocol(connectionProtocol, PacketFlow.SERVERBOUND, consumer);
	}

	public static <T extends ClientboundPacketListener> ProtocolInfo<T> clientboundProtocol(
		ConnectionProtocol connectionProtocol, Consumer<ProtocolInfoBuilder<T, FriendlyByteBuf>> consumer
	) {
		return protocol(connectionProtocol, PacketFlow.CLIENTBOUND, consumer);
	}

	private static <L extends PacketListener, B extends ByteBuf> ProtocolInfo.Unbound<L, B> protocolUnbound(
		ConnectionProtocol connectionProtocol, PacketFlow packetFlow, Consumer<ProtocolInfoBuilder<L, B>> consumer
	) {
		ProtocolInfoBuilder<L, B> protocolInfoBuilder = new ProtocolInfoBuilder<>(connectionProtocol, packetFlow);
		consumer.accept(protocolInfoBuilder);
		return protocolInfoBuilder.buildUnbound();
	}

	public static <T extends ServerboundPacketListener, B extends ByteBuf> ProtocolInfo.Unbound<T, B> serverboundProtocolUnbound(
		ConnectionProtocol connectionProtocol, Consumer<ProtocolInfoBuilder<T, B>> consumer
	) {
		return protocolUnbound(connectionProtocol, PacketFlow.SERVERBOUND, consumer);
	}

	public static <T extends ClientboundPacketListener, B extends ByteBuf> ProtocolInfo.Unbound<T, B> clientboundProtocolUnbound(
		ConnectionProtocol connectionProtocol, Consumer<ProtocolInfoBuilder<T, B>> consumer
	) {
		return protocolUnbound(connectionProtocol, PacketFlow.CLIENTBOUND, consumer);
	}

	static record CodecEntry<T extends PacketListener, P extends Packet<? super T>, B extends ByteBuf>(PacketType<P> type, StreamCodec<? super B, P> serializer) {
		public void addToBuilder(ProtocolCodecBuilder<ByteBuf, T> protocolCodecBuilder, Function<ByteBuf, B> function) {
			StreamCodec<ByteBuf, P> streamCodec = this.serializer.mapStream(function);
			protocolCodecBuilder.add(this.type, streamCodec);
		}
	}

	static record Implementation<L extends PacketListener>(
		ConnectionProtocol id, PacketFlow flow, StreamCodec<ByteBuf, Packet<? super L>> codec, @Nullable BundlerInfo bundlerInfo
	) implements ProtocolInfo<L> {
	}
}
