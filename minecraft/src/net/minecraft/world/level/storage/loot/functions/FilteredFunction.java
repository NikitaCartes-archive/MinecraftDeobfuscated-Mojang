package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FilteredFunction extends LootItemConditionalFunction {
	public static final MapCodec<FilteredFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<ItemPredicate, LootItemFunction>and(
					instance.group(
						ItemPredicate.CODEC.fieldOf("item_filter").forGetter(filteredFunction -> filteredFunction.filter),
						LootItemFunctions.ROOT_CODEC.fieldOf("modifier").forGetter(filteredFunction -> filteredFunction.modifier)
					)
				)
				.apply(instance, FilteredFunction::new)
	);
	private final ItemPredicate filter;
	private final LootItemFunction modifier;

	private FilteredFunction(List<LootItemCondition> list, ItemPredicate itemPredicate, LootItemFunction lootItemFunction) {
		super(list);
		this.filter = itemPredicate;
		this.modifier = lootItemFunction;
	}

	@Override
	public LootItemFunctionType<FilteredFunction> getType() {
		return LootItemFunctions.FILTERED;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		return this.filter.test(itemStack) ? (ItemStack)this.modifier.apply(itemStack, lootContext) : itemStack;
	}

	@Override
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);
		this.modifier.validate(validationContext.forChild(".modifier"));
	}
}
