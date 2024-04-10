package net.minecraft.client.sounds;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.io.IOException;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface FloatSampleSource extends FiniteAudioStream {
	int EXPECTED_MAX_FRAME_SIZE = 8192;

	boolean readChunk(FloatConsumer floatConsumer) throws IOException;

	@Override
	default ByteBuffer read(int i) throws IOException {
		ChunkedSampleByteBuf chunkedSampleByteBuf = new ChunkedSampleByteBuf(i + 8192);

		while (this.readChunk(chunkedSampleByteBuf) && chunkedSampleByteBuf.size() < i) {
		}

		return chunkedSampleByteBuf.get();
	}

	@Override
	default ByteBuffer readAll() throws IOException {
		ChunkedSampleByteBuf chunkedSampleByteBuf = new ChunkedSampleByteBuf(16384);

		while (this.readChunk(chunkedSampleByteBuf)) {
		}

		return chunkedSampleByteBuf.get();
	}
}
