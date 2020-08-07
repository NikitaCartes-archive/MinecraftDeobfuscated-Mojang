package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("villager_trade");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TradeTrigger.TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
		EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "villager", deserializationContext);
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new TradeTrigger.TriggerInstance(composite, composite2, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, AbstractVillager abstractVillager, ItemStack itemStack) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, abstractVillager);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate.Composite villager;
		private final ItemPredicate item;

		public TriggerInstance(EntityPredicate.Composite composite, EntityPredicate.Composite composite2, ItemPredicate itemPredicate) {
			super(TradeTrigger.ID, composite);
			this.villager = composite2;
			this.item = itemPredicate;
		}

		public static TradeTrigger.TriggerInstance tradedWithVillager() {
			return new TradeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, ItemPredicate.ANY);
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
