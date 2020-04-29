package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public CuredZombieVillagerTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "zombie", deserializationContext);
		EntityPredicate.Composite composite3 = EntityPredicate.Composite.fromJson(jsonObject, "villager", deserializationContext);
		return new CuredZombieVillagerTrigger.TriggerInstance(composite, composite2, composite3);
	}

	public void trigger(ServerPlayer serverPlayer, Zombie zombie, Villager villager) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, zombie);
		LootContext lootContext2 = EntityPredicate.createContext(serverPlayer, villager);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, lootContext2));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate.Composite zombie;
		private final EntityPredicate.Composite villager;

		public TriggerInstance(EntityPredicate.Composite composite, EntityPredicate.Composite composite2, EntityPredicate.Composite composite3) {
			super(CuredZombieVillagerTrigger.ID, composite);
			this.zombie = composite2;
			this.villager = composite3;
		}

		public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
			return new CuredZombieVillagerTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY);
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
