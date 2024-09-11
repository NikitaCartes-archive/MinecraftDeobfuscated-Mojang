package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;

public class BooleanProperty extends Property<Boolean> {
	private final ImmutableList<Boolean> values = ImmutableList.of(true, false);

	protected BooleanProperty(String string) {
		super(string, Boolean.class);
	}

	@Override
	public List<Boolean> getPossibleValues() {
		return this.values;
	}

	public static BooleanProperty create(String string) {
		return new BooleanProperty(string);
	}

	@Override
	public Optional<Boolean> getValue(String string) {
		return !"true".equals(string) && !"false".equals(string) ? Optional.empty() : Optional.of(Boolean.valueOf(string));
	}

	public String getName(Boolean boolean_) {
		return boolean_.toString();
	}

	public int getInternalIndex(Boolean boolean_) {
		return boolean_ ? 0 : 1;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof BooleanProperty booleanProperty && super.equals(object)) {
				return this.values.equals(booleanProperty.values);
			}

			return false;
		}
	}

	@Override
	public int generateHashCode() {
		return 31 * super.generateHashCode() + this.values.hashCode();
	}
}
