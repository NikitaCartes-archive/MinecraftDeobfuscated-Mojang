package net.minecraft.client.sounds;

import java.io.IOException;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface FiniteAudioStream extends AudioStream {
	ByteBuffer readAll() throws IOException;
}
