package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class SimpleSoundInstance extends AbstractSoundInstance {
	public SimpleSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, RandomSource randomSource, BlockPos blockPos) {
		this(soundEvent, soundSource, f, g, randomSource, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
	}

	public static SimpleSoundInstance forUI(SoundEvent soundEvent, float f) {
		return forUI(soundEvent, f, 0.25F);
	}

	public static SimpleSoundInstance forUI(Holder<SoundEvent> holder, float f) {
		return forUI(holder.value(), f);
	}

	public static SimpleSoundInstance forUI(SoundEvent soundEvent, float f, float g) {
		return new SimpleSoundInstance(
			soundEvent, SoundSource.MASTER, g, f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true
		);
	}

	public static SimpleSoundInstance forMusic(SoundEvent soundEvent) {
		return new SimpleSoundInstance(
			soundEvent, SoundSource.MUSIC, 1.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true
		);
	}

	public static SimpleSoundInstance forRecord(SoundEvent soundEvent, Vec3 vec3) {
		return new SimpleSoundInstance(
			soundEvent, SoundSource.RECORDS, 4.0F, 1.0F, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR, vec3.x, vec3.y, vec3.z
		);
	}

	public static SimpleSoundInstance forLocalAmbience(SoundEvent soundEvent, float f, float g) {
		return new SimpleSoundInstance(
			soundEvent, SoundSource.AMBIENT, g, f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true
		);
	}

	public static SimpleSoundInstance forAmbientAddition(SoundEvent soundEvent) {
		return forLocalAmbience(soundEvent, 1.0F, 1.0F);
	}

	public static SimpleSoundInstance forAmbientMood(SoundEvent soundEvent, RandomSource randomSource, double d, double e, double f) {
		return new SimpleSoundInstance(soundEvent, SoundSource.AMBIENT, 1.0F, 1.0F, randomSource, false, 0, SoundInstance.Attenuation.LINEAR, d, e, f);
	}

	public SimpleSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float g, RandomSource randomSource, double d, double e, double h) {
		this(soundEvent, soundSource, f, g, randomSource, false, 0, SoundInstance.Attenuation.LINEAR, d, e, h, false);
	}

	private SimpleSoundInstance(
		SoundEvent soundEvent,
		SoundSource soundSource,
		float f,
		float g,
		RandomSource randomSource,
		boolean bl,
		int i,
		SoundInstance.Attenuation attenuation,
		double d,
		double e,
		double h
	) {
		this(soundEvent, soundSource, f, g, randomSource, bl, i, attenuation, d, e, h, false);
	}

	public SimpleSoundInstance(
		SoundEvent soundEvent,
		SoundSource soundSource,
		float f,
		float g,
		RandomSource randomSource,
		boolean bl,
		int i,
		SoundInstance.Attenuation attenuation,
		double d,
		double e,
		double h,
		boolean bl2
	) {
		super(soundEvent, soundSource, randomSource);
		this.volume = f;
		this.pitch = g;
		this.x = d;
		this.y = e;
		this.z = h;
		this.looping = bl;
		this.delay = i;
		this.attenuation = attenuation;
		this.relative = bl2;
	}
}
