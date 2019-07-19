package net.minecraft.world.entity.ai.attributes;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;

public class RangedAttribute extends BaseAttribute {
	private final double minValue;
	private final double maxValue;
	private String importLegacyName;

	public RangedAttribute(@Nullable Attribute attribute, String string, double d, double e, double f) {
		super(attribute, string, d);
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

	public RangedAttribute importLegacyName(String string) {
		this.importLegacyName = string;
		return this;
	}

	public String getImportLegacyName() {
		return this.importLegacyName;
	}

	@Override
	public double sanitizeValue(double d) {
		return Mth.clamp(d, this.minValue, this.maxValue);
	}
}
