package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
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
	public static final MapCodec<EnchantRandomlyFunction> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<Optional<HolderSet<Enchantment>>, boolean>and(
					instance.group(
						RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter(enchantRandomlyFunction -> enchantRandomlyFunction.options),
						Codec.BOOL.optionalFieldOf("only_compatible", Boolean.valueOf(true)).forGetter(enchantRandomlyFunction -> enchantRandomlyFunction.onlyCompatible)
					)
				)
				.apply(instance, EnchantRandomlyFunction::new)
	);
	private final Optional<HolderSet<Enchantment>> options;
	private final boolean onlyCompatible;

	EnchantRandomlyFunction(List<LootItemCondition> list, Optional<HolderSet<Enchantment>> optional, boolean bl) {
		super(list);
		this.options = optional;
		this.onlyCompatible = bl;
	}

	@Override
	public LootItemFunctionType<EnchantRandomlyFunction> getType() {
		return LootItemFunctions.ENCHANT_RANDOMLY;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		RandomSource randomSource = lootContext.getRandom();
		boolean bl = itemStack.is(Items.BOOK);
		boolean bl2 = !bl && this.onlyCompatible;
		Stream<Holder<Enchantment>> stream = ((Stream)this.options
				.map(HolderSet::stream)
				.orElseGet(() -> lootContext.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().map(Function.identity())))
			.filter(holder -> !bl2 || ((Enchantment)holder.value()).canEnchant(itemStack));
		List<Holder<Enchantment>> list = stream.toList();
		Optional<Holder<Enchantment>> optional = Util.getRandomSafe(list, randomSource);
		if (optional.isEmpty()) {
			LOGGER.warn("Couldn't find a compatible enchantment for {}", itemStack);
			return itemStack;
		} else {
			return enchantItem(itemStack, (Holder<Enchantment>)optional.get(), randomSource);
		}
	}

	private static ItemStack enchantItem(ItemStack itemStack, Holder<Enchantment> holder, RandomSource randomSource) {
		int i = Mth.nextInt(randomSource, holder.value().getMinLevel(), holder.value().getMaxLevel());
		if (itemStack.is(Items.BOOK)) {
			itemStack = new ItemStack(Items.ENCHANTED_BOOK);
		}

		itemStack.enchant(holder, i);
		return itemStack;
	}

	public static EnchantRandomlyFunction.Builder randomEnchantment() {
		return new EnchantRandomlyFunction.Builder();
	}

	public static EnchantRandomlyFunction.Builder randomApplicableEnchantment(HolderLookup.Provider provider) {
		return randomEnchantment().withOneOf(provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.ON_RANDOM_LOOT));
	}

	public static class Builder extends LootItemConditionalFunction.Builder<EnchantRandomlyFunction.Builder> {
		private Optional<HolderSet<Enchantment>> options = Optional.empty();
		private boolean onlyCompatible = true;

		protected EnchantRandomlyFunction.Builder getThis() {
			return this;
		}

		public EnchantRandomlyFunction.Builder withEnchantment(Holder<Enchantment> holder) {
			this.options = Optional.of(HolderSet.direct(holder));
			return this;
		}

		public EnchantRandomlyFunction.Builder withOneOf(HolderSet<Enchantment> holderSet) {
			this.options = Optional.of(holderSet);
			return this;
		}

		public EnchantRandomlyFunction.Builder allowingIncompatibleEnchantments() {
			this.onlyCompatible = false;
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new EnchantRandomlyFunction(this.getConditions(), this.options, this.onlyCompatible);
		}
	}
}
