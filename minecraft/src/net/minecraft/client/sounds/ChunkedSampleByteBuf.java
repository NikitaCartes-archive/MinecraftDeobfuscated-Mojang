package net.minecraft.client.sounds;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.nio.ByteBuffer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.lwjgl.BufferUtils;

@Environment(EnvType.CLIENT)
public class ChunkedSampleByteBuf implements FloatConsumer {
	private final List<ByteBuffer> buffers = Lists.<ByteBuffer>newArrayList();
	private final int bufferSize;
	private int byteCount;
	private ByteBuffer currentBuffer;

	public ChunkedSampleByteBuf(int i) {
		this.bufferSize = i + 1 & -2;
		this.currentBuffer = BufferUtils.createByteBuffer(i);
	}

	@Override
	public void accept(float f) {
		if (this.currentBuffer.remaining() == 0) {
			this.currentBuffer.flip();
			this.buffers.add(this.currentBuffer);
			this.currentBuffer = BufferUtils.createByteBuffer(this.bufferSize);
		}

		int i = Mth.clamp((int)(f * 32767.5F - 0.5F), -32768, 32767);
		this.currentBuffer.putShort((short)i);
		this.byteCount += 2;
	}

	public ByteBuffer get() {
		this.currentBuffer.flip();
		if (this.buffers.isEmpty()) {
			return this.currentBuffer;
		} else {
			ByteBuffer byteBuffer = BufferUtils.createByteBuffer(this.byteCount);
			this.buffers.forEach(byteBuffer::put);
			byteBuffer.put(this.currentBuffer);
			byteBuffer.flip();
			return byteBuffer;
		}
	}

	public int size() {
		return this.byteCount;
	}
}
