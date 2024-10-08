package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetEnchantmentsFunction extends LootItemConditionalFunction {
	public static final MapCodec<SetEnchantmentsFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<Map<Holder<Enchantment>, NumberProvider>, boolean>and(
					instance.group(
						Codec.unboundedMap(Enchantment.CODEC, NumberProviders.CODEC)
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
	public Set<ContextKey<?>> getReferencedContextParams() {
		return (Set<ContextKey<?>>)this.enchantments
			.values()
			.stream()
			.flatMap(numberProvider -> numberProvider.getReferencedContextParams().stream())
			.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.is(Items.BOOK)) {
			itemStack = itemStack.transmuteCopy(Items.ENCHANTED_BOOK);
		}

		EnchantmentHelper.updateEnchantments(
			itemStack,
			mutable -> {
				if (this.add) {
					this.enchantments
						.forEach((holder, numberProvider) -> mutable.set(holder, Mth.clamp(mutable.getLevel(holder) + numberProvider.getInt(lootContext), 0, 255)));
				} else {
					this.enchantments.forEach((holder, numberProvider) -> mutable.set(holder, Mth.clamp(numberProvider.getInt(lootContext), 0, 255)));
				}
			}
		);
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

		public SetEnchantmentsFunction.Builder withEnchantment(Holder<Enchantment> holder, NumberProvider numberProvider) {
			this.enchantments.put(holder, numberProvider);
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new SetEnchantmentsFunction(this.getConditions(), this.enchantments.build(), this.add);
		}
	}
}
