package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class Ingredient implements Predicate<ItemStack> {
	public static final Ingredient EMPTY = new Ingredient(Stream.empty());
	private final Ingredient.Value[] values;
	private ItemStack[] itemStacks;
	private IntList stackingIds;

	private Ingredient(Stream<? extends Ingredient.Value> stream) {
		this.values = (Ingredient.Value[])stream.toArray(Ingredient.Value[]::new);
	}

	@Environment(EnvType.CLIENT)
	public ItemStack[] getItems() {
		this.dissolve();
		return this.itemStacks;
	}

	private void dissolve() {
		if (this.itemStacks == null) {
			this.itemStacks = (ItemStack[])Arrays.stream(this.values).flatMap(value -> value.getItems().stream()).distinct().toArray(ItemStack[]::new);
		}
	}

	public boolean test(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		} else {
			this.dissolve();
			if (this.itemStacks.length == 0) {
				return itemStack.isEmpty();
			} else {
				for (ItemStack itemStack2 : this.itemStacks) {
					if (itemStack2.is(itemStack.getItem())) {
						return true;
					}
				}

				return false;
			}
		}
	}

	public IntList getStackingIds() {
		if (this.stackingIds == null) {
			this.dissolve();
			this.stackingIds = new IntArrayList(this.itemStacks.length);

			for (ItemStack itemStack : this.itemStacks) {
				this.stackingIds.add(StackedContents.getStackingIndex(itemStack));
			}

			this.stackingIds.sort(IntComparators.NATURAL_COMPARATOR);
		}

		return this.stackingIds;
	}

	public void toNetwork(FriendlyByteBuf friendlyByteBuf) {
		this.dissolve();
		friendlyByteBuf.writeVarInt(this.itemStacks.length);

		for (int i = 0; i < this.itemStacks.length; i++) {
			friendlyByteBuf.writeItem(this.itemStacks[i]);
		}
	}

	public JsonElement toJson() {
		if (this.values.length == 1) {
			return this.values[0].serialize();
		} else {
			JsonArray jsonArray = new JsonArray();

			for (Ingredient.Value value : this.values) {
				jsonArray.add(value.serialize());
			}

			return jsonArray;
		}
	}

	public boolean isEmpty() {
		return this.values.length == 0 && (this.itemStacks == null || this.itemStacks.length == 0) && (this.stackingIds == null || this.stackingIds.isEmpty());
	}

	private static Ingredient fromValues(Stream<? extends Ingredient.Value> stream) {
		Ingredient ingredient = new Ingredient(stream);
		return ingredient.values.length == 0 ? EMPTY : ingredient;
	}

	public static Ingredient of(ItemLike... itemLikes) {
		return of(Arrays.stream(itemLikes).map(ItemStack::new));
	}

	@Environment(EnvType.CLIENT)
	public static Ingredient of(ItemStack... itemStacks) {
		return of(Arrays.stream(itemStacks));
	}

	public static Ingredient of(Stream<ItemStack> stream) {
		return fromValues(stream.filter(itemStack -> !itemStack.isEmpty()).map(itemStack -> new Ingredient.ItemValue(itemStack)));
	}

	public static Ingredient of(Tag<Item> tag) {
		return fromValues(Stream.of(new Ingredient.TagValue(tag)));
	}

	public static Ingredient fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		int i = friendlyByteBuf.readVarInt();
		return fromValues(Stream.generate(() -> new Ingredient.ItemValue(friendlyByteBuf.readItem())).limit((long)i));
	}

	public static Ingredient fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement == null || jsonElement.isJsonNull()) {
			throw new JsonSyntaxException("Item cannot be null");
		} else if (jsonElement.isJsonObject()) {
			return fromValues(Stream.of(valueFromJson(jsonElement.getAsJsonObject())));
		} else if (jsonElement.isJsonArray()) {
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			if (jsonArray.size() == 0) {
				throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
			} else {
				return fromValues(
					StreamSupport.stream(jsonArray.spliterator(), false).map(jsonElementx -> valueFromJson(GsonHelper.convertToJsonObject(jsonElementx, "item")))
				);
			}
		} else {
			throw new JsonSyntaxException("Expected item to be object or array of objects");
		}
	}

	private static Ingredient.Value valueFromJson(JsonObject jsonObject) {
		if (jsonObject.has("item") && jsonObject.has("tag")) {
			throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
		} else if (jsonObject.has("item")) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "item"));
			Item item = (Item)Registry.ITEM.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + resourceLocation + "'"));
			return new Ingredient.ItemValue(new ItemStack(item));
		} else if (jsonObject.has("tag")) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
			Tag<Item> tag = SerializationTags.getInstance().getItems().getTag(resourceLocation);
			if (tag == null) {
				throw new JsonSyntaxException("Unknown item tag '" + resourceLocation + "'");
			} else {
				return new Ingredient.TagValue(tag);
			}
		} else {
			throw new JsonParseException("An ingredient entry needs either a tag or an item");
		}
	}

	static class ItemValue implements Ingredient.Value {
		private final ItemStack item;

		private ItemValue(ItemStack itemStack) {
			this.item = itemStack;
		}

		@Override
		public Collection<ItemStack> getItems() {
			return Collections.singleton(this.item);
		}

		@Override
		public JsonObject serialize() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("item", Registry.ITEM.getKey(this.item.getItem()).toString());
			return jsonObject;
		}
	}

	static class TagValue implements Ingredient.Value {
		private final Tag<Item> tag;

		private TagValue(Tag<Item> tag) {
			this.tag = tag;
		}

		@Override
		public Collection<ItemStack> getItems() {
			List<ItemStack> list = Lists.<ItemStack>newArrayList();

			for (Item item : this.tag.getValues()) {
				list.add(new ItemStack(item));
			}

			return list;
		}

		@Override
		public JsonObject serialize() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("tag", SerializationTags.getInstance().getItems().getIdOrThrow(this.tag).toString());
			return jsonObject;
		}
	}

	interface Value {
		Collection<ItemStack> getItems();

		JsonObject serialize();
	}
}
