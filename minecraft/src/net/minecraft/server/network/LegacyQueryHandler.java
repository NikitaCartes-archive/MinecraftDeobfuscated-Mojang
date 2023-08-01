package net.minecraft.server.network;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.SocketAddress;
import java.util.Locale;
import net.minecraft.server.ServerInfo;
import org.slf4j.Logger;

public class LegacyQueryHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ServerInfo server;

	public LegacyQueryHandler(ServerInfo serverInfo) {
		this.server = serverInfo;
	}

	@Override
	public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
		ByteBuf byteBuf = (ByteBuf)object;
		byteBuf.markReaderIndex();
		boolean bl = true;

		try {
			try {
				if (byteBuf.readUnsignedByte() != 254) {
					return;
				}

				SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
				int i = byteBuf.readableBytes();
				if (i == 0) {
					LOGGER.debug("Ping: (<1.3.x) from {}", socketAddress);
					String string = createVersion0Response(this.server);
					sendFlushAndClose(channelHandlerContext, createLegacyDisconnectPacket(channelHandlerContext.alloc(), string));
				} else {
					if (byteBuf.readUnsignedByte() != 1) {
						return;
					}

					if (byteBuf.isReadable()) {
						if (!readCustomPayloadPacket(byteBuf)) {
							return;
						}

						LOGGER.debug("Ping: (1.6) from {}", socketAddress);
					} else {
						LOGGER.debug("Ping: (1.4-1.5.x) from {}", socketAddress);
					}

					String string = createVersion1Response(this.server);
					sendFlushAndClose(channelHandlerContext, createLegacyDisconnectPacket(channelHandlerContext.alloc(), string));
				}

				byteBuf.release();
				bl = false;
			} catch (RuntimeException var11) {
			}
		} finally {
			if (bl) {
				byteBuf.resetReaderIndex();
				channelHandlerContext.channel().pipeline().remove(this);
				channelHandlerContext.fireChannelRead(object);
			}
		}
	}

	private static boolean readCustomPayloadPacket(ByteBuf byteBuf) {
		short s = byteBuf.readUnsignedByte();
		if (s != 250) {
			return false;
		} else {
			String string = LegacyProtocolUtils.readLegacyString(byteBuf);
			if (!"MC|PingHost".equals(string)) {
				return false;
			} else {
				int i = byteBuf.readUnsignedShort();
				if (byteBuf.readableBytes() != i) {
					return false;
				} else {
					short t = byteBuf.readUnsignedByte();
					if (t < 73) {
						return false;
					} else {
						String string2 = LegacyProtocolUtils.readLegacyString(byteBuf);
						int j = byteBuf.readInt();
						return j <= 65535;
					}
				}
			}
		}
	}

	private static String createVersion0Response(ServerInfo serverInfo) {
		return String.format(Locale.ROOT, "%s§%d§%d", serverInfo.getMotd(), serverInfo.getPlayerCount(), serverInfo.getMaxPlayers());
	}

	private static String createVersion1Response(ServerInfo serverInfo) {
		return String.format(
			Locale.ROOT,
			"§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d",
			127,
			serverInfo.getServerVersion(),
			serverInfo.getMotd(),
			serverInfo.getPlayerCount(),
			serverInfo.getMaxPlayers()
		);
	}

	private static void sendFlushAndClose(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
		channelHandlerContext.pipeline().firstContext().writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE);
	}

	private static ByteBuf createLegacyDisconnectPacket(ByteBufAllocator byteBufAllocator, String string) {
		ByteBuf byteBuf = byteBufAllocator.buffer();
		byteBuf.writeByte(255);
		LegacyProtocolUtils.writeLegacyString(byteBuf, string);
		return byteBuf;
	}
}
