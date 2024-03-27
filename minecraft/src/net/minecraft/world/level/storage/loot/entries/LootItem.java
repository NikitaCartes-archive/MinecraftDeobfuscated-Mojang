package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItem extends LootPoolSingletonContainer {
	public static final MapCodec<LootItem> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("name").forGetter(lootItem -> lootItem.item))
				.<int, int, List<LootItemCondition>, List<LootItemFunction>>and(singletonFields(instance))
				.apply(instance, LootItem::new)
	);
	private final Holder<Item> item;

	private LootItem(Holder<Item> holder, int i, int j, List<LootItemCondition> list, List<LootItemFunction> list2) {
		super(i, j, list, list2);
		this.item = holder;
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.ITEM;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
		consumer.accept(new ItemStack(this.item));
	}

	public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike itemLike) {
		return simpleBuilder((i, j, list, list2) -> new LootItem(itemLike.asItem().builtInRegistryHolder(), i, j, list, list2));
	}
}
