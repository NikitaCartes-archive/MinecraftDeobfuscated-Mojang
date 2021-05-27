package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger extends SimpleCriterionTrigger<LightningStrikeTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("lightning_strike");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public LightningStrikeTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "lightning", deserializationContext);
		EntityPredicate.Composite composite3 = EntityPredicate.Composite.fromJson(jsonObject, "bystander", deserializationContext);
		return new LightningStrikeTrigger.TriggerInstance(composite, composite2, composite3);
	}

	public void trigger(ServerPlayer serverPlayer, LightningBolt lightningBolt, List<Entity> list) {
		List<LootContext> list2 = (List<LootContext>)list.stream().map(entity -> EntityPredicate.createContext(serverPlayer, entity)).collect(Collectors.toList());
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, lightningBolt);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, list2));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate.Composite lightning;
		private final EntityPredicate.Composite bystander;

		public TriggerInstance(EntityPredicate.Composite composite, EntityPredicate.Composite composite2, EntityPredicate.Composite composite3) {
			super(LightningStrikeTrigger.ID, composite);
			this.lightning = composite2;
			this.bystander = composite3;
		}

		public static LightningStrikeTrigger.TriggerInstance lighthingStrike(EntityPredicate entityPredicate, EntityPredicate entityPredicate2) {
			return new LightningStrikeTrigger.TriggerInstance(
				EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(entityPredicate), EntityPredicate.Composite.wrap(entityPredicate2)
			);
		}

		public boolean matches(LootContext lootContext, List<LootContext> list) {
			return !this.lightning.matches(lootContext) ? false : this.bystander == EntityPredicate.Composite.ANY || !list.stream().noneMatch(this.bystander::matches);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("lightning", this.lightning.toJson(serializationContext));
			jsonObject.add("bystander", this.bystander.toJson(serializationContext));
			return jsonObject;
		}
	}
}
