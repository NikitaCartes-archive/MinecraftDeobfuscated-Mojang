package net.minecraft.world.level.block.state.properties;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class RotationSegment {
	private static final int MAX_SEGMENT_INDEX = 15;
	private static final int NORTH_0 = 0;
	private static final int EAST_90 = 4;
	private static final int SOUTH_180 = 8;
	private static final int WEST_270 = 12;

	public static int getMaxSegmentIndex() {
		return 15;
	}

	public static int convertToSegment(Direction direction) {
		return direction.getAxis().isVertical() ? 0 : direction.getOpposite().get2DDataValue() * 4;
	}

	public static int convertToSegment(float f) {
		return Mth.floor((double)((180.0F + f) * 16.0F / 360.0F) + 0.5) & 15;
	}

	public static Optional<Direction> convertToDirection(int i) {
		Direction direction = switch (i) {
			case 0 -> Direction.NORTH;
			case 4 -> Direction.EAST;
			case 8 -> Direction.SOUTH;
			case 12 -> Direction.WEST;
			default -> null;
		};
		return Optional.ofNullable(direction);
	}

	public static float convertToDegrees(int i) {
		return (float)i * 22.5F;
	}
}
