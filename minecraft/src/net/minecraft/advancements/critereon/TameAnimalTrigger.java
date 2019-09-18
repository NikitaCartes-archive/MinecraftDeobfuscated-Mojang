package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("tame_animal");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TameAnimalTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("entity"));
		return new TameAnimalTrigger.TriggerInstance(entityPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, Animal animal) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, animal));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate entity;

		public TriggerInstance(EntityPredicate entityPredicate) {
			super(TameAnimalTrigger.ID);
			this.entity = entityPredicate;
		}

		public static TameAnimalTrigger.TriggerInstance tamedAnimal() {
			return new TameAnimalTrigger.TriggerInstance(EntityPredicate.ANY);
		}

		public static TameAnimalTrigger.TriggerInstance tamedAnimal(EntityPredicate entityPredicate) {
			return new TameAnimalTrigger.TriggerInstance(entityPredicate);
		}

		public boolean matches(ServerPlayer serverPlayer, Animal animal) {
			return this.entity.matches(serverPlayer, animal);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("entity", this.entity.serializeToJson());
			return jsonObject;
		}
	}
}
