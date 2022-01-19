package com.mojang.blaze3d.audio;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Channel {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int QUEUED_BUFFER_COUNT = 4;
	public static final int BUFFER_DURATION_SECONDS = 1;
	private final int source;
	private final AtomicBoolean initialized = new AtomicBoolean(true);
	private int streamingBufferSize = 16384;
	@Nullable
	private AudioStream stream;

	@Nullable
	static Channel create() {
		int[] is = new int[1];
		AL10.alGenSources(is);
		return OpenAlUtil.checkALError("Allocate new source") ? null : new Channel(is[0]);
	}

	private Channel(int i) {
		this.source = i;
	}

	public void destroy() {
		if (this.initialized.compareAndSet(true, false)) {
			AL10.alSourceStop(this.source);
			OpenAlUtil.checkALError("Stop");
			if (this.stream != null) {
				try {
					this.stream.close();
				} catch (IOException var2) {
					LOGGER.error("Failed to close audio stream", (Throwable)var2);
				}

				this.removeProcessedBuffers();
				this.stream = null;
			}

			AL10.alDeleteSources(new int[]{this.source});
			OpenAlUtil.checkALError("Cleanup");
		}
	}

	public void play() {
		AL10.alSourcePlay(this.source);
	}

	private int getState() {
		return !this.initialized.get() ? 4116 : AL10.alGetSourcei(this.source, 4112);
	}

	public void pause() {
		if (this.getState() == 4114) {
			AL10.alSourcePause(this.source);
		}
	}

	public void unpause() {
		if (this.getState() == 4115) {
			AL10.alSourcePlay(this.source);
		}
	}

	public void stop() {
		if (this.initialized.get()) {
			AL10.alSourceStop(this.source);
			OpenAlUtil.checkALError("Stop");
		}
	}

	public boolean playing() {
		return this.getState() == 4114;
	}

	public boolean stopped() {
		return this.getState() == 4116;
	}

	public void setSelfPosition(Vec3 vec3) {
		AL10.alSourcefv(this.source, 4100, new float[]{(float)vec3.x, (float)vec3.y, (float)vec3.z});
	}

	public void setPitch(float f) {
		AL10.alSourcef(this.source, 4099, f);
	}

	public void setLooping(boolean bl) {
		AL10.alSourcei(this.source, 4103, bl ? 1 : 0);
	}

	public void setVolume(float f) {
		AL10.alSourcef(this.source, 4106, f);
	}

	public void disableAttenuation() {
		AL10.alSourcei(this.source, 53248, 0);
	}

	public void linearAttenuation(float f) {
		AL10.alSourcei(this.source, 53248, 53251);
		AL10.alSourcef(this.source, 4131, f);
		AL10.alSourcef(this.source, 4129, 1.0F);
		AL10.alSourcef(this.source, 4128, 0.0F);
	}

	public void setRelative(boolean bl) {
		AL10.alSourcei(this.source, 514, bl ? 1 : 0);
	}

	public void attachStaticBuffer(SoundBuffer soundBuffer) {
		soundBuffer.getAlBuffer().ifPresent(i -> AL10.alSourcei(this.source, 4105, i));
	}

	public void attachBufferStream(AudioStream audioStream) {
		this.stream = audioStream;
		AudioFormat audioFormat = audioStream.getFormat();
		this.streamingBufferSize = calculateBufferSize(audioFormat, 1);
		this.pumpBuffers(4);
	}

	private static int calculateBufferSize(AudioFormat audioFormat, int i) {
		return (int)((float)(i * audioFormat.getSampleSizeInBits()) / 8.0F * (float)audioFormat.getChannels() * audioFormat.getSampleRate());
	}

	private void pumpBuffers(int i) {
		if (this.stream != null) {
			try {
				for (int j = 0; j < i; j++) {
					ByteBuffer byteBuffer = this.stream.read(this.streamingBufferSize);
					if (byteBuffer != null) {
						new SoundBuffer(byteBuffer, this.stream.getFormat()).releaseAlBuffer().ifPresent(ix -> AL10.alSourceQueueBuffers(this.source, new int[]{ix}));
					}
				}
			} catch (IOException var4) {
				LOGGER.error("Failed to read from audio stream", (Throwable)var4);
			}
		}
	}

	public void updateStream() {
		if (this.stream != null) {
			int i = this.removeProcessedBuffers();
			this.pumpBuffers(i);
		}
	}

	private int removeProcessedBuffers() {
		int i = AL10.alGetSourcei(this.source, 4118);
		if (i > 0) {
			int[] is = new int[i];
			AL10.alSourceUnqueueBuffers(this.source, is);
			OpenAlUtil.checkALError("Unqueue buffers");
			AL10.alDeleteBuffers(is);
			OpenAlUtil.checkALError("Remove processed buffers");
		}

		return i;
	}
}
