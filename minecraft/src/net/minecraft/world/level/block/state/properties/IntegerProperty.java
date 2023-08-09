package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class IntegerProperty extends Property<Integer> {
	private final ImmutableSet<Integer> values;
	private final int min;
	private final int max;

	protected IntegerProperty(String string, int i, int j) {
		super(string, Integer.class);
		if (i < 0) {
			throw new IllegalArgumentException("Min value of " + string + " must be 0 or greater");
		} else if (j <= i) {
			throw new IllegalArgumentException("Max value of " + string + " must be greater than min (" + i + ")");
		} else {
			this.min = i;
			this.max = j;
			Set<Integer> set = Sets.<Integer>newHashSet();

			for (int k = i; k <= j; k++) {
				set.add(k);
			}

			this.values = ImmutableSet.copyOf(set);
		}
	}

	@Override
	public Collection<Integer> getPossibleValues() {
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
			Integer integer = Integer.valueOf(string);
			return integer >= this.min && integer <= this.max ? Optional.of(integer) : Optional.empty();
		} catch (NumberFormatException var3) {
			return Optional.empty();
		}
	}

	public String getName(Integer integer) {
		return integer.toString();
	}
}
