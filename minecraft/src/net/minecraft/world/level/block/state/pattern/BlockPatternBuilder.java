package net.minecraft.world.level.block.state.pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class BlockPatternBuilder {
	private static final Joiner COMMA_JOINED = Joiner.on(",");
	private final List<String[]> pattern = Lists.<String[]>newArrayList();
	private final Map<Character, Predicate<BlockInWorld>> lookup = Maps.<Character, Predicate<BlockInWorld>>newHashMap();
	private int height;
	private int width;

	private BlockPatternBuilder() {
		this.lookup.put(' ', (Predicate)blockInWorld -> true);
	}

	public BlockPatternBuilder aisle(String... strings) {
		if (!ArrayUtils.isEmpty((Object[])strings) && !StringUtils.isEmpty(strings[0])) {
			if (this.pattern.isEmpty()) {
				this.height = strings.length;
				this.width = strings[0].length();
			}

			if (strings.length != this.height) {
				throw new IllegalArgumentException("Expected aisle with height of " + this.height + ", but was given one with a height of " + strings.length + ")");
			} else {
				for (String string : strings) {
					if (string.length() != this.width) {
						throw new IllegalArgumentException(
							"Not all rows in the given aisle are the correct width (expected " + this.width + ", found one with " + string.length() + ")"
						);
					}

					for (char c : string.toCharArray()) {
						if (!this.lookup.containsKey(c)) {
							this.lookup.put(c, null);
						}
					}
				}

				this.pattern.add(strings);
				return this;
			}
		} else {
			throw new IllegalArgumentException("Empty pattern for aisle");
		}
	}

	public static BlockPatternBuilder start() {
		return new BlockPatternBuilder();
	}

	public BlockPatternBuilder where(char c, Predicate<BlockInWorld> predicate) {
		this.lookup.put(c, predicate);
		return this;
	}

	public BlockPattern build() {
		return new BlockPattern(this.createPattern());
	}

	private Predicate<BlockInWorld>[][][] createPattern() {
		this.ensureAllCharactersMatched();
		Predicate<BlockInWorld>[][][] predicates = (Predicate<BlockInWorld>[][][])Array.newInstance(
			Predicate.class, new int[]{this.pattern.size(), this.height, this.width}
		);

		for (int i = 0; i < this.pattern.size(); i++) {
			for (int j = 0; j < this.height; j++) {
				for (int k = 0; k < this.width; k++) {
					predicates[i][j][k] = (Predicate<BlockInWorld>)this.lookup.get(((String[])this.pattern.get(i))[j].charAt(k));
				}
			}
		}

		return predicates;
	}

	private void ensureAllCharactersMatched() {
		List<Character> list = Lists.<Character>newArrayList();

		for (Entry<Character, Predicate<BlockInWorld>> entry : this.lookup.entrySet()) {
			if (entry.getValue() == null) {
				list.add((Character)entry.getKey());
			}
		}

		if (!list.isEmpty()) {
			throw new IllegalStateException("Predicates for character(s) " + COMMA_JOINED.join(list) + " are missing");
		}
	}
}
