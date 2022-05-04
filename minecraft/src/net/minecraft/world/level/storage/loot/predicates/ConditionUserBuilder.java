package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Function;

public interface ConditionUserBuilder<T extends ConditionUserBuilder<T>> {
	T when(LootItemCondition.Builder builder);

	default <E> T when(Iterable<E> iterable, Function<E, LootItemCondition.Builder> function) {
		T conditionUserBuilder = this.unwrap();

		for (E object : iterable) {
			conditionUserBuilder = conditionUserBuilder.when((LootItemCondition.Builder)function.apply(object));
		}

		return conditionUserBuilder;
	}

	T unwrap();
}
