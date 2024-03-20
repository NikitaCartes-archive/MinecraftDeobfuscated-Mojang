package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ContextAwarePredicate {
	public static final Codec<ContextAwarePredicate> CODEC = LootItemConditions.DIRECT_CODEC
		.listOf()
		.xmap(ContextAwarePredicate::new, contextAwarePredicate -> contextAwarePredicate.conditions);
	private final List<LootItemCondition> conditions;
	private final Predicate<LootContext> compositePredicates;

	ContextAwarePredicate(List<LootItemCondition> list) {
		this.conditions = list;
		this.compositePredicates = Util.allOf(list);
	}

	public static ContextAwarePredicate create(LootItemCondition... lootItemConditions) {
		return new ContextAwarePredicate(List.of(lootItemConditions));
	}

	public boolean matches(LootContext lootContext) {
		return this.compositePredicates.test(lootContext);
	}

	public void validate(ValidationContext validationContext) {
		for (int i = 0; i < this.conditions.size(); i++) {
			LootItemCondition lootItemCondition = (LootItemCondition)this.conditions.get(i);
			lootItemCondition.validate(validationContext.forChild("[" + i + "]"));
		}
	}
}
