package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;

public class AmbientAdditionsSettings {
	public static final Codec<AmbientAdditionsSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					SoundEvent.CODEC.fieldOf("sound").forGetter(ambientAdditionsSettings -> ambientAdditionsSettings.soundEvent),
					Codec.DOUBLE.fieldOf("tick_chance").forGetter(ambientAdditionsSettings -> ambientAdditionsSettings.tickChance)
				)
				.apply(instance, AmbientAdditionsSettings::new)
	);
	private SoundEvent soundEvent;
	private double tickChance;

	public AmbientAdditionsSettings(SoundEvent soundEvent, double d) {
		this.soundEvent = soundEvent;
		this.tickChance = d;
	}

	@Environment(EnvType.CLIENT)
	public SoundEvent getSoundEvent() {
		return this.soundEvent;
	}

	@Environment(EnvType.CLIENT)
	public double getTickChance() {
		return this.tickChance;
	}
}
