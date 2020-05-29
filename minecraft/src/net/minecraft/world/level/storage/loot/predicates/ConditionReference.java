package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConditionReference implements LootItemCondition {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ResourceLocation name;

	private ConditionReference(ResourceLocation resourceLocation) {
		this.name = resourceLocation;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.REFERENCE;
	}

	@Override
	public void validate(ValidationContext validationContext) {
		if (validationContext.hasVisitedCondition(this.name)) {
			validationContext.reportProblem("Condition " + this.name + " is recursively called");
		} else {
			LootItemCondition.super.validate(validationContext);
			LootItemCondition lootItemCondition = validationContext.resolveCondition(this.name);
			if (lootItemCondition == null) {
				validationContext.reportProblem("Unknown condition table called " + this.name);
			} else {
				lootItemCondition.validate(validationContext.enterTable(".{" + this.name + "}", this.name));
			}
		}
	}

	public boolean test(LootContext lootContext) {
		LootItemCondition lootItemCondition = lootContext.getCondition(this.name);
		if (lootContext.addVisitedCondition(lootItemCondition)) {
			boolean var3;
			try {
				var3 = lootItemCondition.test(lootContext);
			} finally {
				lootContext.removeVisitedCondition(lootItemCondition);
			}

			return var3;
		} else {
			LOGGER.warn("Detected infinite loop in loot tables");
			return false;
		}
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConditionReference> {
		public void serialize(JsonObject jsonObject, ConditionReference conditionReference, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("name", conditionReference.name.toString());
		}

		public ConditionReference deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "name"));
			return new ConditionReference(resourceLocation);
		}
	}
}
