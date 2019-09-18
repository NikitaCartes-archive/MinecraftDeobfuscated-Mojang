package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.animal.Animal;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("bred_animals");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public BredAnimalsTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("parent"));
		EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("partner"));
		EntityPredicate entityPredicate3 = EntityPredicate.fromJson(jsonObject.get("child"));
		return new BredAnimalsTrigger.TriggerInstance(entityPredicate, entityPredicate2, entityPredicate3);
	}

	public void trigger(ServerPlayer serverPlayer, Animal animal, @Nullable Animal animal2, @Nullable AgableMob agableMob) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, animal, animal2, agableMob));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate parent;
		private final EntityPredicate partner;
		private final EntityPredicate child;

		public TriggerInstance(EntityPredicate entityPredicate, EntityPredicate entityPredicate2, EntityPredicate entityPredicate3) {
			super(BredAnimalsTrigger.ID);
			this.parent = entityPredicate;
			this.partner = entityPredicate2;
			this.child = entityPredicate3;
		}

		public static BredAnimalsTrigger.TriggerInstance bredAnimals() {
			return new BredAnimalsTrigger.TriggerInstance(EntityPredicate.ANY, EntityPredicate.ANY, EntityPredicate.ANY);
		}

		public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate.Builder builder) {
			return new BredAnimalsTrigger.TriggerInstance(builder.build(), EntityPredicate.ANY, EntityPredicate.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Animal animal, @Nullable Animal animal2, @Nullable AgableMob agableMob) {
			return !this.child.matches(serverPlayer, agableMob)
				? false
				: this.parent.matches(serverPlayer, animal) && this.partner.matches(serverPlayer, animal2)
					|| this.parent.matches(serverPlayer, animal2) && this.partner.matches(serverPlayer, animal);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("parent", this.parent.serializeToJson());
			jsonObject.add("partner", this.partner.serializeToJson());
			jsonObject.add("child", this.child.serializeToJson());
			return jsonObject;
		}
	}
}
