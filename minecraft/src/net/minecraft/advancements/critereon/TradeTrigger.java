package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("villager_trade");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TradeTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ContextAwarePredicate contextAwarePredicate2 = EntityPredicate.fromJson(jsonObject, "villager", deserializationContext);
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new TradeTrigger.TriggerInstance(contextAwarePredicate, contextAwarePredicate2, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, AbstractVillager abstractVillager, ItemStack itemStack) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, abstractVillager);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ContextAwarePredicate villager;
		private final ItemPredicate item;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, ContextAwarePredicate contextAwarePredicate2, ItemPredicate itemPredicate) {
			super(TradeTrigger.ID, contextAwarePredicate);
			this.villager = contextAwarePredicate2;
			this.item = itemPredicate;
		}

		public static TradeTrigger.TriggerInstance tradedWithVillager() {
			return new TradeTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ItemPredicate.ANY);
		}

		public static TradeTrigger.TriggerInstance tradedWithVillager(EntityPredicate.Builder builder) {
			return new TradeTrigger.TriggerInstance(EntityPredicate.wrap(builder.build()), ContextAwarePredicate.ANY, ItemPredicate.ANY);
		}

		public boolean matches(LootContext lootContext, ItemStack itemStack) {
			return !this.villager.matches(lootContext) ? false : this.item.matches(itemStack);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("villager", this.villager.toJson(serializationContext));
			return jsonObject;
		}
	}
}
