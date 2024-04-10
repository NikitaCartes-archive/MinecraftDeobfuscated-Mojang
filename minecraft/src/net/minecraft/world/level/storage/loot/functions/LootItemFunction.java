package net.minecraft.world.level.storage.loot.functions;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface LootItemFunction extends LootContextUser, BiFunction<ItemStack, LootContext, ItemStack> {
	LootItemFunctionType<? extends LootItemFunction> getType();

	static Consumer<ItemStack> decorate(BiFunction<ItemStack, LootContext, ItemStack> biFunction, Consumer<ItemStack> consumer, LootContext lootContext) {
		return itemStack -> consumer.accept((ItemStack)biFunction.apply(itemStack, lootContext));
	}

	public interface Builder {
		LootItemFunction build();
	}
}
