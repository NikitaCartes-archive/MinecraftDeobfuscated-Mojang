package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class EnchantedCountIncreaseFunction extends LootItemConditionalFunction {
	public static final int NO_LIMIT = 0;
	public static final MapCodec<EnchantedCountIncreaseFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<Holder<Enchantment>, NumberProvider, int>and(
					instance.group(
						Enchantment.CODEC.fieldOf("enchantment").forGetter(enchantedCountIncreaseFunction -> enchantedCountIncreaseFunction.enchantment),
						NumberProviders.CODEC.fieldOf("count").forGetter(enchantedCountIncreaseFunction -> enchantedCountIncreaseFunction.value),
						Codec.INT.optionalFieldOf("limit", Integer.valueOf(0)).forGetter(enchantedCountIncreaseFunction -> enchantedCountIncreaseFunction.limit)
					)
				)
				.apply(instance, EnchantedCountIncreaseFunction::new)
	);
	private final Holder<Enchantment> enchantment;
	private final NumberProvider value;
	private final int limit;

	EnchantedCountIncreaseFunction(List<LootItemCondition> list, Holder<Enchantment> holder, NumberProvider numberProvider, int i) {
		super(list);
		this.enchantment = holder;
		this.value = numberProvider;
		this.limit = i;
	}

	@Override
	public LootItemFunctionType<EnchantedCountIncreaseFunction> getType() {
		return LootItemFunctions.ENCHANTED_COUNT_INCREASE;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return Sets.<LootContextParam<?>>union(ImmutableSet.of(LootContextParams.ATTACKING_ENTITY), this.value.getReferencedContextParams());
	}

	private boolean hasLimit() {
		return this.limit > 0;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Entity entity = lootContext.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
		if (entity instanceof LivingEntity livingEntity) {
			int i = EnchantmentHelper.getEnchantmentLevel(this.enchantment, livingEntity);
			if (i == 0) {
				return itemStack;
			}

			float f = (float)i * this.value.getFloat(lootContext);
			itemStack.grow(Math.round(f));
			if (this.hasLimit()) {
				itemStack.limitSize(this.limit);
			}
		}

		return itemStack;
	}

	public static EnchantedCountIncreaseFunction.Builder lootingMultiplier(HolderLookup.Provider provider, NumberProvider numberProvider) {
		HolderLookup.RegistryLookup<Enchantment> registryLookup = provider.lookupOrThrow(Registries.ENCHANTMENT);
		return new EnchantedCountIncreaseFunction.Builder(registryLookup.getOrThrow(Enchantments.LOOTING), numberProvider);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<EnchantedCountIncreaseFunction.Builder> {
		private final Holder<Enchantment> enchantment;
		private final NumberProvider count;
		private int limit = 0;

		public Builder(Holder<Enchantment> holder, NumberProvider numberProvider) {
			this.enchantment = holder;
			this.count = numberProvider;
		}

		protected EnchantedCountIncreaseFunction.Builder getThis() {
			return this;
		}

		public EnchantedCountIncreaseFunction.Builder setLimit(int i) {
			this.limit = i;
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new EnchantedCountIncreaseFunction(this.getConditions(), this.enchantment, this.count, this.limit);
		}
	}
}
