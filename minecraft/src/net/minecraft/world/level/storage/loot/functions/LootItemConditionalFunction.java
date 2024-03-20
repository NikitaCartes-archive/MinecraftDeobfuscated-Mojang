package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public abstract class LootItemConditionalFunction implements LootItemFunction {
	protected final List<LootItemCondition> predicates;
	private final Predicate<LootContext> compositePredicates;

	protected LootItemConditionalFunction(List<LootItemCondition> list) {
		this.predicates = list;
		this.compositePredicates = Util.allOf(list);
	}

	protected static <T extends LootItemConditionalFunction> P1<Mu<T>, List<LootItemCondition>> commonFields(Instance<T> instance) {
		return instance.group(
			ExtraCodecs.strictOptionalField(LootItemConditions.DIRECT_CODEC.listOf(), "conditions", List.of())
				.forGetter(lootItemConditionalFunction -> lootItemConditionalFunction.predicates)
		);
	}

	public final ItemStack apply(ItemStack itemStack, LootContext lootContext) {
		return this.compositePredicates.test(lootContext) ? this.run(itemStack, lootContext) : itemStack;
	}

	protected abstract ItemStack run(ItemStack itemStack, LootContext lootContext);

	@Override
	public void validate(ValidationContext validationContext) {
		LootItemFunction.super.validate(validationContext);

		for (int i = 0; i < this.predicates.size(); i++) {
			((LootItemCondition)this.predicates.get(i)).validate(validationContext.forChild(".conditions[" + i + "]"));
		}
	}

	protected static LootItemConditionalFunction.Builder<?> simpleBuilder(Function<List<LootItemCondition>, LootItemFunction> function) {
		return new LootItemConditionalFunction.DummyBuilder(function);
	}

	public abstract static class Builder<T extends LootItemConditionalFunction.Builder<T>> implements LootItemFunction.Builder, ConditionUserBuilder<T> {
		private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();

		public T when(LootItemCondition.Builder builder) {
			this.conditions.add(builder.build());
			return this.getThis();
		}

		public final T unwrap() {
			return this.getThis();
		}

		protected abstract T getThis();

		protected List<LootItemCondition> getConditions() {
			return this.conditions.build();
		}
	}

	static final class DummyBuilder extends LootItemConditionalFunction.Builder<LootItemConditionalFunction.DummyBuilder> {
		private final Function<List<LootItemCondition>, LootItemFunction> constructor;

		public DummyBuilder(Function<List<LootItemCondition>, LootItemFunction> function) {
			this.constructor = function;
		}

		protected LootItemConditionalFunction.DummyBuilder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return (LootItemFunction)this.constructor.apply(this.getConditions());
		}
	}
}
