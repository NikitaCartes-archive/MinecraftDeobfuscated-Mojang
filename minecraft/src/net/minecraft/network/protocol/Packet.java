package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;

public interface Packet<T extends PacketListener> {
	PacketType<? extends Packet<T>> type();

	void handle(T packetListener);

	default boolean isSkippable() {
		return false;
	}

	default boolean isTerminal() {
		return false;
	}

	static <B extends ByteBuf, T extends Packet<?>> StreamCodec<B, T> codec(StreamMemberEncoder<B, T> streamMemberEncoder, StreamDecoder<B, T> streamDecoder) {
		return StreamCodec.ofMember(streamMemberEncoder, streamDecoder);
	}
}
