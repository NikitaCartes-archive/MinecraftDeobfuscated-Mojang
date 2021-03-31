package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;

public class ConstantInt extends IntProvider {
	public static final ConstantInt ZERO = new ConstantInt(0);
	public static final Codec<ConstantInt> CODEC = Codec.either(
			Codec.INT,
			RecordCodecBuilder.create(
				instance -> instance.group(Codec.INT.fieldOf("value").forGetter(constantInt -> constantInt.value)).apply(instance, ConstantInt::new)
			)
		)
		.xmap(either -> either.map(ConstantInt::of, constantInt -> constantInt), constantInt -> Either.left(constantInt.value));
	private final int value;

	public static ConstantInt of(int i) {
		return i == 0 ? ZERO : new ConstantInt(i);
	}

	private ConstantInt(int i) {
		this.value = i;
	}

	public int getValue() {
		return this.value;
	}

	@Override
	public int sample(Random random) {
		return this.value;
	}

	@Override
	public int getMinValue() {
		return this.value;
	}

	@Override
	public int getMaxValue() {
		return this.value;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			ConstantInt constantInt = (ConstantInt)object;
			return this.value == constantInt.value;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Integer.hashCode(this.value);
	}

	@Override
	public IntProviderType<?> getType() {
		return IntProviderType.CONSTANT;
	}

	public String toString() {
		return Integer.toString(this.value);
	}
}
