package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("bred_animals");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public BredAnimalsTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ContextAwarePredicate contextAwarePredicate2 = EntityPredicate.fromJson(jsonObject, "parent", deserializationContext);
		ContextAwarePredicate contextAwarePredicate3 = EntityPredicate.fromJson(jsonObject, "partner", deserializationContext);
		ContextAwarePredicate contextAwarePredicate4 = EntityPredicate.fromJson(jsonObject, "child", deserializationContext);
		return new BredAnimalsTrigger.TriggerInstance(contextAwarePredicate, contextAwarePredicate2, contextAwarePredicate3, contextAwarePredicate4);
	}

	public void trigger(ServerPlayer serverPlayer, Animal animal, Animal animal2, @Nullable AgeableMob ageableMob) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, animal);
		LootContext lootContext2 = EntityPredicate.createContext(serverPlayer, animal2);
		LootContext lootContext3 = ageableMob != null ? EntityPredicate.createContext(serverPlayer, ageableMob) : null;
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, lootContext2, lootContext3));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ContextAwarePredicate parent;
		private final ContextAwarePredicate partner;
		private final ContextAwarePredicate child;

		public TriggerInstance(
			ContextAwarePredicate contextAwarePredicate,
			ContextAwarePredicate contextAwarePredicate2,
			ContextAwarePredicate contextAwarePredicate3,
			ContextAwarePredicate contextAwarePredicate4
		) {
			super(BredAnimalsTrigger.ID, contextAwarePredicate);
			this.parent = contextAwarePredicate2;
			this.partner = contextAwarePredicate3;
			this.child = contextAwarePredicate4;
		}

		public static BredAnimalsTrigger.TriggerInstance bredAnimals() {
			return new BredAnimalsTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
		}

		public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate.Builder builder) {
			return new BredAnimalsTrigger.TriggerInstance(
				ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, EntityPredicate.wrap(builder.build())
			);
		}

		public static BredAnimalsTrigger.TriggerInstance bredAnimals(
			EntityPredicate entityPredicate, EntityPredicate entityPredicate2, EntityPredicate entityPredicate3
		) {
			return new BredAnimalsTrigger.TriggerInstance(
				ContextAwarePredicate.ANY, EntityPredicate.wrap(entityPredicate), EntityPredicate.wrap(entityPredicate2), EntityPredicate.wrap(entityPredicate3)
			);
		}

		public boolean matches(LootContext lootContext, LootContext lootContext2, @Nullable LootContext lootContext3) {
			return this.child == ContextAwarePredicate.ANY || lootContext3 != null && this.child.matches(lootContext3)
				? this.parent.matches(lootContext) && this.partner.matches(lootContext2) || this.parent.matches(lootContext2) && this.partner.matches(lootContext)
				: false;
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("parent", this.parent.toJson(serializationContext));
			jsonObject.add("partner", this.partner.toJson(serializationContext));
			jsonObject.add("child", this.child.toJson(serializationContext));
			return jsonObject;
		}
	}
}
