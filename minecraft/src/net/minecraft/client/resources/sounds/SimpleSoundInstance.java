package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public class SimpleSoundInstance extends AbstractSoundInstance {
	public SimpleSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, BlockPos blockPos) {
		this(soundEvent, soundSource, f, g, (float)blockPos.getX() + 0.5F, (float)blockPos.getY() + 0.5F, (float)blockPos.getZ() + 0.5F);
	}

	public static SimpleSoundInstance forUI(SoundEvent soundEvent, float f) {
		return forUI(soundEvent, f, 0.25F);
	}

	public static SimpleSoundInstance forUI(SoundEvent soundEvent, float f, float g) {
		return new SimpleSoundInstance(soundEvent.getLocation(), SoundSource.MASTER, g, f, false, 0, SoundInstance.Attenuation.NONE, 0.0F, 0.0F, 0.0F, true);
	}

	public static SimpleSoundInstance forMusic(SoundEvent soundEvent) {
		return new SimpleSoundInstance(soundEvent.getLocation(), SoundSource.MUSIC, 1.0F, 1.0F, false, 0, SoundInstance.Attenuation.NONE, 0.0F, 0.0F, 0.0F, true);
	}

	public static SimpleSoundInstance forRecord(SoundEvent soundEvent, float f, float g, float h) {
		return new SimpleSoundInstance(soundEvent, SoundSource.RECORDS, 4.0F, 1.0F, false, 0, SoundInstance.Attenuation.LINEAR, f, g, h);
	}

	public SimpleSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, float h, float i, float j) {
		this(soundEvent, soundSource, f, g, false, 0, SoundInstance.Attenuation.LINEAR, h, i, j);
	}

	private SimpleSoundInstance(
		SoundEvent soundEvent, SoundSource soundSource, float f, float g, boolean bl, int i, SoundInstance.Attenuation attenuation, float h, float j, float k
	) {
		this(soundEvent.getLocation(), soundSource, f, g, bl, i, attenuation, h, j, k, false);
	}

	public SimpleSoundInstance(
		ResourceLocation resourceLocation,
		SoundSource soundSource,
		float f,
		float g,
		boolean bl,
		int i,
		SoundInstance.Attenuation attenuation,
		float h,
		float j,
		float k,
		boolean bl2
	) {
		super(resourceLocation, soundSource);
		this.volume = f;
		this.pitch = g;
		this.x = h;
		this.y = j;
		this.z = k;
		this.looping = bl;
		this.delay = i;
		this.attenuation = attenuation;
		this.relative = bl2;
	}
}
