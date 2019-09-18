package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("villager_trade");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TradeTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("villager"));
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new TradeTrigger.TriggerInstance(entityPredicate, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, AbstractVillager abstractVillager, ItemStack itemStack) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, abstractVillager, itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate villager;
		private final ItemPredicate item;

		public TriggerInstance(EntityPredicate entityPredicate, ItemPredicate itemPredicate) {
			super(TradeTrigger.ID);
			this.villager = entityPredicate;
			this.item = itemPredicate;
		}

		public static TradeTrigger.TriggerInstance tradedWithVillager() {
			return new TradeTrigger.TriggerInstance(EntityPredicate.ANY, ItemPredicate.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, AbstractVillager abstractVillager, ItemStack itemStack) {
			return !this.villager.matches(serverPlayer, abstractVillager) ? false : this.item.matches(itemStack);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("villager", this.villager.serializeToJson());
			return jsonObject;
		}
	}
}
