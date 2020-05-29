package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry extends LootPoolSingletonContainer {
	private final Tag<Item> tag;
	private final boolean expand;

	private TagEntry(Tag<Item> tag, boolean bl, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
		super(i, j, lootItemConditions, lootItemFunctions);
		this.tag = tag;
		this.expand = bl;
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.TAG;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
		this.tag.getValues().forEach(item -> consumer.accept(new ItemStack(item)));
	}

	private boolean expandTag(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
		if (!this.canRun(lootContext)) {
			return false;
		} else {
			for (final Item item : this.tag.getValues()) {
				consumer.accept(new LootPoolSingletonContainer.EntryBase() {
					@Override
					public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
						consumer.accept(new ItemStack(item));
					}
				});
			}

			return true;
		}
	}

	@Override
	public boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
		return this.expand ? this.expandTag(lootContext, consumer) : super.expand(lootContext, consumer);
	}

	public static LootPoolSingletonContainer.Builder<?> expandTag(Tag<Item> tag) {
		return simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new TagEntry(tag, true, i, j, lootItemConditions, lootItemFunctions));
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<TagEntry> {
		public void serializeCustom(JsonObject jsonObject, TagEntry tagEntry, JsonSerializationContext jsonSerializationContext) {
			super.serializeCustom(jsonObject, tagEntry, jsonSerializationContext);
			jsonObject.addProperty("name", SerializationTags.getInstance().getItems().getIdOrThrow(tagEntry.tag).toString());
			jsonObject.addProperty("expand", tagEntry.expand);
		}

		protected TagEntry deserialize(
			JsonObject jsonObject,
			JsonDeserializationContext jsonDeserializationContext,
			int i,
			int j,
			LootItemCondition[] lootItemConditions,
			LootItemFunction[] lootItemFunctions
		) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
			Tag<Item> tag = SerializationTags.getInstance().getItems().getTag(resourceLocation);
			if (tag == null) {
				throw new JsonParseException("Can't find tag: " + resourceLocation);
			} else {
				boolean bl = GsonHelper.getAsBoolean(jsonObject, "expand");
				return new TagEntry(tag, bl, i, j, lootItemConditions, lootItemFunctions);
			}
		}
	}
}
