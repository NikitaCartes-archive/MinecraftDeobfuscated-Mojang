package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;

public final class EnumProperty<T extends Enum<T> & StringRepresentable> extends Property<T> {
	private final List<T> values;
	private final Map<String, T> names;
	private final int[] ordinalToIndex;

	private EnumProperty(String string, Class<T> class_, List<T> list) {
		super(string, class_);
		if (list.isEmpty()) {
			throw new IllegalArgumentException("Trying to make empty EnumProperty '" + string + "'");
		} else {
			this.values = List.copyOf(list);
			T[] enums = (T[])class_.getEnumConstants();
			this.ordinalToIndex = new int[enums.length];

			for (T enum_ : enums) {
				this.ordinalToIndex[enum_.ordinal()] = list.indexOf(enum_);
			}

			Builder<String, T> builder = ImmutableMap.builder();

			for (T enum2 : list) {
				String string2 = enum2.getSerializedName();
				builder.put(string2, enum2);
			}

			this.names = builder.buildOrThrow();
		}
	}

	@Override
	public List<T> getPossibleValues() {
		return this.values;
	}

	@Override
	public Optional<T> getValue(String string) {
		return Optional.ofNullable((Enum)this.names.get(string));
	}

	public String getName(T enum_) {
		return enum_.getSerializedName();
	}

	public int getInternalIndex(T enum_) {
		return this.ordinalToIndex[enum_.ordinal()];
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof EnumProperty<?> enumProperty && super.equals(object)) {
				return this.values.equals(enumProperty.values);
			}

			return false;
		}
	}

	@Override
	public int generateHashCode() {
		int i = super.generateHashCode();
		return 31 * i + this.values.hashCode();
	}

	public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String string, Class<T> class_) {
		return create(string, class_, (Predicate<T>)(enum_ -> true));
	}

	public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String string, Class<T> class_, Predicate<T> predicate) {
		return create(string, class_, (List<T>)Arrays.stream((Enum[])class_.getEnumConstants()).filter(predicate).collect(Collectors.toList()));
	}

	@SafeVarargs
	public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String string, Class<T> class_, T... enums) {
		return create(string, class_, List.of(enums));
	}

	public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String string, Class<T> class_, List<T> list) {
		return new EnumProperty<>(string, class_, list);
	}
}
