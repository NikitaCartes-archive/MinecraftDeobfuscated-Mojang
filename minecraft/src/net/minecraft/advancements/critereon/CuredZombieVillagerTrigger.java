package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public CuredZombieVillagerTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ContextAwarePredicate contextAwarePredicate2 = EntityPredicate.fromJson(jsonObject, "zombie", deserializationContext);
		ContextAwarePredicate contextAwarePredicate3 = EntityPredicate.fromJson(jsonObject, "villager", deserializationContext);
		return new CuredZombieVillagerTrigger.TriggerInstance(contextAwarePredicate, contextAwarePredicate2, contextAwarePredicate3);
	}

	public void trigger(ServerPlayer serverPlayer, Zombie zombie, Villager villager) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, zombie);
		LootContext lootContext2 = EntityPredicate.createContext(serverPlayer, villager);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, lootContext2));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ContextAwarePredicate zombie;
		private final ContextAwarePredicate villager;

		public TriggerInstance(
			ContextAwarePredicate contextAwarePredicate, ContextAwarePredicate contextAwarePredicate2, ContextAwarePredicate contextAwarePredicate3
		) {
			super(CuredZombieVillagerTrigger.ID, contextAwarePredicate);
			this.zombie = contextAwarePredicate2;
			this.villager = contextAwarePredicate3;
		}

		public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
			return new CuredZombieVillagerTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
		}

		public boolean matches(LootContext lootContext, LootContext lootContext2) {
			return !this.zombie.matches(lootContext) ? false : this.villager.matches(lootContext2);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("zombie", this.zombie.toJson(serializationContext));
			jsonObject.add("villager", this.villager.toJson(serializationContext));
			return jsonObject;
		}
	}
}
