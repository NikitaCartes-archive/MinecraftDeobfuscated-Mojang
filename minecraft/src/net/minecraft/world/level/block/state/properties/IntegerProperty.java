package net.minecraft.world.level.block.state.properties;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public final class IntegerProperty extends Property<Integer> {
	private final IntImmutableList values;
	private final int min;
	private final int max;

	private IntegerProperty(String string, int i, int j) {
		super(string, Integer.class);
		if (i < 0) {
			throw new IllegalArgumentException("Min value of " + string + " must be 0 or greater");
		} else if (j <= i) {
			throw new IllegalArgumentException("Max value of " + string + " must be greater than min (" + i + ")");
		} else {
			this.min = i;
			this.max = j;
			this.values = IntImmutableList.toList(IntStream.range(i, j + 1));
		}
	}

	@Override
	public List<Integer> getPossibleValues() {
		return this.values;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof IntegerProperty integerProperty && super.equals(object)) {
				return this.values.equals(integerProperty.values);
			}

			return false;
		}
	}

	@Override
	public int generateHashCode() {
		return 31 * super.generateHashCode() + this.values.hashCode();
	}

	public static IntegerProperty create(String string, int i, int j) {
		return new IntegerProperty(string, i, j);
	}

	@Override
	public Optional<Integer> getValue(String string) {
		try {
			int i = Integer.parseInt(string);
			return i >= this.min && i <= this.max ? Optional.of(i) : Optional.empty();
		} catch (NumberFormatException var3) {
			return Optional.empty();
		}
	}

	public String getName(Integer integer) {
		return integer.toString();
	}

	public int getInternalIndex(Integer integer) {
		return integer <= this.max ? integer - this.min : -1;
	}
}
