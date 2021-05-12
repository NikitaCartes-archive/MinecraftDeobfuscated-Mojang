package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe {
	final int width;
	final int height;
	final NonNullList<Ingredient> recipeItems;
	final ItemStack result;
	private final ResourceLocation id;
	final String group;

	public ShapedRecipe(ResourceLocation resourceLocation, String string, int i, int j, NonNullList<Ingredient> nonNullList, ItemStack itemStack) {
		this.id = resourceLocation;
		this.group = string;
		this.width = i;
		this.height = j;
		this.recipeItems = nonNullList;
		this.result = itemStack;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
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
	public ItemStack getResultItem() {
		return this.result;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return this.recipeItems;
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

	public ItemStack assemble(CraftingContainer craftingContainer) {
		return this.getResultItem().copy();
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	static NonNullList<Ingredient> dissolvePattern(String[] strings, Map<String, Ingredient> map, int i, int j) {
		NonNullList<Ingredient> nonNullList = NonNullList.withSize(i * j, Ingredient.EMPTY);
		Set<String> set = Sets.<String>newHashSet(map.keySet());
		set.remove(" ");

		for (int k = 0; k < strings.length; k++) {
			for (int l = 0; l < strings[k].length(); l++) {
				String string = strings[k].substring(l, l + 1);
				Ingredient ingredient = (Ingredient)map.get(string);
				if (ingredient == null) {
					throw new JsonSyntaxException("Pattern references symbol '" + string + "' but it's not defined in the key");
				}

				set.remove(string);
				nonNullList.set(l + i * k, ingredient);
			}
		}

		if (!set.isEmpty()) {
			throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
		} else {
			return nonNullList;
		}
	}

	@VisibleForTesting
	static String[] shrink(String... strings) {
		int i = Integer.MAX_VALUE;
		int j = 0;
		int k = 0;
		int l = 0;

		for (int m = 0; m < strings.length; m++) {
			String string = strings[m];
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

		if (strings.length == l) {
			return new String[0];
		} else {
			String[] strings2 = new String[strings.length - l - k];

			for (int o = 0; o < strings2.length; o++) {
				strings2[o] = strings[o + k].substring(i, j + 1);
			}

			return strings2;
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

	static String[] patternFromJson(JsonArray jsonArray) {
		String[] strings = new String[jsonArray.size()];
		if (strings.length > 3) {
			throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
		} else if (strings.length == 0) {
			throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
		} else {
			for (int i = 0; i < strings.length; i++) {
				String string = GsonHelper.convertToString(jsonArray.get(i), "pattern[" + i + "]");
				if (string.length() > 3) {
					throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
				}

				if (i > 0 && strings[0].length() != string.length()) {
					throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
				}

				strings[i] = string;
			}

			return strings;
		}
	}

	static Map<String, Ingredient> keyFromJson(JsonObject jsonObject) {
		Map<String, Ingredient> map = Maps.<String, Ingredient>newHashMap();

		for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			if (((String)entry.getKey()).length() != 1) {
				throw new JsonSyntaxException("Invalid key entry: '" + (String)entry.getKey() + "' is an invalid symbol (must be 1 character only).");
			}

			if (" ".equals(entry.getKey())) {
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
			}

			map.put((String)entry.getKey(), Ingredient.fromJson((JsonElement)entry.getValue()));
		}

		map.put(" ", Ingredient.EMPTY);
		return map;
	}

	public static ItemStack itemStackFromJson(JsonObject jsonObject) {
		Item item = itemFromJson(jsonObject);
		if (jsonObject.has("data")) {
			throw new JsonParseException("Disallowed data tag found");
		} else {
			int i = GsonHelper.getAsInt(jsonObject, "count", 1);
			if (i < 1) {
				throw new JsonSyntaxException("Invalid output count: " + i);
			} else {
				return new ItemStack(item, i);
			}
		}
	}

	public static Item itemFromJson(JsonObject jsonObject) {
		String string = GsonHelper.getAsString(jsonObject, "item");
		Item item = (Item)Registry.ITEM.getOptional(new ResourceLocation(string)).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + string + "'"));
		if (item == Items.AIR) {
			throw new JsonSyntaxException("Invalid item: " + string);
		} else {
			return item;
		}
	}

	public static class Serializer implements RecipeSerializer<ShapedRecipe> {
		public ShapedRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
			String string = GsonHelper.getAsString(jsonObject, "group", "");
			Map<String, Ingredient> map = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(jsonObject, "key"));
			String[] strings = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(jsonObject, "pattern")));
			int i = strings[0].length();
			int j = strings.length;
			NonNullList<Ingredient> nonNullList = ShapedRecipe.dissolvePattern(strings, map, i, j);
			ItemStack itemStack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));
			return new ShapedRecipe(resourceLocation, string, i, j, nonNullList, itemStack);
		}

		public ShapedRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
			int i = friendlyByteBuf.readVarInt();
			int j = friendlyByteBuf.readVarInt();
			String string = friendlyByteBuf.readUtf();
			NonNullList<Ingredient> nonNullList = NonNullList.withSize(i * j, Ingredient.EMPTY);

			for (int k = 0; k < nonNullList.size(); k++) {
				nonNullList.set(k, Ingredient.fromNetwork(friendlyByteBuf));
			}

			ItemStack itemStack = friendlyByteBuf.readItem();
			return new ShapedRecipe(resourceLocation, string, i, j, nonNullList, itemStack);
		}

		public void toNetwork(FriendlyByteBuf friendlyByteBuf, ShapedRecipe shapedRecipe) {
			friendlyByteBuf.writeVarInt(shapedRecipe.width);
			friendlyByteBuf.writeVarInt(shapedRecipe.height);
			friendlyByteBuf.writeUtf(shapedRecipe.group);

			for (Ingredient ingredient : shapedRecipe.recipeItems) {
				ingredient.toNetwork(friendlyByteBuf);
			}

			friendlyByteBuf.writeItem(shapedRecipe.result);
		}
	}
}
