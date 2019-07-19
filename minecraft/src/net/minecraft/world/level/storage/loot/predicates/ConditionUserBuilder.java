package net.minecraft.world.level.storage.loot.predicates;

public interface ConditionUserBuilder<T> {
	T when(LootItemCondition.Builder builder);

	T unwrap();
}
