package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyBonusCount extends LootItemConditionalFunction {
	private static final Map<ResourceLocation, ApplyBonusCount.FormulaType> FORMULAS = (Map<ResourceLocation, ApplyBonusCount.FormulaType>)Stream.of(
			ApplyBonusCount.BinomialWithBonusCount.TYPE, ApplyBonusCount.OreDrops.TYPE, ApplyBonusCount.UniformBonusCount.TYPE
		)
		.collect(Collectors.toMap(ApplyBonusCount.FormulaType::id, Function.identity()));
	private static final Codec<ApplyBonusCount.FormulaType> FORMULA_TYPE_CODEC = ResourceLocation.CODEC.comapFlatMap(resourceLocation -> {
		ApplyBonusCount.FormulaType formulaType = (ApplyBonusCount.FormulaType)FORMULAS.get(resourceLocation);
		return formulaType != null ? DataResult.success(formulaType) : DataResult.error(() -> "No formula type with id: '" + resourceLocation + "'");
	}, ApplyBonusCount.FormulaType::id);
	private static final MapCodec<ApplyBonusCount.Formula> FORMULA_CODEC = ExtraCodecs.dispatchOptionalValue(
		"formula", "parameters", FORMULA_TYPE_CODEC, ApplyBonusCount.Formula::getType, ApplyBonusCount.FormulaType::codec
	);
	public static final MapCodec<ApplyBonusCount> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<Holder<Enchantment>, ApplyBonusCount.Formula>and(
					instance.group(
						Enchantment.CODEC.fieldOf("enchantment").forGetter(applyBonusCount -> applyBonusCount.enchantment),
						FORMULA_CODEC.forGetter(applyBonusCount -> applyBonusCount.formula)
					)
				)
				.apply(instance, ApplyBonusCount::new)
	);
	private final Holder<Enchantment> enchantment;
	private final ApplyBonusCount.Formula formula;

	private ApplyBonusCount(List<LootItemCondition> list, Holder<Enchantment> holder, ApplyBonusCount.Formula formula) {
		super(list);
		this.enchantment = holder;
		this.formula = formula;
	}

	@Override
	public LootItemFunctionType<ApplyBonusCount> getType() {
		return LootItemFunctions.APPLY_BONUS;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.TOOL);
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		ItemStack itemStack2 = lootContext.getParamOrNull(LootContextParams.TOOL);
		if (itemStack2 != null) {
			int i = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemStack2);
			int j = this.formula.calculateNewCount(lootContext.getRandom(), itemStack.getCount(), i);
			itemStack.setCount(j);
		}

		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> addBonusBinomialDistributionCount(Holder<Enchantment> holder, float f, int i) {
		return simpleBuilder(list -> new ApplyBonusCount(list, holder, new ApplyBonusCount.BinomialWithBonusCount(i, f)));
	}

	public static LootItemConditionalFunction.Builder<?> addOreBonusCount(Holder<Enchantment> holder) {
		return simpleBuilder(list -> new ApplyBonusCount(list, holder, new ApplyBonusCount.OreDrops()));
	}

	public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Holder<Enchantment> holder) {
		return simpleBuilder(list -> new ApplyBonusCount(list, holder, new ApplyBonusCount.UniformBonusCount(1)));
	}

	public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Holder<Enchantment> holder, int i) {
		return simpleBuilder(list -> new ApplyBonusCount(list, holder, new ApplyBonusCount.UniformBonusCount(i)));
	}

	static record BinomialWithBonusCount(int extraRounds, float probability) implements ApplyBonusCount.Formula {
		private static final Codec<ApplyBonusCount.BinomialWithBonusCount> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("extra").forGetter(ApplyBonusCount.BinomialWithBonusCount::extraRounds),
						Codec.FLOAT.fieldOf("probability").forGetter(ApplyBonusCount.BinomialWithBonusCount::probability)
					)
					.apply(instance, ApplyBonusCount.BinomialWithBonusCount::new)
		);
		public static final ApplyBonusCount.FormulaType TYPE = new ApplyBonusCount.FormulaType(
			ResourceLocation.withDefaultNamespace("binomial_with_bonus_count"), CODEC
		);

		@Override
		public int calculateNewCount(RandomSource randomSource, int i, int j) {
			for (int k = 0; k < j + this.extraRounds; k++) {
				if (randomSource.nextFloat() < this.probability) {
					i++;
				}
			}

			return i;
		}

		@Override
		public ApplyBonusCount.FormulaType getType() {
			return TYPE;
		}
	}

	interface Formula {
		int calculateNewCount(RandomSource randomSource, int i, int j);

		ApplyBonusCount.FormulaType getType();
	}

	static record FormulaType(ResourceLocation id, Codec<? extends ApplyBonusCount.Formula> codec) {
	}

	static record OreDrops() implements ApplyBonusCount.Formula {
		public static final Codec<ApplyBonusCount.OreDrops> CODEC = Codec.unit(ApplyBonusCount.OreDrops::new);
		public static final ApplyBonusCount.FormulaType TYPE = new ApplyBonusCount.FormulaType(ResourceLocation.withDefaultNamespace("ore_drops"), CODEC);

		@Override
		public int calculateNewCount(RandomSource randomSource, int i, int j) {
			if (j > 0) {
				int k = randomSource.nextInt(j + 2) - 1;
				if (k < 0) {
					k = 0;
				}

				return i * (k + 1);
			} else {
				return i;
			}
		}

		@Override
		public ApplyBonusCount.FormulaType getType() {
			return TYPE;
		}
	}

	static record UniformBonusCount(int bonusMultiplier) implements ApplyBonusCount.Formula {
		public static final Codec<ApplyBonusCount.UniformBonusCount> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(Codec.INT.fieldOf("bonusMultiplier").forGetter(ApplyBonusCount.UniformBonusCount::bonusMultiplier))
					.apply(instance, ApplyBonusCount.UniformBonusCount::new)
		);
		public static final ApplyBonusCount.FormulaType TYPE = new ApplyBonusCount.FormulaType(ResourceLocation.withDefaultNamespace("uniform_bonus_count"), CODEC);

		@Override
		public int calculateNewCount(RandomSource randomSource, int i, int j) {
			return i + randomSource.nextInt(this.bonusMultiplier * j + 1);
		}

		@Override
		public ApplyBonusCount.FormulaType getType() {
			return TYPE;
		}
	}
}
