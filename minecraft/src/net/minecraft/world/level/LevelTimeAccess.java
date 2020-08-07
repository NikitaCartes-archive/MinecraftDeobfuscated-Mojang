package net.minecraft.world.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.dimension.DimensionType;

public interface LevelTimeAccess extends LevelReader {
	long dayTime();

	default float getMoonBrightness() {
		return DimensionType.MOON_BRIGHTNESS_PER_PHASE[this.dimensionType().moonPhase(this.dayTime())];
	}

	default float getTimeOfDay(float f) {
		return this.dimensionType().timeOfDay(this.dayTime());
	}

	@Environment(EnvType.CLIENT)
	default int getMoonPhase() {
		return this.dimensionType().moonPhase(this.dayTime());
	}
}
