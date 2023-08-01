package net.minecraft.client.multiplayer;

import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.server.network.LegacyProtocolUtils;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class LegacyServerPinger extends SimpleChannelInboundHandler<ByteBuf> {
	private static final Splitter SPLITTER = Splitter.on('\u0000').limit(6);
	private final ServerAddress address;
	private final LegacyServerPinger.Output output;

	public LegacyServerPinger(ServerAddress serverAddress, LegacyServerPinger.Output output) {
		this.address = serverAddress;
		this.output = output;
	}

	@Override
	public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
		super.channelActive(channelHandlerContext);
		ByteBuf byteBuf = channelHandlerContext.alloc().buffer();

		try {
			byteBuf.writeByte(254);
			byteBuf.writeByte(1);
			byteBuf.writeByte(250);
			LegacyProtocolUtils.writeLegacyString(byteBuf, "MC|PingHost");
			int i = byteBuf.writerIndex();
			byteBuf.writeShort(0);
			int j = byteBuf.writerIndex();
			byteBuf.writeByte(127);
			LegacyProtocolUtils.writeLegacyString(byteBuf, this.address.getHost());
			byteBuf.writeInt(this.address.getPort());
			int k = byteBuf.writerIndex() - j;
			byteBuf.setShort(i, k);
			channelHandlerContext.channel().writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
		} catch (Exception var6) {
			byteBuf.release();
			throw var6;
		}
	}

	protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
		short s = byteBuf.readUnsignedByte();
		if (s == 255) {
			String string = LegacyProtocolUtils.readLegacyString(byteBuf);
			List<String> list = SPLITTER.splitToList(string);
			if ("§1".equals(list.get(0))) {
				int i = Mth.getInt((String)list.get(1), 0);
				String string2 = (String)list.get(2);
				String string3 = (String)list.get(3);
				int j = Mth.getInt((String)list.get(4), -1);
				int k = Mth.getInt((String)list.get(5), -1);
				this.output.handleResponse(i, string2, string3, j, k);
			}
		}

		channelHandlerContext.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
		channelHandlerContext.close();
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Output {
		void handleResponse(int i, String string, String string2, int j, int k);
	}
}
