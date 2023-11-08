package net.minecraft.advancements.critereon;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class CriterionValidator {
	private final ProblemReporter reporter;
	private final LootDataResolver lootData;

	public CriterionValidator(ProblemReporter problemReporter, LootDataResolver lootDataResolver) {
		this.reporter = problemReporter;
		this.lootData = lootDataResolver;
	}

	public void validateEntity(Optional<ContextAwarePredicate> optional, String string) {
		optional.ifPresent(contextAwarePredicate -> this.validateEntity(contextAwarePredicate, string));
	}

	public void validateEntities(List<ContextAwarePredicate> list, String string) {
		this.validate(list, LootContextParamSets.ADVANCEMENT_ENTITY, string);
	}

	public void validateEntity(ContextAwarePredicate contextAwarePredicate, String string) {
		this.validate(contextAwarePredicate, LootContextParamSets.ADVANCEMENT_ENTITY, string);
	}

	public void validate(ContextAwarePredicate contextAwarePredicate, LootContextParamSet lootContextParamSet, String string) {
		contextAwarePredicate.validate(new ValidationContext(this.reporter.forChild(string), lootContextParamSet, this.lootData));
	}

	public void validate(List<ContextAwarePredicate> list, LootContextParamSet lootContextParamSet, String string) {
		for (int i = 0; i < list.size(); i++) {
			ContextAwarePredicate contextAwarePredicate = (ContextAwarePredicate)list.get(i);
			contextAwarePredicate.validate(new ValidationContext(this.reporter.forChild(string + "[" + i + "]"), lootContextParamSet, this.lootData));
		}
	}
}
