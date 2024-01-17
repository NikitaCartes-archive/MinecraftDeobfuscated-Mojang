package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.protocol.Packet;

public class UnconfiguredPipelineHandler {
	public static <T extends PacketListener> UnconfiguredPipelineHandler.InboundConfigurationTask setupInboundProtocol(ProtocolInfo<T> protocolInfo) {
		return setupInboundHandler(new PacketDecoder<>(protocolInfo));
	}

	private static UnconfiguredPipelineHandler.InboundConfigurationTask setupInboundHandler(ChannelInboundHandler channelInboundHandler) {
		return channelHandlerContext -> {
			channelHandlerContext.pipeline().replace(channelHandlerContext.name(), "decoder", channelInboundHandler);
			channelHandlerContext.channel().config().setAutoRead(true);
		};
	}

	public static <T extends PacketListener> UnconfiguredPipelineHandler.OutboundConfigurationTask setupOutboundProtocol(ProtocolInfo<T> protocolInfo) {
		return setupOutboundHandler(new PacketEncoder<>(protocolInfo));
	}

	private static UnconfiguredPipelineHandler.OutboundConfigurationTask setupOutboundHandler(ChannelOutboundHandler channelOutboundHandler) {
		return channelHandlerContext -> channelHandlerContext.pipeline().replace(channelHandlerContext.name(), "encoder", channelOutboundHandler);
	}

	public static class Inbound extends ChannelDuplexHandler {
		@Override
		public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
			if (!(object instanceof ByteBuf) && !(object instanceof Packet)) {
				channelHandlerContext.fireChannelRead(object);
			} else {
				ReferenceCountUtil.release(object);
				throw new DecoderException("Pipeline has no inbound protocol configured, can't process packet " + object);
			}
		}

		@Override
		public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
			if (object instanceof UnconfiguredPipelineHandler.InboundConfigurationTask inboundConfigurationTask) {
				try {
					inboundConfigurationTask.run(channelHandlerContext);
				} finally {
					ReferenceCountUtil.release(object);
				}

				channelPromise.setSuccess();
			} else {
				channelHandlerContext.write(object, channelPromise);
			}
		}
	}

	@FunctionalInterface
	public interface InboundConfigurationTask {
		void run(ChannelHandlerContext channelHandlerContext);

		default UnconfiguredPipelineHandler.InboundConfigurationTask andThen(UnconfiguredPipelineHandler.InboundConfigurationTask inboundConfigurationTask) {
			return channelHandlerContext -> {
				this.run(channelHandlerContext);
				inboundConfigurationTask.run(channelHandlerContext);
			};
		}
	}

	public static class Outbound extends ChannelOutboundHandlerAdapter {
		@Override
		public void write(ChannelHandlerContext channelHandlerContext, Object object, ChannelPromise channelPromise) throws Exception {
			if (object instanceof Packet) {
				ReferenceCountUtil.release(object);
				throw new EncoderException("Pipeline has no outbound protocol configured, can't process packet " + object);
			} else {
				if (object instanceof UnconfiguredPipelineHandler.OutboundConfigurationTask outboundConfigurationTask) {
					try {
						outboundConfigurationTask.run(channelHandlerContext);
					} finally {
						ReferenceCountUtil.release(object);
					}

					channelPromise.setSuccess();
				} else {
					channelHandlerContext.write(object, channelPromise);
				}
			}
		}
	}

	@FunctionalInterface
	public interface OutboundConfigurationTask {
		void run(ChannelHandlerContext channelHandlerContext);

		default UnconfiguredPipelineHandler.OutboundConfigurationTask andThen(UnconfiguredPipelineHandler.OutboundConfigurationTask outboundConfigurationTask) {
			return channelHandlerContext -> {
				this.run(channelHandlerContext);
				outboundConfigurationTask.run(channelHandlerContext);
			};
		}
	}
}
