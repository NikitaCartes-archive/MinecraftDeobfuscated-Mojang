package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractCriterionTriggerInstance implements CriterionTriggerInstance {
	private final ResourceLocation criterion;
	private final Optional<ContextAwarePredicate> player;

	public AbstractCriterionTriggerInstance(ResourceLocation resourceLocation, Optional<ContextAwarePredicate> optional) {
		this.criterion = resourceLocation;
		this.player = optional;
	}

	@Override
	public ResourceLocation getCriterion() {
		return this.criterion;
	}

	protected Optional<ContextAwarePredicate> getPlayerPredicate() {
		return this.player;
	}

	@Override
	public JsonObject serializeToJson() {
		JsonObject jsonObject = new JsonObject();
		this.player.ifPresent(contextAwarePredicate -> jsonObject.add("player", contextAwarePredicate.toJson()));
		return jsonObject;
	}

	public String toString() {
		return "AbstractCriterionInstance{criterion=" + this.criterion + "}";
	}
}
