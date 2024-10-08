package net.minecraft.advancements.critereon;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class CriterionValidator {
	private final ProblemReporter reporter;
	private final HolderGetter.Provider lootData;

	public CriterionValidator(ProblemReporter problemReporter, HolderGetter.Provider provider) {
		this.reporter = problemReporter;
		this.lootData = provider;
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

	public void validate(ContextAwarePredicate contextAwarePredicate, ContextKeySet contextKeySet, String string) {
		contextAwarePredicate.validate(new ValidationContext(this.reporter.forChild(string), contextKeySet, this.lootData));
	}

	public void validate(List<ContextAwarePredicate> list, ContextKeySet contextKeySet, String string) {
		for (int i = 0; i < list.size(); i++) {
			ContextAwarePredicate contextAwarePredicate = (ContextAwarePredicate)list.get(i);
			contextAwarePredicate.validate(new ValidationContext(this.reporter.forChild(string + "[" + i + "]"), contextKeySet, this.lootData));
		}
	}
}
