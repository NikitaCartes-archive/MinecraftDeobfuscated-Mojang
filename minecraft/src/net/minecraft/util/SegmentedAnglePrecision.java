package net.minecraft.util;

import net.minecraft.core.Direction;

public class SegmentedAnglePrecision {
	private final int mask;
	private final int precision;
	private final float degreeToAngle;
	private final float angleToDegree;

	public SegmentedAnglePrecision(int i) {
		if (i < 2) {
			throw new IllegalArgumentException("Precision cannot be less than 2 bits");
		} else if (i > 30) {
			throw new IllegalArgumentException("Precision cannot be greater than 30 bits");
		} else {
			int j = 1 << i;
			this.mask = j - 1;
			this.precision = i;
			this.degreeToAngle = (float)j / 360.0F;
			this.angleToDegree = 360.0F / (float)j;
		}
	}

	public boolean isSameAxis(int i, int j) {
		int k = this.getMask() >> 1;
		return (i & k) == (j & k);
	}

	public int fromDirection(Direction direction) {
		if (direction.getAxis().isVertical()) {
			return 0;
		} else {
			int i = direction.get2DDataValue();
			return i << this.precision - 2;
		}
	}

	public int fromDegreesWithTurns(float f) {
		return Math.round(f * this.degreeToAngle);
	}

	public int fromDegrees(float f) {
		return this.normalize(this.fromDegreesWithTurns(f));
	}

	public float toDegreesWithTurns(int i) {
		return (float)i * this.angleToDegree;
	}

	public float toDegrees(int i) {
		float f = this.toDegreesWithTurns(this.normalize(i));
		return f >= 180.0F ? f - 360.0F : f;
	}

	public int normalize(int i) {
		return i & this.mask;
	}

	public int getMask() {
		return this.mask;
	}
}
