package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Predicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface LootItemCondition extends LootContextUser, Predicate<LootContext> {
	LootItemConditionType getType();

	@FunctionalInterface
	public interface Builder {
		LootItemCondition build();

		default LootItemCondition.Builder invert() {
			return InvertedLootItemCondition.invert(this);
		}

		default AlternativeLootItemCondition.Builder or(LootItemCondition.Builder builder) {
			return AlternativeLootItemCondition.alternative(this, builder);
		}
	}
}
