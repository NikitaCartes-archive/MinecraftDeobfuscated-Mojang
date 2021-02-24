package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class UniformFloat extends FloatProvider {
	public static final Codec<UniformFloat> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.FLOAT.fieldOf("base").forGetter(uniformFloat -> uniformFloat.baseValue),
						Codec.FLOAT.fieldOf("spread").forGetter(uniformFloat -> uniformFloat.spread)
					)
					.apply(instance, UniformFloat::new)
		)
		.comapFlatMap(
			uniformFloat -> uniformFloat.spread < 0.0F ? DataResult.error("Spread must be non-negative, got: " + uniformFloat.spread) : DataResult.success(uniformFloat),
			Function.identity()
		);
	private final float baseValue;
	private final float spread;

	private UniformFloat(float f, float g) {
		this.baseValue = f;
		this.spread = g;
	}

	public static UniformFloat of(float f, float g) {
		return new UniformFloat(f, g);
	}

	@Override
	public float sample(Random random) {
		return this.spread == 0.0F ? this.baseValue : Mth.randomBetween(random, this.baseValue, this.baseValue + this.spread);
	}

	@Override
	public float getMinValue() {
		return this.baseValue;
	}

	@Override
	public float getMaxValue() {
		return this.baseValue + this.spread;
	}

	@Override
	public FloatProviderType<?> getType() {
		return FloatProviderType.UNIFORM;
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
