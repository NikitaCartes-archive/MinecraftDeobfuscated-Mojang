package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootPoolEntryContainer implements ComposableEntryContainer {
	protected final List<LootItemCondition> conditions;
	private final Predicate<LootContext> compositeCondition;

	protected LootPoolEntryContainer(List<LootItemCondition> list) {
		this.conditions = list;
		this.compositeCondition = Util.allOf(list);
	}

	protected static <T extends LootPoolEntryContainer> P1<Mu<T>, List<LootItemCondition>> commonFields(Instance<T> instance) {
		return instance.group(
			LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(lootPoolEntryContainer -> lootPoolEntryContainer.conditions)
		);
	}

	public void validate(ValidationContext validationContext) {
		for (int i = 0; i < this.conditions.size(); i++) {
			((LootItemCondition)this.conditions.get(i)).validate(validationContext.forChild(".condition[" + i + "]"));
		}
	}

	protected final boolean canRun(LootContext lootContext) {
		return this.compositeCondition.test(lootContext);
	}

	public abstract LootPoolEntryType getType();

	public abstract static class Builder<T extends LootPoolEntryContainer.Builder<T>> implements ConditionUserBuilder<T> {
		private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();

		protected abstract T getThis();

		public T when(LootItemCondition.Builder builder) {
			this.conditions.add(builder.build());
			return this.getThis();
		}

		public final T unwrap() {
			return this.getThis();
		}

		protected List<LootItemCondition> getConditions() {
			return this.conditions.build();
		}

		public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> builder) {
			return new AlternativesEntry.Builder(this, builder);
		}

		public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> builder) {
			return new EntryGroup.Builder(this, builder);
		}

		public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> builder) {
			return new SequentialEntry.Builder(this, builder);
		}

		public abstract LootPoolEntryContainer build();
	}
}
