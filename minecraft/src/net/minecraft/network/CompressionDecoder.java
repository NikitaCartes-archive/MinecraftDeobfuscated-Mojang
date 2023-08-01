package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class CompressionDecoder extends ByteToMessageDecoder {
	public static final int MAXIMUM_COMPRESSED_LENGTH = 2097152;
	public static final int MAXIMUM_UNCOMPRESSED_LENGTH = 8388608;
	private final Inflater inflater;
	private int threshold;
	private boolean validateDecompressed;

	public CompressionDecoder(int i, boolean bl) {
		this.threshold = i;
		this.validateDecompressed = bl;
		this.inflater = new Inflater();
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		if (byteBuf.readableBytes() != 0) {
			int i = VarInt.read(byteBuf);
			if (i == 0) {
				list.add(byteBuf.readBytes(byteBuf.readableBytes()));
			} else {
				if (this.validateDecompressed) {
					if (i < this.threshold) {
						throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.threshold);
					}

					if (i > 8388608) {
						throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of 8388608");
					}
				}

				this.setupInflaterInput(byteBuf);
				ByteBuf byteBuf2 = this.inflate(channelHandlerContext, i);
				this.inflater.reset();
				list.add(byteBuf2);
			}
		}
	}

	private void setupInflaterInput(ByteBuf byteBuf) {
		ByteBuffer byteBuffer;
		if (byteBuf.nioBufferCount() > 0) {
			byteBuffer = byteBuf.nioBuffer();
			byteBuf.skipBytes(byteBuf.readableBytes());
		} else {
			byteBuffer = ByteBuffer.allocateDirect(byteBuf.readableBytes());
			byteBuf.readBytes(byteBuffer);
			byteBuffer.flip();
		}

		this.inflater.setInput(byteBuffer);
	}

	private ByteBuf inflate(ChannelHandlerContext channelHandlerContext, int i) throws DataFormatException {
		ByteBuf byteBuf = channelHandlerContext.alloc().directBuffer(i);

		try {
			ByteBuffer byteBuffer = byteBuf.internalNioBuffer(0, i);
			int j = byteBuffer.position();
			this.inflater.inflate(byteBuffer);
			int k = byteBuffer.position() - j;
			if (k != i) {
				throw new DecoderException("Badly compressed packet - actual length of uncompressed payload " + k + " is does not match declared size " + i);
			} else {
				byteBuf.writerIndex(byteBuf.writerIndex() + k);
				return byteBuf;
			}
		} catch (Exception var7) {
			byteBuf.release();
			throw var7;
		}
	}

	public void setThreshold(int i, boolean bl) {
		this.threshold = i;
		this.validateDecompressed = bl;
	}
}
