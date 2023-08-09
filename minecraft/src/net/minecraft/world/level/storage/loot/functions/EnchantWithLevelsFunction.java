package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class EnchantWithLevelsFunction extends LootItemConditionalFunction {
	public static final Codec<EnchantWithLevelsFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.<NumberProvider, boolean>and(
					instance.group(
						NumberProviders.CODEC.fieldOf("levels").forGetter(enchantWithLevelsFunction -> enchantWithLevelsFunction.levels),
						Codec.BOOL.fieldOf("treasure").orElse(false).forGetter(enchantWithLevelsFunction -> enchantWithLevelsFunction.treasure)
					)
				)
				.apply(instance, EnchantWithLevelsFunction::new)
	);
	private final NumberProvider levels;
	private final boolean treasure;

	EnchantWithLevelsFunction(List<LootItemCondition> list, NumberProvider numberProvider, boolean bl) {
		super(list);
		this.levels = numberProvider;
		this.treasure = bl;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.ENCHANT_WITH_LEVELS;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.levels.getReferencedContextParams();
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		RandomSource randomSource = lootContext.getRandom();
		return EnchantmentHelper.enchantItem(randomSource, itemStack, this.levels.getInt(lootContext), this.treasure);
	}

	public static EnchantWithLevelsFunction.Builder enchantWithLevels(NumberProvider numberProvider) {
		return new EnchantWithLevelsFunction.Builder(numberProvider);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<EnchantWithLevelsFunction.Builder> {
		private final NumberProvider levels;
		private boolean treasure;

		public Builder(NumberProvider numberProvider) {
			this.levels = numberProvider;
		}

		protected EnchantWithLevelsFunction.Builder getThis() {
			return this;
		}

		public EnchantWithLevelsFunction.Builder allowTreasure() {
			this.treasure = true;
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new EnchantWithLevelsFunction(this.getConditions(), this.levels, this.treasure);
		}
	}
}
