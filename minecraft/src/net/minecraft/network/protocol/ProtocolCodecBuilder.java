package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;

public class ProtocolCodecBuilder<B extends ByteBuf, L extends PacketListener> {
	private final IdDispatchCodec.Builder<B, Packet<? super L>, PacketType<? extends Packet<? super L>>> dispatchBuilder = IdDispatchCodec.builder(Packet::type);
	private final PacketFlow flow;

	public ProtocolCodecBuilder(PacketFlow packetFlow) {
		this.flow = packetFlow;
	}

	public <T extends Packet<? super L>> ProtocolCodecBuilder<B, L> add(PacketType<T> packetType, StreamCodec<? super B, T> streamCodec) {
		if (packetType.flow() != this.flow) {
			throw new IllegalArgumentException("Invalid packet flow for packet " + packetType + ", expected " + this.flow.name());
		} else {
			this.dispatchBuilder.add(packetType, streamCodec);
			return this;
		}
	}

	public StreamCodec<B, Packet<? super L>> build() {
		return this.dispatchBuilder.build();
	}
}
