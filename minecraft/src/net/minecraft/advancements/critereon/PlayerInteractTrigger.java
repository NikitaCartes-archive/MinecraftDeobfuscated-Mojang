package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("player_interacted_with_entity");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	protected PlayerInteractTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		ContextAwarePredicate contextAwarePredicate2 = EntityPredicate.fromJson(jsonObject, "entity", deserializationContext);
		return new PlayerInteractTrigger.TriggerInstance(contextAwarePredicate, itemPredicate, contextAwarePredicate2);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, Entity entity) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;
		private final ContextAwarePredicate entity;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, ItemPredicate itemPredicate, ContextAwarePredicate contextAwarePredicate2) {
			super(PlayerInteractTrigger.ID, contextAwarePredicate);
			this.item = itemPredicate;
			this.entity = contextAwarePredicate2;
		}

		public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(
			ContextAwarePredicate contextAwarePredicate, ItemPredicate.Builder builder, ContextAwarePredicate contextAwarePredicate2
		) {
			return new PlayerInteractTrigger.TriggerInstance(contextAwarePredicate, builder.build(), contextAwarePredicate2);
		}

		public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(ItemPredicate.Builder builder, ContextAwarePredicate contextAwarePredicate) {
			return itemUsedOnEntity(ContextAwarePredicate.ANY, builder, contextAwarePredicate);
		}

		public boolean matches(ItemStack itemStack, LootContext lootContext) {
			return !this.item.matches(itemStack) ? false : this.entity.matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("entity", this.entity.toJson(serializationContext));
			return jsonObject;
		}
	}
}
