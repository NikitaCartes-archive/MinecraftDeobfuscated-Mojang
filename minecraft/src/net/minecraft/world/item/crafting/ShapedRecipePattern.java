package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;

public record ShapedRecipePattern(int width, int height, NonNullList<Ingredient> ingredients, Optional<ShapedRecipePattern.Data> data) {
	private static final int MAX_SIZE = 3;
	public static final MapCodec<ShapedRecipePattern> MAP_CODEC = ShapedRecipePattern.Data.MAP_CODEC
		.flatXmap(
			ShapedRecipePattern::unpack,
			shapedRecipePattern -> (DataResult)shapedRecipePattern.data()
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe"))
		);

	public static ShapedRecipePattern of(Map<Character, Ingredient> map, String... strings) {
		return of(map, List.of(strings));
	}

	public static ShapedRecipePattern of(Map<Character, Ingredient> map, List<String> list) {
		ShapedRecipePattern.Data data = new ShapedRecipePattern.Data(map, list);
		return Util.getOrThrow(unpack(data), IllegalArgumentException::new);
	}

	private static DataResult<ShapedRecipePattern> unpack(ShapedRecipePattern.Data data) {
		String[] strings = shrink(data.pattern);
		int i = strings[0].length();
		int j = strings.length;
		NonNullList<Ingredient> nonNullList = NonNullList.withSize(i * j, Ingredient.EMPTY);
		CharSet charSet = new CharArraySet(data.key.keySet());

		for (int k = 0; k < strings.length; k++) {
			String string = strings[k];

			for (int l = 0; l < string.length(); l++) {
				char c = string.charAt(l);
				Ingredient ingredient = c == ' ' ? Ingredient.EMPTY : (Ingredient)data.key.get(c);
				if (ingredient == null) {
					return DataResult.error(() -> "Pattern references symbol '" + c + "' but it's not defined in the key");
				}

				charSet.remove(c);
				nonNullList.set(l + i * k, ingredient);
			}
		}

		return !charSet.isEmpty()
			? DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + charSet)
			: DataResult.success(new ShapedRecipePattern(i, j, nonNullList, Optional.of(data)));
	}

	@VisibleForTesting
	static String[] shrink(List<String> list) {
		int i = Integer.MAX_VALUE;
		int j = 0;
		int k = 0;
		int l = 0;

		for (int m = 0; m < list.size(); m++) {
			String string = (String)list.get(m);
			i = Math.min(i, firstNonSpace(string));
			int n = lastNonSpace(string);
			j = Math.max(j, n);
			if (n < 0) {
				if (k == m) {
					k++;
				}

				l++;
			} else {
				l = 0;
			}
		}

		if (list.size() == l) {
			return new String[0];
		} else {
			String[] strings = new String[list.size() - l - k];

			for (int o = 0; o < strings.length; o++) {
				strings[o] = ((String)list.get(o + k)).substring(i, j + 1);
			}

			return strings;
		}
	}

	private static int firstNonSpace(String string) {
		int i = 0;

		while (i < string.length() && string.charAt(i) == ' ') {
			i++;
		}

		return i;
	}

	private static int lastNonSpace(String string) {
		int i = string.length() - 1;

		while (i >= 0 && string.charAt(i) == ' ') {
			i--;
		}

		return i;
	}

	public boolean matches(CraftingContainer craftingContainer) {
		for (int i = 0; i <= craftingContainer.getWidth() - this.width; i++) {
			for (int j = 0; j <= craftingContainer.getHeight() - this.height; j++) {
				if (this.matches(craftingContainer, i, j, true)) {
					return true;
				}

				if (this.matches(craftingContainer, i, j, false)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean matches(CraftingContainer craftingContainer, int i, int j, boolean bl) {
		for (int k = 0; k < craftingContainer.getWidth(); k++) {
			for (int l = 0; l < craftingContainer.getHeight(); l++) {
				int m = k - i;
				int n = l - j;
				Ingredient ingredient = Ingredient.EMPTY;
				if (m >= 0 && n >= 0 && m < this.width && n < this.height) {
					if (bl) {
						ingredient = this.ingredients.get(this.width - m - 1 + n * this.width);
					} else {
						ingredient = this.ingredients.get(m + n * this.width);
					}
				}

				if (!ingredient.test(craftingContainer.getItem(k + l * craftingContainer.getWidth()))) {
					return false;
				}
			}
		}

		return true;
	}

	public void toNetwork(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.width);
		friendlyByteBuf.writeVarInt(this.height);

		for (Ingredient ingredient : this.ingredients) {
			ingredient.toNetwork(friendlyByteBuf);
		}
	}

	public static ShapedRecipePattern fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		int i = friendlyByteBuf.readVarInt();
		int j = friendlyByteBuf.readVarInt();
		NonNullList<Ingredient> nonNullList = NonNullList.withSize(i * j, Ingredient.EMPTY);
		nonNullList.replaceAll(ingredient -> Ingredient.fromNetwork(friendlyByteBuf));
		return new ShapedRecipePattern(i, j, nonNullList, Optional.empty());
	}

	public static record Data(Map<Character, Ingredient> key, List<String> pattern) {
		private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap(list -> {
			if (list.size() > 3) {
				return DataResult.error(() -> "Invalid pattern: too many rows, 3 is maximum");
			} else if (list.isEmpty()) {
				return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
			} else {
				int i = ((String)list.get(0)).length();

				for (String string : list) {
					if (string.length() > 3) {
						return DataResult.error(() -> "Invalid pattern: too many columns, 3 is maximum");
					}

					if (i != string.length()) {
						return DataResult.error(() -> "Invalid pattern: each row must be the same width");
					}
				}

				return DataResult.success(list);
			}
		}, Function.identity());
		private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(string -> {
			if (string.length() != 1) {
				return DataResult.error(() -> "Invalid key entry: '" + string + "' is an invalid symbol (must be 1 character only).");
			} else {
				return " ".equals(string) ? DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.") : DataResult.success(string.charAt(0));
			}
		}, String::valueOf);
		public static final MapCodec<ShapedRecipePattern.Data> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, Ingredient.CODEC_NONEMPTY).fieldOf("key").forGetter(data -> data.key),
						PATTERN_CODEC.fieldOf("pattern").forGetter(data -> data.pattern)
					)
					.apply(instance, ShapedRecipePattern.Data::new)
		);
	}
}
