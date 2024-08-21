package net.minecraft.world.level.redstone;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;

public class ExperimentalRedstoneUtils {
	@Nullable
	public static Orientation initialOrientation(Level level, @Nullable Direction direction, @Nullable Direction direction2) {
		if (level.enabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS)) {
			Orientation orientation = Orientation.random(level.random).withSideBias(Orientation.SideBias.LEFT);
			if (direction2 != null) {
				orientation = orientation.withUp(direction2);
			}

			if (direction != null) {
				orientation = orientation.withFront(direction);
			}

			return orientation;
		} else {
			return null;
		}
	}

	@Nullable
	public static Orientation withFront(@Nullable Orientation orientation, Direction direction) {
		return orientation == null ? null : orientation.withFront(direction);
	}
}
