package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
	public static final Codec<LootPool> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(lootPool -> lootPool.entries),
					LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(lootPool -> lootPool.conditions),
					LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(lootPool -> lootPool.functions),
					NumberProviders.CODEC.fieldOf("rolls").forGetter(lootPool -> lootPool.rolls),
					NumberProviders.CODEC.fieldOf("bonus_rolls").orElse(ConstantValue.exactly(0.0F)).forGetter(lootPool -> lootPool.bonusRolls)
				)
				.apply(instance, LootPool::new)
	);
	private final List<LootPoolEntryContainer> entries;
	private final List<LootItemCondition> conditions;
	private final Predicate<LootContext> compositeCondition;
	private final List<LootItemFunction> functions;
	private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
	private final NumberProvider rolls;
	private final NumberProvider bonusRolls;

	LootPool(
		List<LootPoolEntryContainer> list, List<LootItemCondition> list2, List<LootItemFunction> list3, NumberProvider numberProvider, NumberProvider numberProvider2
	) {
		this.entries = list;
		this.conditions = list2;
		this.compositeCondition = Util.allOf(list2);
		this.functions = list3;
		this.compositeFunction = LootItemFunctions.compose(list3);
		this.rolls = numberProvider;
		this.bonusRolls = numberProvider2;
	}

	private void addRandomItem(Consumer<ItemStack> consumer, LootContext lootContext) {
		RandomSource randomSource = lootContext.getRandom();
		List<LootPoolEntry> list = Lists.<LootPoolEntry>newArrayList();
		MutableInt mutableInt = new MutableInt();

		for (LootPoolEntryContainer lootPoolEntryContainer : this.entries) {
			lootPoolEntryContainer.expand(lootContext, lootPoolEntryx -> {
				int i = lootPoolEntryx.getWeight(lootContext.getLuck());
				if (i > 0) {
					list.add(lootPoolEntryx);
					mutableInt.add(i);
				}
			});
		}

		int i = list.size();
		if (mutableInt.intValue() != 0 && i != 0) {
			if (i == 1) {
				((LootPoolEntry)list.get(0)).createItemStack(consumer, lootContext);
			} else {
				int j = randomSource.nextInt(mutableInt.intValue());

				for (LootPoolEntry lootPoolEntry : list) {
					j -= lootPoolEntry.getWeight(lootContext.getLuck());
					if (j < 0) {
						lootPoolEntry.createItemStack(consumer, lootContext);
						return;
					}
				}
			}
		}
	}

	public void addRandomItems(Consumer<ItemStack> consumer, LootContext lootContext) {
		if (this.compositeCondition.test(lootContext)) {
			Consumer<ItemStack> consumer2 = LootItemFunction.decorate(this.compositeFunction, consumer, lootContext);
			int i = this.rolls.getInt(lootContext) + Mth.floor(this.bonusRolls.getFloat(lootContext) * lootContext.getLuck());

			for (int j = 0; j < i; j++) {
				this.addRandomItem(consumer2, lootContext);
			}
		}
	}

	public void validate(ValidationContext validationContext) {
		for (int i = 0; i < this.conditions.size(); i++) {
			((LootItemCondition)this.conditions.get(i)).validate(validationContext.forChild(".condition[" + i + "]"));
		}

		for (int i = 0; i < this.functions.size(); i++) {
			((LootItemFunction)this.functions.get(i)).validate(validationContext.forChild(".functions[" + i + "]"));
		}

		for (int i = 0; i < this.entries.size(); i++) {
			((LootPoolEntryContainer)this.entries.get(i)).validate(validationContext.forChild(".entries[" + i + "]"));
		}

		this.rolls.validate(validationContext.forChild(".rolls"));
		this.bonusRolls.validate(validationContext.forChild(".bonusRolls"));
	}

	public static LootPool.Builder lootPool() {
		return new LootPool.Builder();
	}

	public static class Builder implements FunctionUserBuilder<LootPool.Builder>, ConditionUserBuilder<LootPool.Builder> {
		private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
		private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();
		private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
		private NumberProvider rolls = ConstantValue.exactly(1.0F);
		private NumberProvider bonusRolls = ConstantValue.exactly(0.0F);

		public LootPool.Builder setRolls(NumberProvider numberProvider) {
			this.rolls = numberProvider;
			return this;
		}

		public LootPool.Builder unwrap() {
			return this;
		}

		public LootPool.Builder setBonusRolls(NumberProvider numberProvider) {
			this.bonusRolls = numberProvider;
			return this;
		}

		public LootPool.Builder add(LootPoolEntryContainer.Builder<?> builder) {
			this.entries.add(builder.build());
			return this;
		}

		public LootPool.Builder when(LootItemCondition.Builder builder) {
			this.conditions.add(builder.build());
			return this;
		}

		public LootPool.Builder apply(LootItemFunction.Builder builder) {
			this.functions.add(builder.build());
			return this;
		}

		public LootPool build() {
			return new LootPool(this.entries.build(), this.conditions.build(), this.functions.build(), this.rolls, this.bonusRolls);
		}
	}
}
