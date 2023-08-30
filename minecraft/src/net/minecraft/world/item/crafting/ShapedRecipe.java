package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.NotImplementedException;

public class ShapedRecipe implements CraftingRecipe {
	final int width;
	final int height;
	final NonNullList<Ingredient> recipeItems;
	final ItemStack result;
	final String group;
	final CraftingBookCategory category;
	final boolean showNotification;

	public ShapedRecipe(
		String string, CraftingBookCategory craftingBookCategory, int i, int j, NonNullList<Ingredient> nonNullList, ItemStack itemStack, boolean bl
	) {
		this.group = string;
		this.category = craftingBookCategory;
		this.width = i;
		this.height = j;
		this.recipeItems = nonNullList;
		this.result = itemStack;
		this.showNotification = bl;
	}

	public ShapedRecipe(String string, CraftingBookCategory craftingBookCategory, int i, int j, NonNullList<Ingredient> nonNullList, ItemStack itemStack) {
		this(string, craftingBookCategory, i, j, nonNullList, itemStack, true);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SHAPED_RECIPE;
	}

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
	public CraftingBookCategory category() {
		return this.category;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return this.result;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return this.recipeItems;
	}

	@Override
	public boolean showNotification() {
		return this.showNotification;
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		return i >= this.width && j >= this.height;
	}

	public boolean matches(CraftingContainer craftingContainer, Level level) {
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
						ingredient = this.recipeItems.get(this.width - m - 1 + n * this.width);
					} else {
						ingredient = this.recipeItems.get(m + n * this.width);
					}
				}

				if (!ingredient.test(craftingContainer.getItem(k + l * craftingContainer.getWidth()))) {
					return false;
				}
			}
		}

		return true;
	}

	public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
		return this.getResultItem(registryAccess).copy();
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
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

	@Override
	public boolean isIncomplete() {
		NonNullList<Ingredient> nonNullList = this.getIngredients();
		return nonNullList.isEmpty() || nonNullList.stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(ingredient -> ingredient.getItems().length == 0);
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

	public static class Serializer implements RecipeSerializer<ShapedRecipe> {
		static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().flatXmap(list -> {
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
		}, DataResult::success);
		static final Codec<String> SINGLE_CHARACTER_STRING_CODEC = Codec.STRING.flatXmap(string -> {
			if (string.length() != 1) {
				return DataResult.error(() -> "Invalid key entry: '" + string + "' is an invalid symbol (must be 1 character only).");
			} else {
				return " ".equals(string) ? DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.") : DataResult.success(string);
			}
		}, DataResult::success);
		private static final Codec<ShapedRecipe> CODEC = ShapedRecipe.Serializer.RawShapedRecipe.CODEC
			.flatXmap(
				rawShapedRecipe -> {
					String[] strings = ShapedRecipe.shrink(rawShapedRecipe.pattern);
					int i = strings[0].length();
					int j = strings.length;
					NonNullList<Ingredient> nonNullList = NonNullList.withSize(i * j, Ingredient.EMPTY);
					Set<String> set = Sets.<String>newHashSet(rawShapedRecipe.key.keySet());

					for (int k = 0; k < strings.length; k++) {
						String string = strings[k];

						for (int l = 0; l < string.length(); l++) {
							String string2 = string.substring(l, l + 1);
							Ingredient ingredient = string2.equals(" ") ? Ingredient.EMPTY : (Ingredient)rawShapedRecipe.key.get(string2);
							if (ingredient == null) {
								return DataResult.error(() -> "Pattern references symbol '" + string2 + "' but it's not defined in the key");
							}

							set.remove(string2);
							nonNullList.set(l + i * k, ingredient);
						}
					}

					if (!set.isEmpty()) {
						return DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + set);
					} else {
						ShapedRecipe shapedRecipe = new ShapedRecipe(
							rawShapedRecipe.group, rawShapedRecipe.category, i, j, nonNullList, rawShapedRecipe.result, rawShapedRecipe.showNotification
						);
						return DataResult.success(shapedRecipe);
					}
				},
				shapedRecipe -> {
					throw new NotImplementedException("Serializing ShapedRecipe is not implemented yet.");
				}
			);

		@Override
		public Codec<ShapedRecipe> codec() {
			return CODEC;
		}

		public ShapedRecipe fromNetwork(FriendlyByteBuf friendlyByteBuf) {
			int i = friendlyByteBuf.readVarInt();
			int j = friendlyByteBuf.readVarInt();
			String string = friendlyByteBuf.readUtf();
			CraftingBookCategory craftingBookCategory = friendlyByteBuf.readEnum(CraftingBookCategory.class);
			NonNullList<Ingredient> nonNullList = NonNullList.withSize(i * j, Ingredient.EMPTY);

			for (int k = 0; k < nonNullList.size(); k++) {
				nonNullList.set(k, Ingredient.fromNetwork(friendlyByteBuf));
			}

			ItemStack itemStack = friendlyByteBuf.readItem();
			boolean bl = friendlyByteBuf.readBoolean();
			return new ShapedRecipe(string, craftingBookCategory, i, j, nonNullList, itemStack, bl);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, ShapedRecipe shapedRecipe) {
			friendlyByteBuf.writeVarInt(shapedRecipe.width);
			friendlyByteBuf.writeVarInt(shapedRecipe.height);
			friendlyByteBuf.writeUtf(shapedRecipe.group);
			friendlyByteBuf.writeEnum(shapedRecipe.category);

			for (Ingredient ingredient : shapedRecipe.recipeItems) {
				ingredient.toNetwork(friendlyByteBuf);
			}

			friendlyByteBuf.writeItem(shapedRecipe.result);
			friendlyByteBuf.writeBoolean(shapedRecipe.showNotification);
		}

		static record RawShapedRecipe(
			String group, CraftingBookCategory category, Map<String, Ingredient> key, List<String> pattern, ItemStack result, boolean showNotification
		) {
			public static final Codec<ShapedRecipe.Serializer.RawShapedRecipe> CODEC = RecordCodecBuilder.create(
				instance -> instance.group(
							ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(rawShapedRecipe -> rawShapedRecipe.group),
							CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(rawShapedRecipe -> rawShapedRecipe.category),
							ExtraCodecs.strictUnboundedMap(ShapedRecipe.Serializer.SINGLE_CHARACTER_STRING_CODEC, Ingredient.CODEC_NONEMPTY)
								.fieldOf("key")
								.forGetter(rawShapedRecipe -> rawShapedRecipe.key),
							ShapedRecipe.Serializer.PATTERN_CODEC.fieldOf("pattern").forGetter(rawShapedRecipe -> rawShapedRecipe.pattern),
							CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter(rawShapedRecipe -> rawShapedRecipe.result),
							ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter(rawShapedRecipe -> rawShapedRecipe.showNotification)
						)
						.apply(instance, ShapedRecipe.Serializer.RawShapedRecipe::new)
			);
		}
	}
}
