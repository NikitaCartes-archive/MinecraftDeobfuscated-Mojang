package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class EnchantRandomlyFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Codec<HolderSet<Enchantment>> ENCHANTMENT_SET_CODEC = BuiltInRegistries.ENCHANTMENT
		.holderByNameCodec()
		.listOf()
		.xmap(HolderSet::direct, holderSet -> holderSet.stream().toList());
	public static final MapCodec<EnchantRandomlyFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.and(ENCHANTMENT_SET_CODEC.optionalFieldOf("enchantments").forGetter(enchantRandomlyFunction -> enchantRandomlyFunction.enchantments))
				.apply(instance, EnchantRandomlyFunction::new)
	);
	private final Optional<HolderSet<Enchantment>> enchantments;

	EnchantRandomlyFunction(List<LootItemCondition> list, Optional<HolderSet<Enchantment>> optional) {
		super(list);
		this.enchantments = optional;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.ENCHANT_RANDOMLY;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		RandomSource randomSource = lootContext.getRandom();
		Optional<Holder<Enchantment>> optional = this.enchantments
			.flatMap(holderSet -> holderSet.getRandomElement(randomSource))
			.or(
				() -> {
					boolean bl = itemStack.is(Items.BOOK);
					List<Holder.Reference<Enchantment>> list = BuiltInRegistries.ENCHANTMENT
						.holders()
						.filter(reference -> ((Enchantment)reference.value()).isEnabled(lootContext.getLevel().enabledFeatures()))
						.filter(reference -> ((Enchantment)reference.value()).isDiscoverable())
						.filter(reference -> bl || ((Enchantment)reference.value()).canEnchant(itemStack))
						.toList();
					return Util.getRandomSafe(list, randomSource);
				}
			);
		if (optional.isEmpty()) {
			LOGGER.warn("Couldn't find a compatible enchantment for {}", itemStack);
			return itemStack;
		} else {
			return enchantItem(itemStack, (Enchantment)((Holder)optional.get()).value(), randomSource);
		}
	}

	private static ItemStack enchantItem(ItemStack itemStack, Enchantment enchantment, RandomSource randomSource) {
		int i = Mth.nextInt(randomSource, enchantment.getMinLevel(), enchantment.getMaxLevel());
		if (itemStack.is(Items.BOOK)) {
			itemStack = new ItemStack(Items.ENCHANTED_BOOK);
		}

		itemStack.enchant(enchantment, i);
		return itemStack;
	}

	public static EnchantRandomlyFunction.Builder randomEnchantment() {
		return new EnchantRandomlyFunction.Builder();
	}

	public static LootItemConditionalFunction.Builder<?> randomApplicableEnchantment() {
		return simpleBuilder(list -> new EnchantRandomlyFunction(list, Optional.empty()));
	}

	public static class Builder extends LootItemConditionalFunction.Builder<EnchantRandomlyFunction.Builder> {
		private final List<Holder<Enchantment>> enchantments = new ArrayList();

		protected EnchantRandomlyFunction.Builder getThis() {
			return this;
		}

		public EnchantRandomlyFunction.Builder withEnchantment(Enchantment enchantment) {
			this.enchantments.add(enchantment.builtInRegistryHolder());
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new EnchantRandomlyFunction(this.getConditions(), this.enchantments.isEmpty() ? Optional.empty() : Optional.of(HolderSet.direct(this.enchantments)));
		}
	}
}
