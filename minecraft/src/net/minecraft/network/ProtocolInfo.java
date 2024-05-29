package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.VisibleForDebug;

public interface ProtocolInfo<T extends PacketListener> {
	ConnectionProtocol id();

	PacketFlow flow();

	StreamCodec<ByteBuf, Packet<? super T>> codec();

	@Nullable
	BundlerInfo bundlerInfo();

	public interface Unbound<T extends PacketListener, B extends ByteBuf> {
		ProtocolInfo<T> bind(Function<ByteBuf, B> function);

		ConnectionProtocol id();

		PacketFlow flow();

		@VisibleForDebug
		void listPackets(ProtocolInfo.Unbound.PacketVisitor packetVisitor);

		@FunctionalInterface
		public interface PacketVisitor {
			void accept(PacketType<?> packetType, int i);
		}
	}
}
