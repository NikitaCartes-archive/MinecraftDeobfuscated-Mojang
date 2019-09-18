package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public CuredZombieVillagerTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("zombie"));
		EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("villager"));
		return new CuredZombieVillagerTrigger.TriggerInstance(entityPredicate, entityPredicate2);
	}

	public void trigger(ServerPlayer serverPlayer, Zombie zombie, Villager villager) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, zombie, villager));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate zombie;
		private final EntityPredicate villager;

		public TriggerInstance(EntityPredicate entityPredicate, EntityPredicate entityPredicate2) {
			super(CuredZombieVillagerTrigger.ID);
			this.zombie = entityPredicate;
			this.villager = entityPredicate2;
		}

		public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
			return new CuredZombieVillagerTrigger.TriggerInstance(EntityPredicate.ANY, EntityPredicate.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Zombie zombie, Villager villager) {
			return !this.zombie.matches(serverPlayer, zombie) ? false : this.villager.matches(serverPlayer, villager);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("zombie", this.zombie.serializeToJson());
			jsonObject.add("villager", this.villager.serializeToJson());
			return jsonObject;
		}
	}
}
