package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractCriterionTriggerInstance implements CriterionTriggerInstance {
	private final ResourceLocation criterion;
	private final ContextAwarePredicate player;

	public AbstractCriterionTriggerInstance(ResourceLocation resourceLocation, ContextAwarePredicate contextAwarePredicate) {
		this.criterion = resourceLocation;
		this.player = contextAwarePredicate;
	}

	@Override
	public ResourceLocation getCriterion() {
		return this.criterion;
	}

	protected ContextAwarePredicate getPlayerPredicate() {
		return this.player;
	}

	@Override
	public JsonObject serializeToJson(SerializationContext serializationContext) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("player", this.player.toJson(serializationContext));
		return jsonObject;
	}

	public String toString() {
		return "AbstractCriterionInstance{criterion=" + this.criterion + "}";
	}
}
