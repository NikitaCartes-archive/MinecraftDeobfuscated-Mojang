package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public abstract class LootPoolEntryContainer implements ComposableEntryContainer {
	protected final LootItemCondition[] conditions;
	private final Predicate<LootContext> compositeCondition;

	protected LootPoolEntryContainer(LootItemCondition[] lootItemConditions) {
		this.conditions = lootItemConditions;
		this.compositeCondition = LootItemConditions.andConditions(lootItemConditions);
	}

	public void validate(
		LootTableProblemCollector lootTableProblemCollector,
		Function<ResourceLocation, LootTable> function,
		Set<ResourceLocation> set,
		LootContextParamSet lootContextParamSet
	) {
		for (int i = 0; i < this.conditions.length; i++) {
			this.conditions[i].validate(lootTableProblemCollector.forChild(".condition[" + i + "]"), function, set, lootContextParamSet);
		}
	}

	protected final boolean canRun(LootContext lootContext) {
		return this.compositeCondition.test(lootContext);
	}

	public abstract static class Builder<T extends LootPoolEntryContainer.Builder<T>> implements ConditionUserBuilder<T> {
		private final List<LootItemCondition> conditions = Lists.<LootItemCondition>newArrayList();

		protected abstract T getThis();

		public T when(LootItemCondition.Builder builder) {
			this.conditions.add(builder.build());
			return this.getThis();
		}

		public final T unwrap() {
			return this.getThis();
		}

		protected LootItemCondition[] getConditions() {
			return (LootItemCondition[])this.conditions.toArray(new LootItemCondition[0]);
		}

		public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> builder) {
			return new AlternativesEntry.Builder(this, builder);
		}

		public abstract LootPoolEntryContainer build();
	}

	public abstract static class Serializer<T extends LootPoolEntryContainer> {
		private final ResourceLocation name;
		private final Class<T> clazz;

		protected Serializer(ResourceLocation resourceLocation, Class<T> class_) {
			this.name = resourceLocation;
			this.clazz = class_;
		}

		public ResourceLocation getName() {
			return this.name;
		}

		public Class<T> getContainerClass() {
			return this.clazz;
		}

		public abstract void serialize(JsonObject jsonObject, T lootPoolEntryContainer, JsonSerializationContext jsonSerializationContext);

		public abstract T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions);
	}
}
