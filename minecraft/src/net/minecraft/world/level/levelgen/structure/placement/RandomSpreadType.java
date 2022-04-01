package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.RandomSource;

public enum RandomSpreadType implements StringRepresentable {
	LINEAR("linear"),
	TRIANGULAR("triangular");

	private static final RandomSpreadType[] VALUES = values();
	public static final Codec<RandomSpreadType> CODEC = StringRepresentable.fromEnum(() -> VALUES, RandomSpreadType::byName);
	private final String id;

	private RandomSpreadType(String string2) {
		this.id = string2;
	}

	public static RandomSpreadType byName(String string) {
		for (RandomSpreadType randomSpreadType : VALUES) {
			if (randomSpreadType.getSerializedName().equals(string)) {
				return randomSpreadType;
			}
		}

		throw new IllegalArgumentException("Unknown Random Spread type: " + string);
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}

	public int evaluate(RandomSource randomSource, int i) {
		return switch (this) {
			case LINEAR -> randomSource.nextInt(i);
			case TRIANGULAR -> (randomSource.nextInt(i) + randomSource.nextInt(i)) / 2;
		};
	}
}
