package net.minecraft.world.level.block.state.properties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.util.StringRepresentable;

public class EnumProperty<T extends Enum<T> & StringRepresentable> extends Property<T> {
	private static final int UNABLE_TO_USE_ORDINALS = -1;
	private final List<T> values;
	private final Map<String, T> names = Maps.<String, T>newHashMap();
	@VisibleForTesting
	protected int minOffset;
	@VisibleForTesting
	protected final int maxUsableOrdinal;

	protected EnumProperty(String string, Class<T> class_, List<T> list) {
		super(string, class_);
		if (list.isEmpty()) {
			throw new IllegalArgumentException("Trying to make empty EnumProperty '" + string + "'");
		} else {
			int[] is = new int[]{-1};
			if (IntStream.range(0, list.size()).allMatch(i -> {
				int j = ((Enum)list.get(i)).ordinal() - i;
				if (is[0] == -1) {
					is[0] = j;
				}

				return j == is[0];
			})) {
				this.values = Collections.unmodifiableList(list);
				this.maxUsableOrdinal = ((Enum)list.getLast()).ordinal();
				this.minOffset = is[0];
			} else {
				this.values = new ReferenceArrayList<>(list);
				this.maxUsableOrdinal = -1;
				this.minOffset = -1;
			}

			for (T enum_ : list) {
				String string2 = enum_.getSerializedName();
				if (this.names.containsKey(string2)) {
					throw new IllegalArgumentException("Multiple values have the same name '" + string2 + "'");
				}

				this.names.put(string2, enum_);
			}
		}
	}

	@Override
	public List<T> getPossibleValues() {
		return this.maxUsableOrdinal == -1 ? Collections.unmodifiableList(this.values) : this.values;
	}

	@Override
	public Optional<T> getValue(String string) {
		return Optional.ofNullable((Enum)this.names.get(string));
	}

	public String getName(T enum_) {
		return enum_.getSerializedName();
	}

	public int getInternalIndex(T enum_) {
		int i = enum_.ordinal();
		return i <= this.maxUsableOrdinal ? i - this.minOffset : this.values.indexOf(enum_);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof EnumProperty<?> enumProperty && super.equals(object)) {
				return this.values.equals(enumProperty.values) && this.names.equals(enumProperty.names);
			}

			return false;
		}
	}

	@Override
	public int generateHashCode() {
		int i = super.generateHashCode();
		i = 31 * i + this.values.hashCode();
		return 31 * i + this.names.hashCode();
	}

	public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String string, Class<T> class_) {
		return create(string, class_, (Predicate<T>)(enum_ -> true));
	}

	public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String string, Class<T> class_, Predicate<T> predicate) {
		return create(string, class_, (List<T>)Arrays.stream((Enum[])class_.getEnumConstants()).filter(predicate).collect(Collectors.toList()));
	}

	public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String string, Class<T> class_, T... enums) {
		return create(string, class_, Lists.<T>newArrayList(enums));
	}

	public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String string, Class<T> class_, List<T> list) {
		return new EnumProperty<>(string, class_, list);
	}
}
