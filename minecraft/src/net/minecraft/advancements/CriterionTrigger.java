package net.minecraft.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.server.PlayerAdvancements;

public interface CriterionTrigger<T extends CriterionTriggerInstance> {
	void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<T> listener);

	void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<T> listener);

	void removePlayerListeners(PlayerAdvancements playerAdvancements);

	T createInstance(JsonObject jsonObject, DeserializationContext deserializationContext);

	default Criterion<T> createCriterion(T criterionTriggerInstance) {
		return new Criterion<>(this, criterionTriggerInstance);
	}

	public static record Listener<T extends CriterionTriggerInstance>(T trigger, AdvancementHolder advancement, String criterion) {
		public void run(PlayerAdvancements playerAdvancements) {
			playerAdvancements.award(this.advancement, this.criterion);
		}
	}
}
