package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry extends LootPoolSingletonContainer {
	final TagKey<Item> tag;
	final boolean expand;

	TagEntry(TagKey<Item> tagKey, boolean bl, int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
		super(i, j, lootItemConditions, lootItemFunctions);
		this.tag = tagKey;
		this.expand = bl;
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.TAG;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
		Registry.ITEM.getTagOrEmpty(this.tag).forEach(holder -> consumer.accept(new ItemStack(holder)));
	}

	private boolean expandTag(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
		if (!this.canRun(lootContext)) {
			return false;
		} else {
			for (final Holder<Item> holder : Registry.ITEM.getTagOrEmpty(this.tag)) {
				consumer.accept(new LootPoolSingletonContainer.EntryBase() {
					@Override
					public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
						consumer.accept(new ItemStack(holder));
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

	public static LootPoolSingletonContainer.Builder<?> tagContents(TagKey<Item> tagKey) {
		return simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new TagEntry(tagKey, false, i, j, lootItemConditions, lootItemFunctions));
	}

	public static LootPoolSingletonContainer.Builder<?> expandTag(TagKey<Item> tagKey) {
		return simpleBuilder((i, j, lootItemConditions, lootItemFunctions) -> new TagEntry(tagKey, true, i, j, lootItemConditions, lootItemFunctions));
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<TagEntry> {
		public void serializeCustom(JsonObject jsonObject, TagEntry tagEntry, JsonSerializationContext jsonSerializationContext) {
			super.serializeCustom(jsonObject, tagEntry, jsonSerializationContext);
			jsonObject.addProperty("name", tagEntry.tag.location().toString());
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
			TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, resourceLocation);
			boolean bl = GsonHelper.getAsBoolean(jsonObject, "expand");
			return new TagEntry(tagKey, bl, i, j, lootItemConditions, lootItemFunctions);
		}
	}
}
