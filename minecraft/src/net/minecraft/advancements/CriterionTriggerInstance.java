package net.minecraft.advancements;

import net.minecraft.advancements.critereon.CriterionValidator;

public interface CriterionTriggerInstance {
	void validate(CriterionValidator criterionValidator);
}
