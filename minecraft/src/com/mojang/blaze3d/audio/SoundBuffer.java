package com.mojang.blaze3d.audio;

import java.nio.ByteBuffer;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.openal.AL10;

@Environment(EnvType.CLIENT)
public class SoundBuffer {
	@Nullable
	private ByteBuffer data;
	private final AudioFormat format;
	private boolean hasAlBuffer;
	private int alBuffer;

	public SoundBuffer(ByteBuffer byteBuffer, AudioFormat audioFormat) {
		this.data = byteBuffer;
		this.format = audioFormat;
	}

	OptionalInt getAlBuffer() {
		if (!this.hasAlBuffer) {
			if (this.data == null) {
				return OptionalInt.empty();
			}

			int i = OpenAlUtil.audioFormatToOpenAl(this.format);
			int[] is = new int[1];
			AL10.alGenBuffers(is);
			if (OpenAlUtil.checkALError("Creating buffer")) {
				return OptionalInt.empty();
			}

			AL10.alBufferData(is[0], i, this.data, (int)this.format.getSampleRate());
			if (OpenAlUtil.checkALError("Assigning buffer data")) {
				return OptionalInt.empty();
			}

			this.alBuffer = is[0];
			this.hasAlBuffer = true;
			this.data = null;
		}

		return OptionalInt.of(this.alBuffer);
	}

	public void discardAlBuffer() {
		if (this.hasAlBuffer) {
			AL10.alDeleteBuffers(new int[]{this.alBuffer});
			if (OpenAlUtil.checkALError("Deleting stream buffers")) {
				return;
			}
		}

		this.hasAlBuffer = false;
	}

	public OptionalInt releaseAlBuffer() {
		OptionalInt optionalInt = this.getAlBuffer();
		this.hasAlBuffer = false;
		return optionalInt;
	}
}
