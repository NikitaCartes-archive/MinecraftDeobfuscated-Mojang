package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EmptyLootItem extends LootPoolSingletonContainer {
	public static final MapCodec<EmptyLootItem> CODEC = RecordCodecBuilder.mapCodec(instance -> singletonFields(instance).apply(instance, EmptyLootItem::new));

	private EmptyLootItem(int i, int j, List<LootItemCondition> list, List<LootItemFunction> list2) {
		super(i, j, list, list2);
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
}
