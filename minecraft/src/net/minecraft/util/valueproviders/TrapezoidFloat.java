package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Function;

public class TrapezoidFloat extends FloatProvider {
	public static final Codec<TrapezoidFloat> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.FLOAT.fieldOf("min").forGetter(trapezoidFloat -> trapezoidFloat.min),
						Codec.FLOAT.fieldOf("max").forGetter(trapezoidFloat -> trapezoidFloat.max),
						Codec.FLOAT.fieldOf("plateau").forGetter(trapezoidFloat -> trapezoidFloat.plateau)
					)
					.apply(instance, TrapezoidFloat::new)
		)
		.comapFlatMap(
			trapezoidFloat -> {
				if (trapezoidFloat.max < trapezoidFloat.min) {
					return DataResult.error("Max must be larger than min: [" + trapezoidFloat.min + ", " + trapezoidFloat.max + "]");
				} else {
					return trapezoidFloat.plateau > trapezoidFloat.max - trapezoidFloat.min
						? DataResult.error("Plateau can at most be the full span: [" + trapezoidFloat.min + ", " + trapezoidFloat.max + "]")
						: DataResult.success(trapezoidFloat);
				}
			},
			Function.identity()
		);
	private final float min;
	private final float max;
	private final float plateau;

	public static TrapezoidFloat of(float f, float g, float h) {
		return new TrapezoidFloat(f, g, h);
	}

	private TrapezoidFloat(float f, float g, float h) {
		this.min = f;
		this.max = g;
		this.plateau = h;
	}

	@Override
	public float sample(Random random) {
		float f = this.max - this.min;
		float g = (f - this.plateau) / 2.0F;
		float h = f - g;
		return this.min + random.nextFloat() * h + random.nextFloat() * g;
	}

	@Override
	public float getMinValue() {
		return this.min;
	}

	@Override
	public float getMaxValue() {
		return this.max;
	}

	@Override
	public FloatProviderType<?> getType() {
		return FloatProviderType.TRAPEZOID;
	}

	public String toString() {
		return "trapezoid(" + this.plateau + ") in [" + this.min + "-" + this.max + "]";
	}
}
