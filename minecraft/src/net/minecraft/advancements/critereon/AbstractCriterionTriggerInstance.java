package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;

public abstract class AbstractCriterionTriggerInstance implements SimpleCriterionTrigger.SimpleInstance {
	private final Optional<ContextAwarePredicate> player;

	public AbstractCriterionTriggerInstance(Optional<ContextAwarePredicate> optional) {
		this.player = optional;
	}

	@Override
	public Optional<ContextAwarePredicate> playerPredicate() {
		return this.player;
	}

	@Override
	public JsonObject serializeToJson() {
		JsonObject jsonObject = new JsonObject();
		this.player.ifPresent(contextAwarePredicate -> jsonObject.add("player", contextAwarePredicate.toJson()));
		return jsonObject;
	}
}
