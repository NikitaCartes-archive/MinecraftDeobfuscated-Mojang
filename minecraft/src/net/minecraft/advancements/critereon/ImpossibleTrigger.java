package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;

public class ImpossibleTrigger implements CriterionTrigger<ImpossibleTrigger.TriggerInstance> {
	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> listener) {
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> listener) {
	}

	@Override
	public void removePlayerListeners(PlayerAdvancements playerAdvancements) {
	}

	@Override
	public Codec<ImpossibleTrigger.TriggerInstance> codec() {
		return ImpossibleTrigger.TriggerInstance.CODEC;
	}

	public static record TriggerInstance() implements CriterionTriggerInstance {
		public static final Codec<ImpossibleTrigger.TriggerInstance> CODEC = Codec.unit(new ImpossibleTrigger.TriggerInstance());

		@Override
		public void validate(CriterionValidator criterionValidator) {
		}
	}
}
