package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;

public class ConstantInt extends IntProvider {
	public static final ConstantInt ZERO = new ConstantInt(0);
	public static final MapCodec<ConstantInt> CODEC = Codec.INT.fieldOf("value").xmap(ConstantInt::of, ConstantInt::getValue);
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
	public int sample(RandomSource randomSource) {
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

	@Override
	public IntProviderType<?> getType() {
		return IntProviderType.CONSTANT;
	}

	public String toString() {
		return Integer.toString(this.value);
	}
}
