package net.minecraft.world.entity.ai.attributes;

import net.minecraft.util.Mth;

public class RangedAttribute extends Attribute {
	private final double minValue;
	private final double maxValue;

	public RangedAttribute(String string, double d, double e, double f) {
		super(string, d);
		this.minValue = e;
		this.maxValue = f;
		if (e > f) {
			throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
		} else if (d < e) {
			throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
		} else if (d > f) {
			throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
		}
	}

	public double getMinValue() {
		return this.minValue;
	}

	public double getMaxValue() {
		return this.maxValue;
	}

	@Override
	public double sanitizeValue(double d) {
		return Double.isNaN(d) ? this.minValue : Mth.clamp(d, this.minValue, this.maxValue);
	}
}
