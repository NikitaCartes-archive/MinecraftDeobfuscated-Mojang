package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class UniformFloat {
	public static final Codec<UniformFloat> CODEC = Codec.either(
			Codec.FLOAT,
			RecordCodecBuilder.create(
					instance -> instance.group(
								Codec.FLOAT.fieldOf("base").forGetter(uniformFloat -> uniformFloat.baseValue),
								Codec.FLOAT.fieldOf("spread").forGetter(uniformFloat -> uniformFloat.spread)
							)
							.apply(instance, UniformFloat::new)
				)
				.comapFlatMap(
					uniformFloat -> uniformFloat.spread < 0.0F
							? DataResult.error("Spread must be non-negative, got: " + uniformFloat.spread)
							: DataResult.success(uniformFloat),
					Function.identity()
				)
		)
		.xmap(
			either -> either.map(UniformFloat::fixed, uniformFloat -> uniformFloat),
			uniformFloat -> uniformFloat.spread == 0.0F ? Either.left(uniformFloat.baseValue) : Either.right(uniformFloat)
		);
	private final float baseValue;
	private final float spread;

	public static Codec<UniformFloat> codec(float f, float g, float h) {
		Function<UniformFloat, DataResult<UniformFloat>> function = uniformFloat -> {
			if (!(uniformFloat.baseValue >= f) || !(uniformFloat.baseValue <= g)) {
				return DataResult.error("Base value out of range: " + uniformFloat.baseValue + " [" + f + "-" + g + "]");
			} else {
				return uniformFloat.spread <= h ? DataResult.success(uniformFloat) : DataResult.error("Spread too big: " + uniformFloat.spread + " > " + h);
			}
		};
		return CODEC.flatXmap(function, function);
	}

	private UniformFloat(float f, float g) {
		this.baseValue = f;
		this.spread = g;
	}

	public static UniformFloat fixed(float f) {
		return new UniformFloat(f, 0.0F);
	}

	public static UniformFloat of(float f, float g) {
		return new UniformFloat(f, g);
	}

	public float sample(Random random) {
		return this.spread == 0.0F ? this.baseValue : Mth.randomBetween(random, this.baseValue, this.baseValue + this.spread);
	}

	public float getBaseValue() {
		return this.baseValue;
	}

	public float getMaxValue() {
		return this.baseValue + this.spread;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			UniformFloat uniformFloat = (UniformFloat)object;
			return this.baseValue == uniformFloat.baseValue && this.spread == uniformFloat.spread;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.baseValue, this.spread});
	}

	public String toString() {
		return "[" + this.baseValue + '-' + (this.baseValue + this.spread) + ']';
	}
}
