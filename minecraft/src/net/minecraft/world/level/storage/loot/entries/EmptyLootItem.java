package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EmptyLootItem extends LootPoolSingletonContainer {
	private EmptyLootItem(int i, int j, LootItemCondition[] lootItemConditions, LootItemFunction[] lootItemFunctions) {
		super(i, j, lootItemConditions, lootItemFunctions);
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.EMPTY;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
	}

	public static LootPoolSingletonContainer.Builder<?> emptyItem() {
		return simpleBuilder(EmptyLootItem::new);
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<EmptyLootItem> {
		public EmptyLootItem deserialize(
			JsonObject jsonObject,
			JsonDeserializationContext jsonDeserializationContext,
			int i,
			int j,
			LootItemCondition[] lootItemConditions,
			LootItemFunction[] lootItemFunctions
		) {
			return new EmptyLootItem(i, j, lootItemConditions, lootItemFunctions);
		}
	}
}
