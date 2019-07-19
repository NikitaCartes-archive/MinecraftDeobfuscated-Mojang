package net.minecraft.client.sounds;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface AudioStream extends Closeable {
	AudioFormat getFormat();

	ByteBuffer readAll() throws IOException;

	@Nullable
	ByteBuffer read(int i) throws IOException;
}
