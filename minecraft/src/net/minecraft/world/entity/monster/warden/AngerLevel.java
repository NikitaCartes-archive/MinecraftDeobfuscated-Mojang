package net.minecraft.world.entity.monster.warden;

import java.util.Arrays;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum AngerLevel {
	CALM(0, SoundEvents.WARDEN_AMBIENT),
	AGITATED(40, SoundEvents.WARDEN_AGITATED),
	ANGRY(80, SoundEvents.WARDEN_ANGRY);

	private static final AngerLevel[] SORTED_LEVELS = Util.make(
		values(), angerLevels -> Arrays.sort(angerLevels, (angerLevel, angerLevel2) -> Integer.compare(angerLevel2.minimumAnger, angerLevel.minimumAnger))
	);
	private final int minimumAnger;
	private final SoundEvent ambientSound;

	private AngerLevel(int j, SoundEvent soundEvent) {
		this.minimumAnger = j;
		this.ambientSound = soundEvent;
	}

	public int getMinimumAnger() {
		return this.minimumAnger;
	}

	public SoundEvent getAmbientSound() {
		return this.ambientSound;
	}

	public static AngerLevel byAnger(int i) {
		for (AngerLevel angerLevel : SORTED_LEVELS) {
			if (i >= angerLevel.minimumAnger) {
				return angerLevel;
			}
		}

		return CALM;
	}
}
