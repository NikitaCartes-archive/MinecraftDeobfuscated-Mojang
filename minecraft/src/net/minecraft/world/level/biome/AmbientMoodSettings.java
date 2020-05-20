package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class AmbientMoodSettings {
	public static final Codec<AmbientMoodSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					SoundEvent.CODEC.fieldOf("sound").forGetter(ambientMoodSettings -> ambientMoodSettings.soundEvent),
					Codec.INT.fieldOf("tick_delay").forGetter(ambientMoodSettings -> ambientMoodSettings.tickDelay),
					Codec.INT.fieldOf("block_search_extent").forGetter(ambientMoodSettings -> ambientMoodSettings.blockSearchExtent),
					Codec.DOUBLE.fieldOf("offset").forGetter(ambientMoodSettings -> ambientMoodSettings.soundPositionOffset)
				)
				.apply(instance, AmbientMoodSettings::new)
	);
	public static final AmbientMoodSettings LEGACY_CAVE_SETTINGS = new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0);
	private SoundEvent soundEvent;
	private int tickDelay;
	private int blockSearchExtent;
	private double soundPositionOffset;

	public AmbientMoodSettings(SoundEvent soundEvent, int i, int j, double d) {
		this.soundEvent = soundEvent;
		this.tickDelay = i;
		this.blockSearchExtent = j;
		this.soundPositionOffset = d;
	}

	@Environment(EnvType.CLIENT)
	public SoundEvent getSoundEvent() {
		return this.soundEvent;
	}

	@Environment(EnvType.CLIENT)
	public int getTickDelay() {
		return this.tickDelay;
	}

	@Environment(EnvType.CLIENT)
	public int getBlockSearchExtent() {
		return this.blockSearchExtent;
	}

	@Environment(EnvType.CLIENT)
	public double getSoundPositionOffset() {
		return this.soundPositionOffset;
	}
}
