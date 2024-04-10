package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetEnchantmentsFunction extends LootItemConditionalFunction {
	public static final MapCodec<SetEnchantmentsFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<Map<Holder<Enchantment>, NumberProvider>, boolean>and(
					instance.group(
						Codec.unboundedMap(BuiltInRegistries.ENCHANTMENT.holderByNameCodec(), NumberProviders.CODEC)
							.optionalFieldOf("enchantments", Map.of())
							.forGetter(setEnchantmentsFunction -> setEnchantmentsFunction.enchantments),
						Codec.BOOL.fieldOf("add").orElse(false).forGetter(setEnchantmentsFunction -> setEnchantmentsFunction.add)
					)
				)
				.apply(instance, SetEnchantmentsFunction::new)
	);
	private final Map<Holder<Enchantment>, NumberProvider> enchantments;
	private final boolean add;

	SetEnchantmentsFunction(List<LootItemCondition> list, Map<Holder<Enchantment>, NumberProvider> map, boolean bl) {
		super(list);
		this.enchantments = Map.copyOf(map);
		this.add = bl;
	}

	@Override
	public LootItemFunctionType<SetEnchantmentsFunction> getType() {
		return LootItemFunctions.SET_ENCHANTMENTS;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return (Set<LootContextParam<?>>)this.enchantments
			.values()
			.stream()
			.flatMap(numberProvider -> numberProvider.getReferencedContextParams().stream())
			.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Object2IntMap<Enchantment> object2IntMap = new Object2IntOpenHashMap<>();
		this.enchantments.forEach((holder, numberProvider) -> object2IntMap.put((Enchantment)holder.value(), Mth.clamp(numberProvider.getInt(lootContext), 0, 255)));
		if (itemStack.is(Items.BOOK)) {
			itemStack = itemStack.transmuteCopy(Items.ENCHANTED_BOOK, itemStack.getCount());
			itemStack.set(DataComponents.STORED_ENCHANTMENTS, itemStack.remove(DataComponents.ENCHANTMENTS));
		}

		EnchantmentHelper.updateEnchantments(itemStack, mutable -> {
			if (this.add) {
				object2IntMap.forEach((enchantment, integer) -> mutable.set(enchantment, mutable.getLevel(enchantment) + integer));
			} else {
				object2IntMap.forEach(mutable::set);
			}
		});
		return itemStack;
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetEnchantmentsFunction.Builder> {
		private final ImmutableMap.Builder<Holder<Enchantment>, NumberProvider> enchantments = ImmutableMap.builder();
		private final boolean add;

		public Builder() {
			this(false);
		}

		public Builder(boolean bl) {
			this.add = bl;
		}

		protected SetEnchantmentsFunction.Builder getThis() {
			return this;
		}

		public SetEnchantmentsFunction.Builder withEnchantment(Enchantment enchantment, NumberProvider numberProvider) {
			this.enchantments.put(enchantment.builtInRegistryHolder(), numberProvider);
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new SetEnchantmentsFunction(this.getConditions(), this.enchantments.build(), this.add);
		}
	}
}
